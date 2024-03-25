package com.example.geminipro.Adapter;

import android.content.Context;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.geminipro.Database.User;
import com.example.geminipro.R;
import com.example.geminipro.Util.MyPopupMenu;
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
                boolean isPin = user.isPin();

                holder.binding.divider.setVisibility(View.GONE);

                if (position > 0 && title.get(position - 1).isPin() == isPin ) {
                    holder.binding.textViewDate.setVisibility(View.GONE);
                }
                else {
                    holder.binding.textViewDate.setVisibility(View.VISIBLE);
                    holder.binding.textViewDate.setText(context.getString(R.string.menu_pin));
                }

                if (position > 0 && title.get(position - 1).getDate().equals(date) && !title.get(position - 1).isPin()) {
                    holder.binding.textViewDate.setVisibility(View.GONE);
                }
                else if (!isPin){
                    holder.binding.divider.setVisibility(View.VISIBLE);
                    holder.binding.textViewDate.setVisibility(View.VISIBLE);
                    holder.binding.textViewDate.setText(formattedDate);
                }

                holder.binding.historyTitle.setText(user.getTitle());
                holder.binding.imageViewPin.setImageResource(isPin ? R.drawable.baseline_push_pin_24 : R.drawable.baseline_chat_bubble_24);

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

    public List<User> getSettingTitle() {
        return title;
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

            MyPopupMenu popupMenu = new MyPopupMenu(context, R.menu.menu_more_item, v, title, listener, user);
            popupMenu.startPopUp();
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
        void onChooseHistoryStatus(User user, String status, String oriName);
    }
}