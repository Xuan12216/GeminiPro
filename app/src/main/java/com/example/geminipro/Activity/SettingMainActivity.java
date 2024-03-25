package com.example.geminipro.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import com.example.geminipro.Page.SettingPage.SettingApi;
import com.example.geminipro.Page.SettingPage.SettingName;
import com.example.geminipro.Page.SettingPage.SettingParameter;
import com.example.geminipro.Page.SettingPage.SettingSafe;
import com.example.geminipro.databinding.SettingApiKeyBinding;
import com.example.geminipro.databinding.SettingNameBinding;
import com.example.geminipro.databinding.SettingParameterBinding;

public class SettingMainActivity extends AppCompatActivity {
    private LinearLayout linearLayout;
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

            if ("0".equals(text)) initName();
            else if ("1".equals(text)) initParameter();
            else if ("2".equals(text)) initSafe();
            else if ("3".equals(text)) initSetApi();
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
}
