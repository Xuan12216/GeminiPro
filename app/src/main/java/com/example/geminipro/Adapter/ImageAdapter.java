package com.example.geminipro.Adapter;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.geminipro.Util.ImageDialog;
import com.example.geminipro.databinding.ItemImageBinding;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    private List<Uri> imageUris = new ArrayList<>();
    private final Context context;
    private boolean isShowCloseBtn = false;
    private final ImageAdapterListener listener;

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
        Uri imageUri = imageUris.get(holder.getAdapterPosition());
        Glide.with(context)
                .load(imageUri)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        holder.binding.imageView.setOnLongClickListener(view -> {
                            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                            showImageDialog(imageUris.get(holder.getAdapterPosition()), holder);
                            return true;
                        });
                        return false;
                    }
                })
                .into(holder.binding.imageView);

        holder.binding.closeImageView.setVisibility(isShowCloseBtn ? View.VISIBLE : View.GONE);

        holder.binding.closeImageView.setOnClickListener(v -> {
            removeImage(holder.getAdapterPosition());
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

    private void showImageDialog(Uri imageUri, @NonNull ImageViewHolder holder) {
        ImageDialog dialog = new ImageDialog(holder.itemView.getContext(), imageUri);
        dialog.show();
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
