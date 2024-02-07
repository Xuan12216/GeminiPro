package com.example.geminipro.Util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.geminipro.Adapter.ImageAdapter;
import com.example.geminipro.Adapter.ImagePagerAdapter;
import com.example.geminipro.Fragment.ImageFragment;
import com.example.geminipro.R;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ImageDialog extends Dialog implements ImageFragment.DialogStatusListener {
    private Context context;
    private ViewPager2 viewPager;
    private ImagePagerAdapter imagePagerAdapter;
    private int position = 0;
    private List<Uri> imageUris = new ArrayList<>();
    private ImageAdapter.ImageViewHolder holder;
    //=================

    public ImageDialog(@NonNull Context context, List<Uri> imageUris, int position) {
        super(context, android.R.style.Theme_NoTitleBar);
        this.position = position;
        this.context = context;
        this.imageUris = imageUris;
    }

    public ImageDialog(@NonNull Context context, List<Uri> imageUris, int position, ImageAdapter.ImageViewHolder holder) {
        super(context, android.R.style.Theme_NoTitleBar);
        this.position = position;
        this.context = context;
        this.imageUris = imageUris;
        this.holder = holder;
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
        this.viewPager.setCurrentItem(position, false);

        openDialogAnim();
    }

    private void openDialogAnim() {

        viewPager.setPivotX(0);
        viewPager.setPivotY(0);
        viewPager.setScaleX(0.15f);
        viewPager.setScaleY(0.15f);

        if (null == holder){

            ObjectAnimator.ofFloat(viewPager, "scaleX", 1f).start();
            ObjectAnimator.ofFloat(viewPager, "scaleY", 1f).start();
        }
        else {
            // 获取点击视图的位置信息
            int[] location = new int[2];
            holder.binding.imageView.getLocationOnScreen(location);
            int startX = location[0];
            int startY = location[1];

            viewPager.setX(startX);
            viewPager.setY(startY - 100);

            int endX = (viewPager.getRootView().getWidth() - viewPager.getWidth()) / 2;
            int endY = (viewPager.getRootView().getHeight() - viewPager.getHeight()) / 2;

            // 创建并执行动画
            ObjectAnimator xAnimator = ObjectAnimator.ofFloat(viewPager, View.X, startX, endX);
            ObjectAnimator yAnimator = ObjectAnimator.ofFloat(viewPager, View.Y, startY - 100, endY);
            ObjectAnimator.ofFloat(viewPager, "scaleX", 1f).start();
            ObjectAnimator.ofFloat(viewPager, "scaleY", 1f).start();

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(xAnimator, yAnimator);
            animatorSet.setDuration(280); // 设置动画持续时间
            animatorSet.start();
        }
    }

    @Override
    public void dismiss() {

        if (null == holder){
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
        else {
            int[] location = new int[2];
            holder.binding.imageView.getLocationOnScreen(location);
            int endX = location[0];
            int endY = location[1];

            int startX = (viewPager.getRootView().getWidth() - viewPager.getWidth()) / 2;
            int startY = (viewPager.getRootView().getHeight() - viewPager.getHeight()) / 2;

            viewPager.setX(startX);
            viewPager.setY(startY);

            // 创建并执行动画
            ObjectAnimator xAnimator = ObjectAnimator.ofFloat(viewPager, View.X, startX, endX);
            ObjectAnimator yAnimator = ObjectAnimator.ofFloat(viewPager, View.Y, startY - 100, endY);
            ObjectAnimator.ofFloat(viewPager, "scaleX", 0.15f).start();
            ObjectAnimator.ofFloat(viewPager, "scaleY", 0.15f).start();

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(xAnimator, yAnimator);
            animatorSet.setDuration(280); // 设置动画持续时间
            animatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    ImageDialog.super.dismiss();

                }
            });

            animatorSet.start();
        }
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
