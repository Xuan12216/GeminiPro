package com.example.geminipro.Model;

import android.content.Context;

import com.example.geminipro.BuildConfig;
import com.example.geminipro.R;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.BlockThreshold;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerationConfig;
import com.google.ai.client.generativeai.type.HarmCategory;
import com.google.ai.client.generativeai.type.SafetySetting;

import java.util.ArrayList;
import java.util.Arrays;
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
    private static List<String> stopSequences = Arrays.asList("");

    public static void initializeGenerativeModel() {

        generateConfig();
        setSafetySetting();
        // 初始化 Generative Model
        GenerativeModel gm1 = new GenerativeModel("gemini-pro", BuildConfig.apiKey, generationConfig, safetyList);
        GenerativeModel gm2 = new GenerativeModel("gemini-pro-vision", BuildConfig.apiKey, generationConfig, safetyList);

        // 使用 GenerativeModelFutures 创建 GenerativeModelFutures 实例
        model = GenerativeModelFutures.from(gm1);
        modelVision = GenerativeModelFutures.from(gm2);

        createHistoryData();
    }

    private static void setSafetySetting() {
        SafetySetting harassmentSafety = new SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.NONE);
        SafetySetting hateSpeechSafety = new SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.NONE);
        SafetySetting sexuallyExplicitSafety = new SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.NONE);
        SafetySetting dangerousContentSafety = new SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.NONE);
        safetyList = Arrays.asList(harassmentSafety, hateSpeechSafety, sexuallyExplicitSafety, dangerousContentSafety);
    }

    private static void generateConfig() {
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

    public static List<SafetySetting> getSafetyList() {
        return safetyList;
    }

    public static void setSafetyList(List<SafetySetting> safetyList) {
        GenerativeModelManager.safetyList = safetyList;
    }

    public static Float getTemperature() {
        return temperature;
    }

    public static void setTemperature(Float temperature) {
        GenerativeModelManager.temperature = temperature;
    }

    public static Float getTopP() {
        return topP;
    }

    public static void setTopP(Float topP) {
        GenerativeModelManager.topP = topP;
    }

    public static Integer getTopK() {
        return topK;
    }

    public static void setTopK(Integer topK) {
        GenerativeModelManager.topK = topK;
    }

    public static Integer getMaxOutputToken() {
        return maxOutputToken;
    }

    public static void setMaxOutputToken(Integer maxOutputToken) {
        GenerativeModelManager.maxOutputToken = maxOutputToken;
    }

    public static Integer getCandidateCount() {
        return candidateCount;
    }

    public static void setCandidateCount(Integer candidateCount) {
        GenerativeModelManager.candidateCount = candidateCount;
    }

    public static List<String> getStopSequences() {
        return stopSequences;
    }

    public static void setStopSequences(List<String> stopSequences) {
        GenerativeModelManager.stopSequences = stopSequences;
    }
}
