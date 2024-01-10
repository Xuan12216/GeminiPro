package com.example.geminipro.Util;

import com.example.geminipro.Model.GenerativeModelManager;
import com.google.ai.client.generativeai.java.ChatFutures;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class SendToServer {
    private GenerativeModelFutures model;
    private ChatFutures chatNormal;

    public SendToServer(GenerativeModelFutures model) {
        this.model = model;
    }

    public SendToServer(ChatFutures chatNormal) {
        this.chatNormal = chatNormal;
    }

    public void sendToServerFunc(boolean isVision, Content contentUser, ResultCallback callback) {
        Executor executor = Executors.newSingleThreadExecutor();
        ListenableFuture<GenerateContentResponse> response = isVision ? model.generateContent(contentUser) : chatNormal.sendMessage(contentUser);
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String resultText = result.getText();
                callback.onResult(resultText);
            }

            @Override
            public void onFailure(Throwable t) {
                System.out.println("TestXuan: " + t.toString());
                callback.onResult(t.toString());
                GenerativeModelManager.initializeGenerativeModel();
            }
        }, executor);
    }

    public interface ResultCallback {
        void onResult(String result);
    }
}
