package com.example.geminipro.Model;

import android.content.Context;

import com.example.geminipro.BuildConfig;
import com.example.geminipro.R;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;

public class GenerativeModelManager {
    private static GenerativeModelFutures model;
    private static GenerativeModelFutures modelVision;

    public static void initializeGenerativeModel() {
        // 初始化 Generative Model
        GenerativeModel gm1 = new GenerativeModel("gemini-pro", BuildConfig.apiKey);
        GenerativeModel gm2 = new GenerativeModel("gemini-pro-vision", BuildConfig.apiKey);

        // 使用 GenerativeModelFutures 创建 GenerativeModelFutures 实例
        model = GenerativeModelFutures.from(gm1);
        modelVision = GenerativeModelFutures.from(gm2);
    }

    public static GenerativeModelFutures getGenerativeModel() {
        return model;
    }

    public static GenerativeModelFutures getGenerativeModelVision() {
        return modelVision;
    }
}
