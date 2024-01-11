package com.example.geminipro.Util;

import android.content.Context;
import android.net.Uri;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;

public class PickImageFunc {
    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;
    private ComponentActivity activity;
    private ImageResize imageResize;
    private Context context;
    private PickImageFunc.onImageResultCallback callback;

    public PickImageFunc(ComponentActivity activity, Context context){
        this.activity = activity;
        this.context = context;
        if (null != activity && null != context){
            imageResize = new ImageResize(context);
            pickMedia = activity.registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), this::saveImage);
        }
    }

    public void startPickImage(onImageResultCallback callback){
        if (null != activity && null != context){
            this.callback = callback;
            ActivityResultContracts.PickVisualMedia.VisualMediaType mediaType = (ActivityResultContracts.PickVisualMedia.VisualMediaType) ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE;
            PickVisualMediaRequest request = new PickVisualMediaRequest.Builder()
                    .setMediaType(mediaType)
                    .build();
            pickMedia.launch(request);
        }
    }

    private void saveImage(Uri imageUri){
        if (imageUri != null) {
            imageResize.imgResize(imageUri, new ImageResize.ImageResizeCallback() {
                @Override
                public void onImageResized(Uri compressedUri) {
                    callback.onResult(compressedUri);
                }
            });
        }
    }

    public interface onImageResultCallback{
        void onResult(Uri compressedUri);
    }
}
