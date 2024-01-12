package com.example.geminipro.Util;

import android.content.Context;
import android.util.DisplayMetrics;

public class Utils {

    public static int convertDpToPixel(float dp, Context context){
        int px = (int) (dp * getDensity(context));
        return px;
    }

    private static float getDensity(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return metrics.density;
    }

    public static int dp2px(Context mContext, float dp) {
        return (int) (mContext.getResources().getDisplayMetrics().density * dp + 0.5f);
    }
}
