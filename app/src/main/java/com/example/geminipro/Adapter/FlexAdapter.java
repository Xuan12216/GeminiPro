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
import com.example.geminipro.databinding.ItemFlexboxBinding;
import com.example.geminipro.databinding.RecyclerItemBinding;
import com.example.geminipro.enums.ResType;

public class FlexAdapter extends RecyclerView.Adapter<FlexAdapter.FlexViewHolder> {
    private final Context context;
    private String[] settingTitle;

    public FlexAdapter(Context context){
        this.context = context;
        this.settingTitle = new String[0];
    }
    @NonNull
    @Override
    public FlexViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFlexboxBinding binding = ItemFlexboxBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new FlexViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FlexViewHolder holder, int position) {
        String[] text = settingTitle[position].split(":");
        if (text.length == 2){
            holder.binding.FlexTitle.setText(text[0]);
            holder.binding.FlexBelow.setText(text[1]);
        }
    }

    @Override
    public int getItemCount() {
        return settingTitle.length;
    }

    public void setSettingTitle(String[] settingTitle){
        this.settingTitle = settingTitle;
        notifyDataSetChanged();
    }

    public static class FlexViewHolder extends RecyclerView.ViewHolder {
        public final ItemFlexboxBinding binding;
        public FlexViewHolder(@NonNull ItemFlexboxBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
