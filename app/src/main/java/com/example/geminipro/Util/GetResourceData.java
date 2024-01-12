package com.example.geminipro.Util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;

public class GetResourceData {

    private Context context;

    public GetResourceData(Context context){
        this.context = context;
    }

    public int getResource(String resourceName, String resourceType) {
        if (null != context){
            Resources resources = context.getResources();
            int resId = resources.getIdentifier(resourceName, resourceType, context.getPackageName());

            if (resId != 0) return resId;
        }
        return 0;
    }
}
