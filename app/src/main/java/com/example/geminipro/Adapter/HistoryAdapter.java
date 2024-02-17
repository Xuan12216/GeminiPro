package com.example.geminipro.Adapter;

import android.content.Context;
import android.content.Intent;
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
import com.example.geminipro.databinding.RecyclerHistoryItemBinding;

import java.util.ArrayList;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {
    private final Context context;
    private List<User> title = new ArrayList<>();
    private final HistoryAdapterListener listener;

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
                holder.binding.historyTitle.setText(user.getTitle());
                holder.binding.imageViewPin.setVisibility(user.isPin() ? View.VISIBLE : View.GONE);

                holder.itemView.setOnClickListener(onClickListener);
                holder.itemView.setOnLongClickListener(onClickListenerMore);
                holder.itemView.setTag(holder.getAdapterPosition());
            }
        }
    }

    @Override
    public int getItemCount() {
        return title.size();
    }

    public void setSettingTitle(List<User> title){
        this.title = title;
        notifyDataSetChanged();
    }
    
    private final View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            int position = (int) v.getTag();
            User user = title.get(position);
            if (null != user && null != listener) listener.onChooseHistory(user);
        }
    };

    private final View.OnLongClickListener onClickListenerMore = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            int position = (int) v.getTag();
            User user = title.get(position);

            PopupMenu popupMenu = new PopupMenu(context, v, Gravity.END, 0, R.style.MyPopupMenuStyle);
            popupMenu.setForceShowIcon(true);
            popupMenu.inflate(R.menu.menu_more_item);
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if (item.getItemId() == R.id.pin) {
                        if (null != user && null != listener) listener.onChooseHistoryStatus(user, "pin", "");
                        return true;
                    }
                    else if (item.getItemId() == R.id.rename){
                        if (null != user && null != listener) listener.onChooseHistoryStatus(user, "rename", "");
                        return true;
                    }
                    else if (item.getItemId() == R.id.delete){
                        if (null != user && null != listener) listener.onChooseHistoryStatus(user, "delete", "");
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
        void onChooseHistoryStatus(User user, String status, String rename);
    }
}