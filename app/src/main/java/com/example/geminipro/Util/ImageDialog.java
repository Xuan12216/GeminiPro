package com.example.geminipro.Util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
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

        if (null == holder){
            viewPager.setScaleX(0.5f);
            viewPager.setScaleY(0.5f);
            ObjectAnimator.ofFloat(viewPager, "scaleX", 1f).start();
            ObjectAnimator.ofFloat(viewPager, "scaleY", 1f).start();
        }
        else {
            int[] location = new int[2];
            holder.binding.imageView.getLocationOnScreen(location);
            int startX = location[0];
            int startY = location[1];

            // 创建并显示动画视图
            ImageView animImageView = new ImageView(holder.itemView.getContext());
            animImageView.setImageDrawable(holder.binding.imageView.getDrawable());
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    holder.binding.imageView.getWidth(), holder.binding.imageView.getHeight());
            params.leftMargin = startX;
            params.topMargin = startY;
            ((ViewGroup) holder.itemView.getRootView()).addView(animImageView, params);

            // 计算目标位置
            int endX = (holder.itemView.getRootView().getWidth() - holder.binding.imageView.getWidth()) / 2; // 水平居中
            int endY = (holder.itemView.getRootView().getHeight() - holder.binding.imageView.getHeight()) / 2; // 垂直居中

            // 创建并执行动画
            ObjectAnimator xAnimator = ObjectAnimator.ofFloat(animImageView, View.X, startX, endX);
            ObjectAnimator yAnimator = ObjectAnimator.ofFloat(animImageView, View.Y, startY, endY);
            ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(animImageView, View.SCALE_X, 1f, (float) holder.itemView.getRootView().getWidth() / holder.binding.imageView.getWidth());
            ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(animImageView, View.SCALE_Y, 1f, (float) holder.itemView.getRootView().getHeight() / holder.binding.imageView.getHeight());

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(xAnimator, yAnimator, scaleXAnimator, scaleYAnimator);
            animatorSet.setDuration(300); // 设置动画持续时间
            animatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    // 动画结束后显示图像对话框
                    ImageDialog dialog = new ImageDialog(holder.itemView.getContext(), imageUris, position);
                    dialog.show();
                    // 移除动画视图
                    ((ViewGroup) animImageView.getParent()).removeView(animImageView);
                }
            });
            animatorSet.start();
        }
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
