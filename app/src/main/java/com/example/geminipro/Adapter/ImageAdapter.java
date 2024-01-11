package com.example.geminipro.Adapter;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.geminipro.R;
import com.example.geminipro.databinding.ItemImageBinding;

import java.util.ArrayList;
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    private List<Uri> imageUris = new ArrayList<>();
    private final Context context;
    private boolean isShowCloseBtn = false;
    private ImageAdapterListener listener;

    public ImageAdapter(Context context, ImageAdapterListener listener) {
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemImageBinding binding = ItemImageBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ImageViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Glide.with(context).clear(holder.binding.imageView);
        Uri imageUri = imageUris.get(position);
        Glide.with(context)
                .load(imageUri)
                .into(holder.binding.imageView);

        holder.binding.closeImageView.setVisibility(isShowCloseBtn ? View.VISIBLE : View.GONE);

        holder.binding.closeImageView.setOnClickListener(v -> {
            removeImage(position);
        });
    }

    public void removeImage(int position) {
        if (position >= 0 && position < imageUris.size()) {
            imageUris.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, getItemCount());

            if (listener != null) listener.onImageListUpdated(imageUris);
        }
    }

    @Override
    public int getItemCount() {
        return imageUris.size();
    }

    public void setNewImage(List<Uri> imageUris, boolean isShowCloseBtn) {
        this.imageUris = imageUris;
        this.isShowCloseBtn = isShowCloseBtn;
        notifyDataSetChanged();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        public final ItemImageBinding binding;

        public ImageViewHolder(@NonNull ItemImageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public interface ImageAdapterListener {
        void onImageListUpdated(List<Uri> updatedImageUris);
    }

}
