package com.example.geminipro.Adapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.geminipro.Util.Utils;
import com.example.geminipro.databinding.ItemFlexboxBinding;
import java.util.Random;

public class FlexAdapter extends RecyclerView.Adapter<FlexAdapter.FlexViewHolder> {
    private final Context context;
    private String[] settingTitle;
    private FlexAdapterListener listener;

    public FlexAdapter(Context context, FlexAdapterListener listener){
        this.context = context;
        this.settingTitle = new String[0];
        this.listener = listener;
    }

    @NonNull
    @Override
    public FlexViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFlexboxBinding binding = ItemFlexboxBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new FlexViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FlexViewHolder holder, int position) {
        String[] text = settingTitle[holder.getAdapterPosition()].split(":");
        if (text.length == 2){
            holder.binding.FlexTitle.setText(text[0]);
            holder.binding.FlexBelow.setText(text[1]);
        }

        if (position == 0) holder.binding.itemFlexboxLayout.setPadding(Utils.convertDpToPixel(15, context),0,Utils.convertDpToPixel(10, context),Utils.convertDpToPixel(15, context));
        else if (position == settingTitle.length - 1) holder.binding.itemFlexboxLayout.setPadding(0,0,Utils.convertDpToPixel(15, context),Utils.convertDpToPixel(15, context));
        else  holder.binding.itemFlexboxLayout.setPadding(0,0,Utils.convertDpToPixel(10, context),Utils.convertDpToPixel(15, context));

        holder.binding.flexCardView.setTag(holder.getAdapterPosition());
        holder.binding.flexCardView.setOnClickListener(onClickListener);
    }

    @Override
    public int getItemCount() {
        return settingTitle.length;
    }

    public void setSettingTitle(String[] settingTitle){
        this.settingTitle = settingTitle;

        this.settingTitle = getRandomTitles(settingTitle, 4);
        notifyDataSetChanged();
    }

    private String[] getRandomTitles(String[] allTitles, int count) {
        if (allTitles == null || allTitles.length == 0 || count <= 0) {
            return new String[0];
        }

        Random random = new Random();
        int totalTitles = allTitles.length;

        count = Math.min(count, totalTitles);

        String[] randomTitles = new String[count];
        boolean[] chosen = new boolean[totalTitles];

        for (int i = 0; i < count; i++) {
            int randomIndex;
            do {
                randomIndex = random.nextInt(totalTitles);
            } while (chosen[randomIndex]);

            randomTitles[i] = allTitles[randomIndex];
            chosen[randomIndex] = true;
        }
        return randomTitles;
    }

    private final View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = (int) v.getTag();
            String text = settingTitle[position];
            if (!text.isEmpty() && null != listener) listener.onChooseFlex(text);
        }
    };

    public static class FlexViewHolder extends RecyclerView.ViewHolder {
        public final ItemFlexboxBinding binding;
        public FlexViewHolder(@NonNull ItemFlexboxBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public interface FlexAdapterListener {
        void onChooseFlex(String text);
    }
}
