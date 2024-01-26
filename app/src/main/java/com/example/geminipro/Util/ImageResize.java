package com.example.geminipro.Util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.annotation.Nullable;

public class ImageResize {
    private Context context;

    public ImageResize(Context context) {
        this.context = context;
    }

    public void imgResize(Uri imageUri, ImageResizeCallback callback){
        if (null != imageUri && null != context){
            Glide.with(context)
                    .asBitmap()
                    .load(imageUri)
                    .override(500)
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            Uri compressedUri = compressAndSaveBitmap(resource);
                            callback.onImageResized(compressedUri);
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                            callback.onImageResized(imageUri);
                        }
                    });
        }
    }

    private Uri compressAndSaveBitmap(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream);
        // 这里是示例代码，实际情况可能需要根据具体需求调整
        File compressedFile = saveByteArrayToFile(byteArrayOutputStream.toByteArray());
        return Uri.fromFile(compressedFile);
    }

    private File saveByteArrayToFile(byte[] byteArray) {
        try {
            File tempFile = File.createTempFile("compressed_image", ".jpg");
            FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
            fileOutputStream.write(byteArray);
            fileOutputStream.close();
            return tempFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public interface ImageResizeCallback {
        void onImageResized(Uri compressedUri);
    }
}
