package com.example.geminipro.Page.SettingPage;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.View;
import androidx.activity.ComponentActivity;
import com.bumptech.glide.Glide;
import com.example.geminipro.Util.PickImageFunc;
import com.example.geminipro.databinding.SettingNameBinding;

public class SettingName {
    private Activity activity;
    private PickImageFunc pickImageFunc;
    private Context context;
    private SharedPreferences preferences;

    public SettingName(Activity activity, Context context){
        this.context = context;
        this.activity = activity;
        pickImageFunc = new PickImageFunc((ComponentActivity) activity, context);
        preferences = context.getSharedPreferences("your_private_prefs", Context.MODE_PRIVATE);
    }

    public SettingNameBinding startRunPage(){
        SettingNameBinding binding;
        binding = SettingNameBinding.inflate(activity.getLayoutInflater());

        setData(binding);
        setListener(binding);
        return binding;
    }
    //set data================================================
    private void setData(SettingNameBinding binding){
        String storedImagePath = preferences.getString("userImage", "");
        String geminiName = preferences.getString("geminiName", "");
        String userName = preferences.getString("userName", "");

        if (!storedImagePath.isEmpty()){
            Glide.with(context)
                    .load(storedImagePath)
                    .into(binding.imageViewSetting);
        }

        if (!geminiName.isEmpty()) binding.textViewGemini.setText("Gemini Name : "+geminiName);
        if (!userName.isEmpty()) binding.textViewName.setText("Your Name : "+userName);
    }
    //listener================================================
    private void setListener(SettingNameBinding binding) {
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {activity.finish();}
        });

        binding.addImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImageFunc.startPickImage(new PickImageFunc.onImageResultCallback() {
                    @Override
                    public void onResult(Uri compressedUri) {
                        Glide.with(context)
                                .load(compressedUri)
                                .into(binding.imageViewSetting);

                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("userImage", compressedUri.toString());
                        editor.apply();
                    }
                });
            }
        });

        binding.textInputLayout.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = binding.textInputEditText.getText().toString();
                binding.textInputEditText.setText("");
                if (text.length() > 0){
                    binding.textViewName.setText("Your Name : "+ text);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("userName", text);
                    editor.apply();
                }
            }
        });

        binding.textInputLayoutGemini.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = binding.textInputEditTextGemini.getText().toString();
                binding.textInputEditTextGemini.setText("");
                if (text.length() > 0){
                    binding.textViewGemini.setText("Gemini Name : "+ text);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("geminiName", text);
                    editor.apply();
                }
            }
        });
    }
}
