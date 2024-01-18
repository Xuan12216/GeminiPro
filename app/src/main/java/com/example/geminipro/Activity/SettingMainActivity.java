package com.example.geminipro.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.geminipro.BuildConfig;
import com.example.geminipro.Page.SettingPage.SettingApi;
import com.example.geminipro.Page.SettingPage.SettingName;
import com.example.geminipro.Page.SettingPage.SettingParameter;
import com.example.geminipro.Page.SettingPage.SettingSafe;
import com.example.geminipro.R;
import com.example.geminipro.Util.Utils;
import com.example.geminipro.databinding.SettingApiKeyBinding;
import com.example.geminipro.databinding.SettingNameBinding;
import com.example.geminipro.databinding.SettingParameterBinding;

public class SettingMainActivity extends AppCompatActivity {
    private LinearLayout linearLayout;
    private int colorPrimary,colorSecondary;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = getApplicationContext();
        initLinearLayout();

        setContentView(linearLayout);

        Intent intent = getIntent();
        if (intent.hasExtra("id")){
            String text = intent.getStringExtra("id");

            if ("設定名字，圖片".equals(text) || "Set name, picture".equals(text)) initName();
            else if ("設定模型參數".equals(text) || "Set model parameters".equals(text)) initParameter();
            else if ("設定安全參數".equals(text) || "Set security parameters".equals(text)) initSafe();
            else if ("設定apiKey".equals(text) || "Set apiKey".equals(text)) initSetApi();
        }
    }

    private void initSetApi() {
        SettingApi api = new SettingApi(context, this);
        SettingApiKeyBinding binding = api.startPage();
        setContentView(binding.getRoot());
    }

    private void initSafe() {
        SettingSafe safe = new SettingSafe(this, context);
        SettingParameterBinding binding = safe.startRunPage();
        setContentView(binding.getRoot());
    }

    private void initParameter() {
        SettingParameter parameter = new SettingParameter(this, context);
        SettingParameterBinding binding = parameter.startRunPage();
        setContentView(binding.getRoot());
    }

    private void initName() {
        SettingName name = new SettingName(this, context);
        SettingNameBinding binding = name.startRunPage();
        setContentView(binding.getRoot());
    }

    private void initLinearLayout(){
        int[] attrs = {com.google.android.material.R.attr.colorPrimary, com.google.android.material.R.attr.colorSecondary};
        TypedArray typedArray = obtainStyledAttributes(attrs);
        colorPrimary = typedArray.getColor(0, 0);//light : black, dark : white
        colorSecondary = typedArray.getColor(1, 0);//light : white, dark : black
        typedArray.recycle();

        linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        linearLayout.setLayoutParams(layoutParams);
        linearLayout.setBackgroundColor(colorSecondary);
        linearLayout.setGravity(Gravity.CENTER);
    }
}
