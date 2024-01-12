package com.example.geminipro.Adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.geminipro.R;
import com.example.geminipro.databinding.RecyclerItemBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class ModelAdapter extends RecyclerView.Adapter<ModelAdapter.ModelViewHolder> implements ImageAdapter.ImageAdapterListener {

    private List<String> StringUris = new ArrayList<>();
    private List<String> userOrGemini = new ArrayList<>();
    private HashMap<Integer,List<Uri>> imageHashMap = new HashMap<Integer,List<Uri>>();
    private final Context context;
    private SharedPreferences preferences;
    private String geminiName, userName, storedImagePath;

    public ModelAdapter(Context context) {
        this.context = context;
        checkSharedPreferences();
    }

    @NonNull
    @Override
    public ModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerItemBinding binding = RecyclerItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ModelViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ModelViewHolder holder, int position) {

        holder.binding.recyclerViewModel.setAdapter(null);

        if (imageHashMap.containsKey(position)){
            holder.binding.recyclerViewModel.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
            ImageAdapter adapterDown = new ImageAdapter(context, this);
            holder.binding.recyclerViewModel.setAdapter(adapterDown);
            adapterDown.setNewImage(imageHashMap.get(position), false);
        }

        String who = userOrGemini.get(position);
        String text = StringUris.get(position);
        holder.binding.messageTextView.setText(text);
        holder.binding.avatarCardView.setCardBackgroundColor(("User").equals(who) ? context.getResources().getColor(R.color.navy_blue,null) : context.getResources().getColor(R.color.transparent,null));
        holder.binding.usernameTextView.setText(("User").equals(who) ? userName : geminiName);

        if (storedImagePath.isEmpty()){
            Glide.with(context)
                    .load(("User").equals(who) ? R.drawable.baseline_person_24 : R.mipmap.gemini)
                    .into(holder.binding.avatarImageView);
        }
        else {
            Glide.with(context)
                    .load(("User").equals(who) ? storedImagePath : R.mipmap.gemini)
                    .into(holder.binding.avatarImageView);
        }
    }

    @Override
    public int getItemCount() {
        return StringUris.size();
    }

    public void addData(String resultText,List<Uri> imageUris, String who, int index) {
        StringUris.add(resultText);
        userOrGemini.add(who);
        List<Uri> newImageUris = new ArrayList<>(imageUris);
        if (newImageUris.size() > 0 && !"Gemini".equals(who)) imageHashMap.put(index,newImageUris);
        notifyDataSetChanged();
    }

    public void refreshData(String text, int index){
        StringUris.set(index, text);
        notifyDataSetChanged();
    }

    public void checkSharedPreferences() {
        if (context != null){
            preferences = context.getSharedPreferences("your_private_prefs", Context.MODE_PRIVATE);
            geminiName = preferences.getString("geminiName", "");
            userName = preferences.getString("userName", "");
            storedImagePath = preferences.getString("userImage", "");
            if (geminiName.isEmpty()) geminiName = "Gemini";
            if (userName.isEmpty()) userName  = "You";
        }
    }

    @Override
    public void onImageListUpdated(List<Uri> updatedImageUris) {}

    public static class ModelViewHolder extends RecyclerView.ViewHolder {
        public final RecyclerItemBinding binding;

        public ModelViewHolder(@NonNull RecyclerItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
