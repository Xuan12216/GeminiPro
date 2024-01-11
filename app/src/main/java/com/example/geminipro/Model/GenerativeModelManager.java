package com.example.geminipro.Model;

import android.content.Context;

import com.example.geminipro.BuildConfig;
import com.example.geminipro.R;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;

public class GenerativeModelManager {
    private static GenerativeModelFutures model;
    private static GenerativeModelFutures modelVision;
    public static Content userContent, modelContent;

    public static void initializeGenerativeModel() {
        // 初始化 Generative Model
        GenerativeModel gm1 = new GenerativeModel("gemini-pro", BuildConfig.apiKey);
        GenerativeModel gm2 = new GenerativeModel("gemini-pro-vision", BuildConfig.apiKey);

        // 使用 GenerativeModelFutures 创建 GenerativeModelFutures 实例
        model = GenerativeModelFutures.from(gm1);
        modelVision = GenerativeModelFutures.from(gm2);

        Content.Builder userContentBuilder = new Content.Builder();
        userContentBuilder.setRole("user");
        userContentBuilder.addText("Hello.");
        userContent = userContentBuilder.build();

        Content.Builder modelContentBuilder = new Content.Builder();
        modelContentBuilder.setRole("model");
        modelContentBuilder.addText("Great to meet you.");
        modelContent = modelContentBuilder.build();
    }

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
