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
import com.example.geminipro.Util.Utils;
import com.example.geminipro.databinding.RecyclerHistoryItemBinding;
import com.example.geminipro.enums.ResType;

public class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.SettingViewHolder> {
    private final Context context;
    private String[] settingTitle, settingIcon;
    private final GetResourceData resourceData;
    private boolean state = false;

    public SettingsAdapter(Context context, boolean state){
        this.context = context;
        this.settingTitle = new String[0];
        this.settingIcon = new String[0];
        this.state = state;
        resourceData = new GetResourceData(context);
    }
    @NonNull
    @Override
    public SettingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerHistoryItemBinding binding = RecyclerHistoryItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new SettingViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SettingViewHolder holder, int position) {

        if (settingTitle.length != settingIcon.length || position < 0 || position >= settingTitle.length) {
            return;
        }

        // 获取 CardView 的布局参数
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) holder.binding.cardViewHistory.getLayoutParams();

        // 设置边距
        layoutParams.leftMargin = Utils.convertDpToPixel(16, context);
        layoutParams.topMargin = Utils.convertDpToPixel(8, context);
        layoutParams.bottomMargin = Utils.convertDpToPixel(8, context);

        // 应用新的布局参数
        holder.binding.cardViewHistory.setLayoutParams(layoutParams);


        holder.binding.textViewDate.setVisibility(View.GONE);
        holder.binding.divider.setVisibility(View.GONE);
        String text = settingTitle[position];
        String textIcon = settingIcon[position];

        holder.binding.historyTitle.setText(text);
        holder.binding.imageViewPin.setImageResource(resourceData.getResource(textIcon, ResType.drawable));

        holder.itemView.setOnClickListener(onClickListener);
        int pos = holder.getAdapterPosition();
        if (state) pos += 4;
        holder.itemView.setTag(String.valueOf(pos));
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
        public final RecyclerHistoryItemBinding binding;
        public SettingViewHolder(@NonNull RecyclerHistoryItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
