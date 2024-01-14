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
import com.example.geminipro.Util.ImageDialog;
import com.example.geminipro.Util.PickImageFunc;
import com.example.geminipro.databinding.SettingNameBinding;

public class SettingName {
    private Activity activity;
    private PickImageFunc pickImageFunc;
    private Context context;
    private SharedPreferences preferences;
    private String pictureSave = "";

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
            pictureSave = storedImagePath;
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

                        pictureSave = compressedUri.toString();
                    }
                });
            }
        });

        binding.imageViewSetting.setOnLongClickListener(view -> {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            if (!pictureSave.isEmpty()){
                Uri uri = Uri.parse(pictureSave);
                ImageDialog dialog = new ImageDialog(activity, uri);
                dialog.show();
            }
            return true;
        });

        binding.acceptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String textUser = binding.textInputEditText.getText().toString();
                String textGemini = binding.textInputEditTextGemini.getText().toString();

                binding.textInputEditText.setText("");
                binding.textInputEditTextGemini.setText("");

                SharedPreferences.Editor editor = preferences.edit();

                if (!textUser.isEmpty()) editor.putString("userName", textUser);
                if (!textGemini.isEmpty()) editor.putString("geminiName", textGemini);
                if (!pictureSave.isEmpty()) editor.putString("userImage", pictureSave);

                editor.apply();

                Toast.makeText(context,"修改成功！",Toast.LENGTH_SHORT).show();
                activity.finish();
            }
        });
    }
}
