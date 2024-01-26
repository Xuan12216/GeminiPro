package com.example.geminipro.Util;

import android.content.Context;
import android.net.Uri;
import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PickImageUsingCamera {
    private ActivityResultLauncher<Uri> takePicture;
    private ComponentActivity activity;
    private ImageResize imageResize;
    private Context context;
    private onImageResultCallback callback;

    private Uri photoUri;

    public PickImageUsingCamera(ComponentActivity activity, Context context){
        this.activity = activity;
        this.context = context;
        if (null != activity && null != context){
            imageResize = new ImageResize(context);
            takePicture = activity.registerForActivityResult(new ActivityResultContracts.TakePicture(), this::saveTakenImage);
        }
    }

    public void startPickImage(onImageResultCallback callback){
        if (null != activity && null != context){
            this.callback = callback;
            startTakePicture();
        }
    }

    private void startTakePicture() {
        try {
            File photoFile = createImageFile();
            if (photoFile != null) {
                photoUri = FileProvider.getUriForFile(context, "com.example.geminipro.fileprovider", photoFile);
                takePicture.launch(photoUri);
            }
        } catch (IOException ex) {
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(null);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    private void saveTakenImage(Boolean success) {
        if (success && null != photoUri) {
            imageResize.imgResize(photoUri, new ImageResize.ImageResizeCallback() {
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
