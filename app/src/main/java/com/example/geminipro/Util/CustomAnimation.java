package com.example.geminipro.Util;

import android.content.Context;
import android.view.View;
import android.view.animation.TranslateAnimation;

public class CustomAnimation {

    public CustomAnimation(Context context) {}

    public void swapAnimation(View view1, View view2) {
        int[] Location1 = new int[2], Location2 = new int[2];

        view1.getLocationOnScreen(Location1);
        view2.getLocationOnScreen(Location2);

        // 创建 TranslateAnimation 实例来实现移动动画
        TranslateAnimation anim1 = new TranslateAnimation(0, Location2[0] - Location1[0], 0, Location2[1] - Location1[1]);
        anim1.setDuration(200); // 设置动画持续时间

        TranslateAnimation anim2 = new TranslateAnimation(0, Location1[0] - Location2[0], 0, Location1[1] - Location2[1]);
        anim2.setDuration(200);

        // 给两个 Spinner 应用动画
        view1.startAnimation(anim1);
        view2.startAnimation(anim2);
    }
}
