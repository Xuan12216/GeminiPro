package com.example.geminipro.Util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.example.geminipro.Adapter.ImagePagerAdapter;
import com.example.geminipro.Fragment.ImageFragment;
import com.example.geminipro.R;
import java.util.ArrayList;
import java.util.List;

public class ImageDialog extends Dialog implements ImageFragment.DialogStatusListener {
    private Context context;
    private ViewPager2 viewPager;
    private ImagePagerAdapter imagePagerAdapter;
    private int position = 0;
    private List<Uri> imageUris = new ArrayList<>();
    //=================

    public ImageDialog(@NonNull Context context, List<Uri> imageUris, int position) {
        super(context, android.R.style.Theme_NoTitleBar);
        this.position = position;
        this.context = context;
        this.imageUris = imageUris;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        setContentView(R.layout.image_dialog);

        ImageView imageViewClose = findViewById(R.id.close_dialog);
        this.viewPager = findViewById(R.id.viewPager);

        imageViewClose.setOnClickListener(v -> dismiss());

        this.imagePagerAdapter = new ImagePagerAdapter((FragmentActivity) context, imageUris, this);
        this.viewPager.setAdapter(imagePagerAdapter);

        // Set the current item in ViewPager2
        this.viewPager.setCurrentItem(position, true);

        openDialogAnim();
    }

    private void openDialogAnim() {
        viewPager.setScaleX(0.5f);
        viewPager.setScaleY(0.5f);
        ObjectAnimator.ofFloat(viewPager, "scaleX", 1f).start();
        ObjectAnimator.ofFloat(viewPager, "scaleY", 1f).start();
    }

    @Override
    public void dismiss() {

        ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(viewPager, "scaleX", 0.5f);
        ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(viewPager, "scaleY", 0.5f);
        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(viewPager, "alpha", 1f, 0f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleXAnimator, scaleYAnimator, alphaAnimator);
        animatorSet.setDuration(200);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                ImageDialog.super.dismiss();

            }
        });

        animatorSet.start();
    }

    @Override
    public void DialogDismiss(String status) {

        switch (status){
            case "dismiss":
                dismiss();
                break;
            case "no_scale":
                viewPager.setUserInputEnabled(true);
                break;
            case "yes_scale":
                viewPager.setUserInputEnabled(false);
                break;
        }
    }
}
