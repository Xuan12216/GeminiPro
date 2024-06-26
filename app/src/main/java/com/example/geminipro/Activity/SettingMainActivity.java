package com.example.geminipro.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.example.geminipro.Page.MoreFuncPage.FuncTesting;
import com.example.geminipro.Page.MoreFuncPage.FuncTranslate;
import com.example.geminipro.Page.SettingPage.SettingApi;
import com.example.geminipro.Page.SettingPage.SettingName;
import com.example.geminipro.Page.SettingPage.SettingParameter;
import com.example.geminipro.Page.SettingPage.SettingSafe;
import com.example.geminipro.databinding.MoreFuncTranslateBinding;
import com.example.geminipro.databinding.SettingApiKeyBinding;
import com.example.geminipro.databinding.SettingNameBinding;
import com.example.geminipro.databinding.SettingParameterBinding;
import com.example.geminipro.databinding.MoreFuncTestingBinding;

import java.util.Objects;

public class SettingMainActivity extends AppCompatActivity {
    private LinearLayout linearLayout;
    private Context context;
    private String text;
    private FuncTranslate translate;
    private FuncTesting testing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;
        initLinearLayout();

        setContentView(linearLayout);

        Intent intent = getIntent();
        if (intent.hasExtra("id")){
            text = intent.getStringExtra("id");

            switch (Objects.requireNonNull(text)){
                case "0" :
                    initName();
                    break;
                case "1" :
                    initParameter();
                    break;
                case "2" :
                    initSafe();
                    break;
                case "3" :
                    initSetApi();
                    break;
                case "4" :
                    initFuncTranslate();
                    break;
                case "5" :
                    initFuncTesting();
                    break;
            }
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

    private void initFuncTranslate() {
        translate = new FuncTranslate(this, context,
                getSupportFragmentManager(), getLifecycle());
        MoreFuncTranslateBinding binding = translate.startRunPage();
        setContentView(binding.getRoot());
    }

    private void initFuncTesting() {
        testing = new FuncTesting(this, context);
        MoreFuncTestingBinding binding = testing.startRunPage();
        setContentView(binding.getRoot());
    }

    //======

    private void initLinearLayout(){
        TypedValue value = new TypedValue();
        getTheme().resolveAttribute(com.google.android.material.R.attr.colorSecondary, value, true);

        linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        linearLayout.setLayoutParams(layoutParams);
        linearLayout.setBackgroundColor(value.data);
        linearLayout.setGravity(Gravity.CENTER);
    }

    //=====
    @Override
    protected void onResume() {
        super.onResume();
        callOnResume();
    }

    //=====
    @Override
    protected void onPause() {
        super.onPause();
        callOnPause();
    }

    //=====
    @Override
    protected void onDestroy(){
        super.onDestroy();
        callOnDestroy();
    }

    //=====

    private void callOnResume() {
        switch (Objects.requireNonNull(text)){
            case "4" :
                if (null != translate) translate.onResume();
                break;
        }
    }

    private void callOnPause() {
        switch (Objects.requireNonNull(text)){
            case "4" :
                if (null != translate) translate.onPause();
                break;
        }
    }

    private void callOnDestroy() {
        switch (Objects.requireNonNull(text)){
            case "4" :
                if (null != translate) translate.onDestroy();
                break;
        }
    }
}
