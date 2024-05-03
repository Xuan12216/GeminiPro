package com.example.geminipro.Model;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.example.geminipro.Activity.SettingMainActivity;
import com.example.geminipro.BuildConfig;
import com.example.geminipro.R;
import com.example.geminipro.Util.Secure.SecuritySharedPreference;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.BlockThreshold;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerationConfig;
import com.google.ai.client.generativeai.type.HarmCategory;
import com.google.ai.client.generativeai.type.SafetySetting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GenerativeModelManager {
    private static GenerativeModelFutures model;
    private static GenerativeModelFutures modelVision;
    private static Content userContent, modelContent;
    private static GenerationConfig generationConfig;
    private static List<SafetySetting> safetyList;
    //modelGenerateConfig================
    private static Float temperature = 0.9f;
    private static Float topP = 0.95f;
    private static Integer topK = 3;
    private static Integer maxOutputToken = 2048;
    private static Integer candidateCount = 1;
    private static List<String> stopSequences = new ArrayList<>();
    private static SharedPreferences preferences;
    private static SecuritySharedPreference pres;
    private static String[] safeList = new String[4];

    public static void initializeGenerativeModel(Context context) {

        preferences = context.getSharedPreferences("gemini_private_prefs", Context.MODE_PRIVATE);
        pres = new SecuritySharedPreference(context, "gemini_private_api_prefs", Context.MODE_PRIVATE);
        resetModel();
        generateConfig();
        setSafetySetting();
        // 初始化 Generative Model
        String api = pres.getString("api_key", "");
        GenerativeModel gm1 = new GenerativeModel("gemini-1.5-pro-latest", api, generationConfig, safetyList);
        GenerativeModel gm2 = new GenerativeModel("gemini-pro-vision", api, generationConfig, safetyList);

        // 使用 GenerativeModelFutures 创建 GenerativeModelFutures 实例
        model = GenerativeModelFutures.from(gm1);
        modelVision = GenerativeModelFutures.from(gm2);

        createHistoryData();
    }

    public static void checkApiKey(Context context) {
        SecuritySharedPreference pres = new SecuritySharedPreference(context, "gemini_private_api_prefs", Context.MODE_PRIVATE);
        String api = pres.getString("api_key", "");
        if (api.isEmpty()) {
            Intent intent = new Intent(context, SettingMainActivity.class);
            intent.putExtra("id", "3");
            context.startActivity(intent);
        }
    }

    private static void setSafetySetting() {
        safeList[0] = preferences.getString("harassment", "0");
        safeList[1] = preferences.getString("hate_speech", "0");
        safeList[2] = preferences.getString("sexually_explicit", "0");
        safeList[3] = preferences.getString("dangerous_content", "0");

        SafetySetting harassmentSafety = new SafetySetting(HarmCategory.HARASSMENT, getBlockThreshold(safeList[0]));
        SafetySetting hateSpeechSafety = new SafetySetting(HarmCategory.HATE_SPEECH, getBlockThreshold(safeList[1]));
        SafetySetting sexuallyExplicitSafety = new SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, getBlockThreshold(safeList[2]));
        SafetySetting dangerousContentSafety = new SafetySetting(HarmCategory.DANGEROUS_CONTENT, getBlockThreshold(safeList[3]));
        safetyList = Arrays.asList(harassmentSafety, hateSpeechSafety, sexuallyExplicitSafety, dangerousContentSafety);
    }

    private static BlockThreshold getBlockThreshold(String s) {
        switch (s){
            case "0":
                return BlockThreshold.NONE;
            case "1":
                return BlockThreshold.ONLY_HIGH;
            case "2":
                return BlockThreshold.MEDIUM_AND_ABOVE;
            case "3":
                return BlockThreshold.LOW_AND_ABOVE;
            default:
                return BlockThreshold.UNSPECIFIED;
        }
    }

    private static void generateConfig() {

        temperature = preferences.getFloat("temperature", temperature);
        topP = preferences.getFloat("topP", topP);
        topK = preferences.getInt("topK", topK);
        maxOutputToken = preferences.getInt("maxOutputToken", maxOutputToken);
        String stp = preferences.getString("stop", "");
        candidateCount = preferences.getInt("candidateCount", candidateCount);

        stopSequences.clear();
        stopSequences.add(stp);

        GenerationConfig.Builder configBuilder = new GenerationConfig.Builder();
        configBuilder.temperature = temperature;
        configBuilder.topK = topK;
        configBuilder.topP = topP;
        configBuilder.maxOutputTokens = maxOutputToken;
        configBuilder.stopSequences = new ArrayList<>(stopSequences);
        configBuilder.candidateCount = 1;
        generationConfig = configBuilder.build();
    }

    private static void createHistoryData() {
        Content.Builder userContentBuilder = new Content.Builder();
        userContentBuilder.setRole("user");
        userContentBuilder.addText("Hello.");
        userContent = userContentBuilder.build();

        Content.Builder modelContentBuilder = new Content.Builder();
        modelContentBuilder.setRole("model");
        modelContentBuilder.addText("Great to meet you.");
        modelContent = modelContentBuilder.build();
    }

    private static void resetModel(){
        model = null;
        modelVision = null;
        userContent = null;
        modelContent = null;
        generationConfig = null;
        safetyList = null;
    }
    //===============================================
    
    public static Content getUserContent() {
        return userContent;
    }

    public static Content getModelContent() {
        return modelContent;
    }

    public static GenerativeModelFutures getGenerativeModel() {
        return model;
    }

    public static GenerativeModelFutures getGenerativeModelVision() {
        return modelVision;
    }

    public static String[] getSafetyList() {
        return safeList;
    }

    public static Float getTemperature() {
        return temperature;
    }

    public static Float getTopP() {
        return topP;
    }

    public static Integer getTopK() {
        return topK;
    }

    public static Integer getMaxOutputToken() {
        return maxOutputToken;
    }

    public static Integer getCandidateCount() {
        return candidateCount;
    }
    public static List<String> getStopSequences() {
        return stopSequences;
    }
}
