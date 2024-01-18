package com.example.geminipro.Page.SettingPage;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.TypedValue;
import android.view.View;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.example.geminipro.Model.GenerativeModelManager;
import com.example.geminipro.R;
import com.example.geminipro.Util.GeminiContentBuilder;
import com.example.geminipro.Util.Secure.SecuritySharedPreference;
import com.example.geminipro.databinding.SettingApiKeyBinding;

public class SettingApi {

    private Context context;
    private Activity activity;
    private SecuritySharedPreference preferences;

    public SettingApi(Context context, Activity activity){
        this.context = context;
        this.activity = activity;
        preferences = new SecuritySharedPreference(context, "gemini_private_api_prefs", Context.MODE_PRIVATE);
    }

    public SettingApiKeyBinding startPage(){
        SettingApiKeyBinding binding;
        binding = SettingApiKeyBinding.inflate(activity.getLayoutInflater());

        setData(binding);
        setListener(binding);
        return binding;
    }
    //setData=================================================
    private void setData(SettingApiKeyBinding binding) {
        String statusTrue = context.getResources().getString(R.string.status_true);
        String statusFalse = context.getResources().getString(R.string.status_false);
        String oriString = context.getResources().getString(R.string.get_api_hint);

        if (!preferences.contains("api_key")) binding.textViewStatus.setText(statusFalse);
        else binding.textViewStatus.setText(statusTrue);

        SpannableString spannableString = new SpannableString(oriString);

        findStringAndSetOnClickFunc(spannableString, oriString.contains("這裏") ? "這裏" : "here");
        binding.gotoUrl.setText(spannableString);
        binding.gotoUrl.setMovementMethod(LinkMovementMethod.getInstance());

        String[] title = context.getResources().getStringArray(R.array.settingsItem);
        binding.textViewTitle.setText(title[3]);
    }
    //setListener=================================================
    private void setListener(SettingApiKeyBinding binding) {

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.finish();
            }
        });
        binding.btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String api = binding.textInputEditText.getText().toString();
                String toastSuccess = context.getResources().getString(R.string.successfully_toast);
                if (!api.isEmpty()){
                    SecuritySharedPreference.Editor editor = preferences.edit();
                    editor.putString("api_key", api);
                    editor.apply();

                    Toast.makeText(context, toastSuccess, Toast.LENGTH_SHORT ).show();
                    activity.finish();

                    GenerativeModelManager.initializeGenerativeModel(context);
                    GeminiContentBuilder.resetChatNormal();
                }
                else Toast.makeText(context, R.string.editText_empty, Toast.LENGTH_SHORT ).show();
            }
        });
    }
    //findStringAndSetOnClickFunc=================================================
    private void findStringAndSetOnClickFunc(SpannableString spannableString, String targetString) {
        int indexStart = -1;

        while ((indexStart = spannableString.toString().indexOf(targetString, indexStart + 1)) != -1) {
            int indexEnd = indexStart + targetString.length();

            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    openUrl(context.getResources().getString(R.string.get_api_key_url));
                }

                @Override
                public void updateDrawState(TextPaint ds) {
                    ds.setUnderlineText(true);
                }
            };

            spannableString.setSpan(clickableSpan, indexStart, indexEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }
    //openUrl=================================================
    private void openUrl(String url) {
        Uri webpage = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);

        try {
            if (activity != null) {
                activity.startActivity(intent);
            }
        }
        catch (ActivityNotFoundException e) {}
    }
}
