package com.example.geminipro.Page.SettingPage;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.DragEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.geminipro.Model.GenerativeModelManager;
import com.example.geminipro.R;
import com.example.geminipro.Util.GeminiContentBuilder;
import com.example.geminipro.databinding.SettingNameBinding;
import com.example.geminipro.databinding.SettingParameterBinding;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.android.material.slider.Slider;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SettingParameter {
    private Activity activity;
    private Context context;
    private float temperature, topP;
    private int topK, maxOutputToken, candidateCount;
    private List<String> stop = new ArrayList<>();
    private SharedPreferences preferences;

    public SettingParameter(Activity activity, Context context){
        this.activity = activity;
        this.context = context;
        preferences = context.getSharedPreferences("gemini_private_prefs", Context.MODE_PRIVATE);
    }

    public SettingParameterBinding startRunPage(){
        SettingParameterBinding binding;
        binding = SettingParameterBinding.inflate(activity.getLayoutInflater());

        setData(binding);
        setListener(binding);
        return binding;
    }

    private void setData(SettingParameterBinding binding){
        temperature = GenerativeModelManager.getTemperature();
        topP = GenerativeModelManager.getTopP();
        topK = GenerativeModelManager.getTopK();
        maxOutputToken = GenerativeModelManager.getMaxOutputToken();
        stop = GenerativeModelManager.getStopSequences();
        candidateCount = GenerativeModelManager.getCandidateCount();

        binding.temperature.setText("Temperature : " + String.valueOf(temperature));
        if (temperature > 0) binding.sliderTemperature.setValue(temperature);

        binding.topP.setText("TopP : " + String.valueOf(topP));
        if (topP > 0) binding.sliderTopP.setValue(topP);

        binding.topK.setText("TopK : " + String.valueOf(topK));
        if (topK > 0) binding.sliderTopK.setValue(topK);

        binding.maxOutToken.setText("Max Output Tokens : " + String.valueOf(maxOutputToken));
        if (maxOutputToken > 0) binding.sliderMaxOutTokken.setValue(maxOutputToken);

        binding.candidateCount.setText("Candidate Count : " + String.valueOf(candidateCount));
        if (candidateCount > 0) binding.sliderCandidateCOunt.setValue(candidateCount);

        if (!stop.isEmpty()) binding.stopSequences.setText("Stop Sequences : " + stop.get(0));

        String[] title = context.getResources().getStringArray(R.array.settingsItem);
        binding.textviewTitle.setText(title[1]);
    }

    private void setListener(SettingParameterBinding binding){

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.finish();
            }
        });
        binding.sliderTemperature.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                binding.temperature.setText("Temperature : " + String.valueOf(temperature) + " \u2192 " + value);

            }
        });

        binding.sliderCandidateCOunt.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                binding.candidateCount.setText("Candidate Count : " + String.valueOf(candidateCount) + " \u2192 " + (int)value);
            }
        });

        binding.sliderTopP.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                binding.topP.setText("TopP : " + String.valueOf(topP) + " \u2192 " + value);
            }
        });

        binding.sliderTopK.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                binding.topK.setText("TopK : " + String.valueOf(topK) + " \u2192 " + (int)value);
            }
        });

        binding.sliderMaxOutTokken.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                binding.maxOutToken.setText("Max Output Tokens : " + String.valueOf(maxOutputToken) + " \u2192 " + (int)value);
            }
        });

        binding.stopEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.stopSequences.setText("Stop Sequences : " + (!stop.isEmpty() ? stop.get(0) : "") + " \u2192 " + charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                float temperature = binding.sliderTemperature.getValue();
                float topP = binding.sliderTopP.getValue();
                int topK = (int) binding.sliderTopK.getValue();
                int maxOutputToken = (int) binding.sliderMaxOutTokken.getValue();
                String stop = binding.stopEditText.getText().toString();
                int candidateCount = (int) binding.sliderCandidateCOunt.getValue();

                SharedPreferences.Editor editor = preferences.edit();
                editor.putFloat("temperature", temperature);
                editor.putFloat("topP", topP);
                editor.putInt("topK", topK);
                editor.putInt("maxOutputToken", maxOutputToken);
                editor.putString("stop", stop);
                editor.putInt("candidateCount", candidateCount);

                editor.apply();

                GenerativeModelManager.initializeGenerativeModel(context);
                GeminiContentBuilder.resetChatNormal();

                Toast.makeText(context,R.string.successfully_toast,Toast.LENGTH_SHORT).show();
                activity.finish();
            }
        });
    }
}
