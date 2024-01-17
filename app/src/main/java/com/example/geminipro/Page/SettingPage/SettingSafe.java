package com.example.geminipro.Page.SettingPage;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.TypedValue;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.geminipro.Model.GenerativeModelManager;
import com.example.geminipro.R;
import com.example.geminipro.Util.GeminiContentBuilder;
import com.example.geminipro.databinding.SettingParameterBinding;
import com.google.android.material.slider.LabelFormatter;
import com.google.android.material.slider.Slider;

public class SettingSafe {
    private Activity activity;
    private Context context;
    private SharedPreferences preferences;
    private String[] contentUp = new String[4], contentDown = new String[5];
    private String[] safeList = new String[4], contentCurrent = new String[5];
    private int harassment = 0, hate_speech = 0, sexually_explicit = 0, dangerous_content = 0;

    public SettingSafe(Activity activity, Context context){
        this.context = context;
        this.activity = activity;
        preferences = context.getSharedPreferences("your_private_prefs", Context.MODE_PRIVATE);
        safeList = GenerativeModelManager.getSafetyList();
    }

    public SettingParameterBinding startRunPage(){
        SettingParameterBinding binding;
        binding = SettingParameterBinding.inflate(activity.getLayoutInflater());

        settingPage(binding);
        setData(binding);
        setListener(binding);
        return binding;
    }
    //settingPage================================================
    private void settingPage(SettingParameterBinding binding) {
        binding.stopSequences.setVisibility(View.GONE);
        binding.textInputLayout.setVisibility(View.GONE);
        binding.contentStop.setVisibility(View.GONE);
        binding.candidateCount.setVisibility(View.GONE);
        binding.sliderCandidateCOunt.setVisibility(View.GONE);
        binding.textCandidateCount.setVisibility(View.GONE);

        contentUp[0] = context.getResources().getString(R.string.harassment_content);
        contentUp[1] = context.getResources().getString(R.string.hate_speech_content);
        contentUp[2] = context.getResources().getString(R.string.sexually_explicit_content);
        contentUp[3] = context.getResources().getString(R.string.dangerous_content);

        contentDown[0] = context.getResources().getString(R.string.block_none);
        contentDown[1] = context.getResources().getString(R.string.block_only_hig);
        contentDown[2] = context.getResources().getString(R.string.block_medium_and_above);
        contentDown[3] = context.getResources().getString(R.string.block_low_and_above);
        contentDown[4] = context.getResources().getString(R.string.harm_block_threshold_unspecified);

        contentCurrent[0] = "NONE";
        contentCurrent[1] = "ONLY_HIGH";
        contentCurrent[2] = "MEDIUM_AND_ABOVE";
        contentCurrent[3] = "LOW_AND_ABOVE";
        contentCurrent[4] = "UNSPECIFIED";
    }
    //set data================================================
    private void setData(SettingParameterBinding binding){
        harassment = Integer.parseInt(safeList[0]);
        hate_speech = Integer.parseInt(safeList[1]);
        sexually_explicit = Integer.parseInt(safeList[2]);
        dangerous_content = Integer.parseInt(safeList[3]);

        float textSizeInSp = 16;
        binding.temperature.setText("HARASSMENT(騷擾)");
        binding.temperature.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeInSp);
        binding.contentTemperature.setText(contentUp[0] + "\n" + contentDown[harassment]);

        binding.topK.setText("HATE_SPEECH(仇恨言論)");
        binding.topK.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeInSp);
        binding.contentTopK.setText(contentUp[1] + "\n" + contentDown[hate_speech]);

        binding.topP.setText("SEXUALLY_EXPLICIT(煽情露骨內容)");
        binding.topP.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeInSp);
        binding.contentTopP.setText(contentUp[2] + "\n" + contentDown[sexually_explicit]);

        binding.maxOutToken.setText("DANGEROUS_CONTENT(危險)");
        binding.maxOutToken.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeInSp);
        binding.contentMaxOutputTokens.setText(contentUp[3] + "\n" + contentDown[dangerous_content]);

        LabelFormatter letterLabelFormatter = new LabelFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (value >= 0 && value <= 4) return contentCurrent[(int) value];
                else return "";
            }
        };

        binding.sliderTemperature.setValue(harassment);
        binding.sliderTemperature.setLabelFormatter(letterLabelFormatter);
        binding.sliderTemperature.setValueFrom(0);
        binding.sliderTemperature.setValueTo(4);
        binding.sliderTemperature.setStepSize(1);

        binding.sliderTopK.setValue(hate_speech);
        binding.sliderTopK.setLabelFormatter(letterLabelFormatter);
        binding.sliderTopK.setValueFrom(0);
        binding.sliderTopK.setValueTo(4);
        binding.sliderTopK.setStepSize(1);

        binding.sliderTopP.setValue(sexually_explicit);
        binding.sliderTopP.setLabelFormatter(letterLabelFormatter);
        binding.sliderTopP.setValueFrom(0);
        binding.sliderTopP.setValueTo(4);
        binding.sliderTopP.setStepSize(1);

        binding.sliderMaxOutTokken.setValue(dangerous_content);
        binding.sliderMaxOutTokken.setLabelFormatter(letterLabelFormatter);
        binding.sliderMaxOutTokken.setValueFrom(0);
        binding.sliderMaxOutTokken.setValueTo(4);
        binding.sliderMaxOutTokken.setStepSize(1);
    }
    //listener================================================
    private void setListener(SettingParameterBinding binding) {
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.finish();
            }
        });

        binding.sliderTemperature.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                binding.temperature.setText("HARASSMENT(騷擾)" + "\n" + contentCurrent[harassment] + " \u2192 " + contentCurrent[(int) value]);
                binding.contentTemperature.setText(contentUp[0] + "\n" + contentDown[(int) value]);
            }
        });

        binding.sliderTopP.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                binding.topP.setText("SEXUALLY_EXPLICIT(煽情露骨內容)" + "\n" + contentCurrent[hate_speech] + " \u2192 " + contentCurrent[(int) value]);
                binding.contentTopP.setText(contentUp[1] + "\n" + contentDown[(int) value]);
            }
        });

        binding.sliderTopK.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                binding.topK.setText("HATE_SPEECH(仇恨言論)" + "\n" + contentCurrent[sexually_explicit] + " \u2192 " + contentCurrent[(int) value]);
                binding.contentTopK.setText(contentUp[2] + "\n" + contentDown[(int) value]);
            }
        });

        binding.sliderMaxOutTokken.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                binding.maxOutToken.setText("DANGEROUS_CONTENT(危險)" + "\n" + contentCurrent[dangerous_content] + " \u2192 " + contentCurrent[(int) value]);
                binding.contentMaxOutputTokens.setText(contentUp[3] + "\n" + contentDown[(int) value]);
            }
        });

        binding.btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                harassment = (int) binding.sliderTemperature.getValue();
                hate_speech = (int) binding.sliderTopK.getValue();
                sexually_explicit = (int) binding.sliderTopP.getValue();
                dangerous_content = (int) binding.sliderMaxOutTokken.getValue();

                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("harassment", String.valueOf(harassment));
                editor.putString("hate_speech", String.valueOf(hate_speech));
                editor.putString("sexually_explicit", String.valueOf(sexually_explicit));
                editor.putString("dangerous_content", String.valueOf(dangerous_content));

                editor.apply();

                GenerativeModelManager.initializeGenerativeModel(context);
                GeminiContentBuilder.resetChatNormal();

                Toast.makeText(context,"模型修改成功！",Toast.LENGTH_SHORT).show();
                activity.finish();
            }
        });
    }
}
