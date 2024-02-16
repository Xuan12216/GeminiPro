package com.example.geminipro.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.geminipro.Activity.SettingMainActivity;
import com.example.geminipro.Util.GetResourceData;
import com.example.geminipro.databinding.RecyclerItemBinding;
import com.example.geminipro.enums.ResType;

public class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.SettingViewHolder> {
    private final Context context;
    private String[] settingTitle, settingIcon;
    private GetResourceData resourceData;

    public SettingsAdapter(Context context){
        this.context = context;
        this.settingTitle = new String[0];
        this.settingIcon = new String[0];
        resourceData = new GetResourceData(context);
    }
    @NonNull
    @Override
    public SettingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerItemBinding binding = RecyclerItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new SettingViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SettingViewHolder holder, int position) {

        if (settingTitle.length != settingIcon.length || position < 0 || position >= settingTitle.length) {
            return;
        }

        holder.binding.avatarCardView.setVisibility(View.GONE);
        String text = settingTitle[position];
        String textIcon = settingIcon[position];

        holder.binding.messageTextView.setText(text);
        holder.binding.messageTextView.setTextIsSelectable(false);
        holder.binding.usernameTextView.setText("");
        holder.binding.imageNext.setVisibility(View.VISIBLE);
        holder.binding.imageViewSetting.setVisibility(View.VISIBLE);
        holder.binding.imageViewSetting.setImageResource(resourceData.getResource(textIcon, ResType.drawable));

        holder.itemView.setOnClickListener(onClickListener);
        holder.itemView.setTag(String.valueOf(holder.getAdapterPosition()));
    }

    private final View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String id = String.valueOf(v.getTag());
            Intent intent = new Intent(context, SettingMainActivity.class);

            if (!id.isEmpty()) intent.putExtra("id", id);
            else intent.putExtra("id", "0");

            context.startActivity(intent);
        }
    };

    @Override
    public int getItemCount() {
        return settingTitle.length;
    }

    public void setSettingTitle(String[] settingTitle, String[] settingIcon) {
        this.settingTitle = settingTitle;
        this.settingIcon = settingIcon;
        notifyDataSetChanged();
    }


    public static class SettingViewHolder extends RecyclerView.ViewHolder {
        public final RecyclerItemBinding binding;
        public SettingViewHolder(@NonNull RecyclerItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
