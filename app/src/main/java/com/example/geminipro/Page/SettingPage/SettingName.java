package com.example.geminipro.Page.SettingPage;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import com.bumptech.glide.Glide;
import com.example.geminipro.R;
import com.example.geminipro.Util.ImageDialog;
import com.example.geminipro.Util.PickImageFunc;
import com.example.geminipro.databinding.SettingNameBinding;

import java.util.ArrayList;
import java.util.List;

public class SettingName {
    private Activity activity;
    private PickImageFunc pickImageFunc;
    private Context context;
    private SharedPreferences preferences;
    private String pictureSave = "";
    private String name = "", gemini_name = "", enter_name = "", enter_gemini_name = "";

    public SettingName(Activity activity, Context context){
        this.context = context;
        this.activity = activity;
        pickImageFunc = new PickImageFunc((ComponentActivity) activity, context);
        preferences = context.getSharedPreferences("gemini_private_prefs", Context.MODE_PRIVATE);
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

        name = context.getResources().getString(R.string.name);
        gemini_name = context.getResources().getString(R.string.gemini_name);
        enter_name = context.getResources().getString(R.string.enter_name);
        enter_gemini_name = context.getResources().getString(R.string.enter_gemini_name);

        if (!storedImagePath.isEmpty()){
            pictureSave = storedImagePath;
            Glide.with(context)
                    .load(storedImagePath)
                    .into(binding.imageViewSetting);
        }

        if (!geminiName.isEmpty()) binding.textViewGemini.setText(gemini_name + geminiName);
        else binding.textViewGemini.setText(gemini_name + "Gemini");

        if (!userName.isEmpty()) binding.textViewName.setText(name + userName);
        else binding.textViewName.setText(name + "User");

        String[] title = context.getResources().getStringArray(R.array.settingsItem);
        binding.textviewTitle.setText(title[0]);
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

                        pictureSave = compressedUri.toString();
                    }
                });
            }
        });

        binding.imageViewSetting.setOnClickListener(view -> {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            if (!pictureSave.isEmpty()){
                Uri uri = Uri.parse(pictureSave);
                List<Uri> list = new ArrayList<>();
                list.add(uri);

                ImageDialog dialog = new ImageDialog(activity, list, 0);
                dialog.show();
            }
        });

        binding.acceptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String textUser = binding.textInputEditText.getText().toString();
                String textGemini = binding.textInputEditTextGemini.getText().toString();
                String toast = context.getResources().getString(R.string.successfully_toast);

                binding.textInputEditText.setText("");
                binding.textInputEditTextGemini.setText("");

                SharedPreferences.Editor editor = preferences.edit();

                if (!textUser.isEmpty()) editor.putString("userName", textUser);
                if (!textGemini.isEmpty()) editor.putString("geminiName", textGemini);
                if (!pictureSave.isEmpty()) editor.putString("userImage", pictureSave);

                editor.apply();

                Toast.makeText(context,toast,Toast.LENGTH_SHORT).show();
                activity.finish();
            }
        });
    }
}
