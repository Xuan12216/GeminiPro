package com.example.geminipro.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.example.geminipro.Activity.InfoActivity;
import com.example.geminipro.Activity.MainActivity;
import com.example.geminipro.Activity.SettingsActivity;
import com.example.geminipro.Database.User;
import com.example.geminipro.R;
import com.example.geminipro.Util.CustomDialog;
import com.example.geminipro.databinding.RecyclerHistoryItemBinding;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {
    private final Context context;
    private List<User> title = new ArrayList<>();
    private final HistoryAdapterListener listener;
    private static String targetTitle = "";

    public HistoryAdapter(Context context, HistoryAdapterListener listener){
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerHistoryItemBinding binding = RecyclerHistoryItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new HistoryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        if (!title.isEmpty() && position < title.size() && position >= 0){
            User user = title.get(holder.getAdapterPosition());
            if (user != null) {
                String date = user.getDate();
                String formattedDate = formatDate(date);

                if (position > 0 && title.get(position - 1).getDate().equals(date)) {
                    holder.binding.textViewDate.setVisibility(View.GONE);
                } else {
                    holder.binding.textViewDate.setVisibility(View.VISIBLE);
                    holder.binding.textViewDate.setText(formattedDate);
                }
                holder.binding.historyTitle.setText(user.getTitle());
                holder.binding.imageViewPin.setImageResource(user.isPin() ? R.drawable.baseline_push_pin_24 : R.drawable.baseline_chat_bubble_24);

                holder.itemView.setOnClickListener(onClickListener);
                holder.itemView.setOnLongClickListener(onClickListenerMore);
                holder.itemView.setTag(holder);

                if (!targetTitle.isEmpty() && targetTitle.equals(user.getTitle())){
                    holder.binding.layout.setBackgroundResource(R.drawable.recycler_item_click);
                }
                else holder.binding.layout.setBackgroundColor(context.getResources().getColor(R.color.transparent, null));
            }
        }
    }

    private String formatDate(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String formattedDate = "";

        try {
            Date currentDate = sdf.parse(sdf.format(new Date()));
            Date itemDate = sdf.parse(date);

            assert currentDate != null;
            assert itemDate != null;
            long diffInMillies = Math.abs(currentDate.getTime() - itemDate.getTime());
            long diff = diffInMillies / (24 * 60 * 60 * 1000);

            if (diff == 0) {
                formattedDate = context.getResources().getString(R.string.today);
            } else if (diff == 1) {
                formattedDate = context.getResources().getString(R.string.yesterday);
            } else {
                // Format date as needed for other cases
                SimpleDateFormat sdfOutput = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
                formattedDate = sdfOutput.format(itemDate);
            }
        } 
        catch (ParseException e) {e.printStackTrace();}

        return formattedDate;
    }
    
    @Override
    public int getItemCount() {
        return title.size();
    }

    public void setSettingTitle(List<User> title){
        this.title = title;
        notifyDataSetChanged();
    }

    public void setTargetTitle(String title){
        targetTitle = title;
        notifyDataSetChanged();
    }

    public String getTargetTitle() {
        return targetTitle;
    }
    
    private final View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            HistoryViewHolder holder = (HistoryViewHolder) v.getTag();
            int position = holder.getAdapterPosition();
            User user = title.get(position);

            targetTitle = holder.binding.historyTitle.getText().toString();
            if (null != user && null != listener) listener.onChooseHistory(user);
            notifyDataSetChanged();
        }
    };

    private final View.OnLongClickListener onClickListenerMore = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            HistoryViewHolder holder = (HistoryViewHolder) v.getTag();
            int position = holder.getAdapterPosition();
            User user = title.get(position);
            boolean isPinned = false;
            if (null != user) isPinned = user.isPin();

            PopupMenu popupMenu = new PopupMenu(context, v, Gravity.END, 0, R.style.MyPopupMenuStyle);
            popupMenu.setForceShowIcon(true);
            popupMenu.inflate(R.menu.menu_more_item);

            MenuItem pinMenuItem = popupMenu.getMenu().findItem(R.id.pin);
            pinMenuItem.setTitle(isPinned ? R.string.menu_uppin : R.string.menu_pin);
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if (item.getItemId() == R.id.pin) {
                        if (null != user && null != listener) {
                            user.setPin(!user.isPin());
                            listener.onChooseHistoryStatus(user, "pin");
                        }
                        return true;
                    }
                    else if (item.getItemId() == R.id.rename){
                        CustomDialog dialog = new CustomDialog(context, title, false, new CustomDialog.onEditSuccess() {
                            @Override
                            public void onSuccess(String rename) {
                                if (null != user && null != listener) {
                                    user.setTitle(rename);
                                    listener.onChooseHistoryStatus(user, "rename");
                                }
                            }
                        });
                        dialog.show();
                        return true;
                    }
                    else if (item.getItemId() == R.id.delete){
                        CustomDialog dialog = new CustomDialog(context, title, true,new CustomDialog.onEditSuccess() {
                            @Override
                            public void onSuccess(String status) {
                                if (null != user && null != listener && status.equals("true")) listener.onChooseHistoryStatus(user, "delete");
                            }
                        });
                        dialog.show();
                        return  true;
                    }
                    return false;
                }
            });
            popupMenu.show();
            return false;
        }
    };

    public static class HistoryViewHolder extends RecyclerView.ViewHolder {
        public final RecyclerHistoryItemBinding binding;
        public HistoryViewHolder(@NonNull RecyclerHistoryItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public interface HistoryAdapterListener {
        void onChooseHistory(User user);
        void onChooseHistoryStatus(User user, String status);
    }
}