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
    public static Content userContent, modelContent;
    private static GenerationConfig generationConfig;
    private static List<SafetySetting> safetyList;

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
        configBuilder.temperature = 0.9f;
        configBuilder.topK = 10;
        configBuilder.topP = 0.8f;
        configBuilder.maxOutputTokens = 2048;
        configBuilder.stopSequences = Arrays.asList("red");

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
}
