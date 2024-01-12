package com.example.geminipro.Util;

import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.example.geminipro.R;

public class ImageDialog extends Dialog {

    private Uri imageUri;

    public ImageDialog(@NonNull Context context, Uri imageUri) {
        super(context, android.R.style.Theme_NoTitleBar);
        this.imageUri = imageUri;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.image_dialog);

        ImageView imageView = findViewById(R.id.imageView_dialog);
        ImageView imageViewClose = findViewById(R.id.close_dialog);
        Glide.with(getContext().getApplicationContext())
                .load(imageUri)
                .into(imageView);

        imageView.setOnClickListener(v -> dismiss());
        imageViewClose.setOnClickListener(v -> dismiss());
    }
}
