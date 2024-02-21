package com.example.geminipro.Util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

import com.example.geminipro.Database.User;
import com.example.geminipro.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CustomDialog extends Dialog {
    private final Context context;
    private List<User> title = new ArrayList<>();
    private final onEditSuccess callback;
    private boolean isForDelete = false;

    //=====

    private CardView cardView;
    private TextView buttonAccept, buttonCancel, textTitle;
    private TextInputEditText editText;
    private TextInputLayout renameEdittextLayout;
    private View rootView;

    public CustomDialog(@NonNull Context context, List<User> title, boolean isForDelete,onEditSuccess callback) {
        super(context, android.R.style.Animation_Dialog);
        this.context = context;
        this.title = title;
        this.callback = callback;
        this.isForDelete = isForDelete;
    }

    //====================================================
    @Override
    protected void onCreate(Bundle savedInstanceState){
        setContentView(R.layout.rename_dialog);
        Objects.requireNonNull(getWindow()).setBackgroundDrawableResource(android.R.color.transparent);

        buttonAccept = findViewById(R.id.button_rename);
        buttonCancel = findViewById(R.id.button_cancel);
        editText = findViewById(R.id.renameEdittext);
        renameEdittextLayout = findViewById(R.id.renameEdittextLayout);
        textTitle = findViewById(R.id.textTitle);
        cardView = findViewById(R.id.renameCardView);
        rootView = getWindow().getDecorView().getRootView();

        init();
        setListener();
        openDialogAnim();
    }

    private void init(){
        if (isForDelete) {
            ViewGroup.LayoutParams layoutParams = renameEdittextLayout.getLayoutParams();
            layoutParams.height = Utils.convertDpToPixel(1, context);
            renameEdittextLayout.setLayoutParams(layoutParams);

            buttonAccept.setText(R.string.delete_accept);
            textTitle.setText(R.string.delete_title);
        }
    }

    private void setListener() {
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                rootView.getWindowVisibleDisplayFrame(r);
                int screenHeight = rootView.getHeight();

                // Calculate the height difference between the screen height and the visible display frame
                int heightDifference = screenHeight - (r.bottom - r.top);

                // If the height difference is more than 200 pixels, it's probably a keyboard
                if (heightDifference > 200) {
                    // Keyboard is showing, adjust the position of cardView
                    moveCardViewUp();
                } else {
                    // Keyboard is hidden, reset the position of cardView
                    resetCardViewPosition();
                }
            }
        });

        buttonAccept.setOnClickListener(v -> {
            if (isForDelete){
                Toast.makeText(context, R.string.rename_successful,Toast.LENGTH_SHORT).show();
                if (null != callback) callback.onSuccess("true");
                dismiss();
            }
            else {
                String rename = Objects.requireNonNull(editText.getText()).toString();
                boolean isSuccess = true;

                if (!rename.isEmpty()){
                    for (User user : title){
                        if (user.getTitle().equals(rename)){
                            Toast.makeText(context, R.string.rename_repeat,Toast.LENGTH_SHORT).show();
                            isSuccess = false;
                            break;
                        }
                    }
                }
                else {
                    isSuccess = false;
                    Toast.makeText(context, R.string.rename_error,Toast.LENGTH_SHORT).show();
                }

                if (isSuccess){
                    Toast.makeText(context, R.string.rename_successful,Toast.LENGTH_SHORT).show();
                    if (null != callback) callback.onSuccess(rename);
                    dismiss();
                }
            }
        });

        buttonCancel.setOnClickListener((view) -> dismiss());
    }

    private void moveCardViewUp() {
        // Calculate how much you want to move the cardView up
        float translationY = -150f; // Adjust this value according to your requirement

        // Animate the translation of cardView
        cardView.animate()
                .translationY(translationY)
                .setDuration(200)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    private void resetCardViewPosition() {
        // Reset the position of cardView
        cardView.animate()
                .translationY(0)
                .setDuration(200)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }


    private void openDialogAnim() {
        cardView.setScaleX(0.8f);
        cardView.setScaleY(0.8f);

        // 创建并启动 X 轴缩放动画
        ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(cardView, "scaleX", 1f);
        scaleXAnimator.setDuration(200); // 设置动画持续时间为 200 毫秒
        scaleXAnimator.start();

        // 创建并启动 Y 轴缩放动画
        ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(cardView, "scaleY", 1f);
        scaleYAnimator.setDuration(200); // 设置动画持续时间为 200 毫秒
        scaleYAnimator.start();
    }


    @Override
    public void dismiss() {
        ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(cardView, "scaleX", 0.8f);
        ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(cardView, "scaleY", 0.8f);
        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(cardView, "alpha", 1f, 0f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleXAnimator, scaleYAnimator, alphaAnimator);
        animatorSet.setDuration(200);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                CustomDialog.super.dismiss();
            }
        });

        animatorSet.start();
    }

    public interface onEditSuccess {
        void onSuccess(String rename);
    }
}