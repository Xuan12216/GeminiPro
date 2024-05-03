package com.example.geminipro.Util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.example.geminipro.Adapter.ImagePagerAdapter;
import com.example.geminipro.Fragment.ImageFragment;
import com.example.geminipro.R;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ImageDialog extends Dialog implements ImageFragment.DialogStatusListener {
    private final Context context;
    private ViewPager2 viewPager;
    private int position = 0;
    private List<Uri> imageUris = new ArrayList<>();
    private HashMap<Integer, ImageView> imageViews = new HashMap<>();
    private AnimatorSet currentAnimator = null;
    private final int shortAnimationDuration = 300;
    private final float startScale = 0.2f;

    //=================

    public ImageDialog(@NonNull Context context, List<Uri> imageUris, int position) {
        super(context, android.R.style.Theme_NoTitleBar);
        this.position = position;
        this.context = context;
        this.imageUris = imageUris;
    }

    public ImageDialog(@NonNull Context context, List<Uri> imageUris, int position, HashMap<Integer,
            ImageView> imageViews) {
        super(context, android.R.style.Theme_NoTitleBar);
        this.position = position;
        this.context = context;
        this.imageUris = imageUris;
        this.imageViews = imageViews;
    }
    //====================================================
    @Override
    protected void onCreate(Bundle savedInstanceState){
        setContentView(R.layout.image_dialog);

        ImageView imageViewClose = findViewById(R.id.close_dialog);
        this.viewPager = findViewById(R.id.viewPager);

        imageViewClose.setOnClickListener(v -> dismiss());

        ImagePagerAdapter imagePagerAdapter = new ImagePagerAdapter((FragmentActivity) context , imageUris, this);
        this.viewPager.setAdapter(imagePagerAdapter);

        // Set the current item in ViewPager2
        this.viewPager.setCurrentItem(position, false);

        openDialogAnim();
    }
    //====================================================
    private void openDialogAnim() {

        if (imageViews.size() == 0){
            viewPager.setScaleX(0.2f);
            viewPager.setScaleY(0.2f);
            ObjectAnimator.ofFloat(viewPager, "scaleX", 1f).start();
            ObjectAnimator.ofFloat(viewPager, "scaleY", 1f).start();
        }
        else if (position >= 0 && position < imageViews.size()){
            ImageView imageView = imageViews.get(position);

            if (imageView != null){
                viewPager.setPivotX(0);
                viewPager.setPivotY(0);

                final Rect startBounds = new Rect();
                final Rect finalBounds = new Rect();

                imageView.getGlobalVisibleRect(startBounds);
                if (currentAnimator != null) {
                    currentAnimator.cancel();
                }

                AnimatorSet set = new AnimatorSet();
                set.play(ObjectAnimator.ofFloat(viewPager, View.X, startBounds.left, finalBounds.left))
                        .with(ObjectAnimator.ofFloat(viewPager, View.Y, startBounds.top, finalBounds.top))
                        .with(ObjectAnimator.ofFloat(viewPager, View.SCALE_X, startScale, 1f))
                        .with(ObjectAnimator.ofFloat(viewPager, View.SCALE_Y, startScale, 1f));
                set.setDuration(shortAnimationDuration);
                set.setInterpolator(new DecelerateInterpolator());
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        currentAnimator = null;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        currentAnimator = null;
                    }
                });
                set.start();
                currentAnimator = set;
            }
            else {
                viewPager.setScaleX(1f);
                viewPager.setScaleY(1f);
            }
        }
    }
    //====================================================
    @Override
    public void dismiss() {

        if (imageViews.size() == 0){
            ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(viewPager, "scaleX", 0.5f);
            ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(viewPager, "scaleY", 0.5f);
            ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(viewPager, "alpha", 1f, 0f);

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(scaleXAnimator, scaleYAnimator, alphaAnimator);
            animatorSet.setDuration(shortAnimationDuration);
            animatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    ImageDialog.super.dismiss();
                }
            });

            animatorSet.start();
        }
        else if (viewPager.getCurrentItem() >= 0 && viewPager.getCurrentItem() < imageViews.size()){
            ImageView imageView = imageViews.get(viewPager.getCurrentItem());

            if (imageView != null){
                if (currentAnimator != null) {
                    currentAnimator.cancel();
                }

                final Rect bounds = new Rect();
                imageView.getGlobalVisibleRect(bounds);

                if (bounds.left == bounds.top){
                    viewPager.setPivotX(viewPager.getMeasuredWidth() / 2f);
                    viewPager.setPivotY(viewPager.getMeasuredHeight() / 2f);
                }

                AnimatorSet set = new AnimatorSet();
                set.play(ObjectAnimator
                                .ofFloat(viewPager, View.X, bounds.left))
                        .with(ObjectAnimator
                                .ofFloat(viewPager, View.Y, bounds.top - ((bounds.bottom - bounds.top) / 2f)))
                        .with(ObjectAnimator
                                .ofFloat(viewPager, View.SCALE_X, startScale))
                        .with(ObjectAnimator
                                .ofFloat(viewPager, View.SCALE_Y, startScale));
                set.setDuration(shortAnimationDuration);
                set.setInterpolator(new DecelerateInterpolator());
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        currentAnimator = null;
                        ImageDialog.super.dismiss();
                    }
                });
                set.start();
                currentAnimator = set;
            }
        }
        else ImageDialog.super.dismiss();
    }
    //====================================================
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
