package com.example.geminipro.Util;

import android.content.Context;

import com.example.geminipro.Model.GenerativeModelManager;
import com.google.ai.client.generativeai.java.ChatFutures;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SendToServer {
    private GenerativeModelFutures model;
    private ChatFutures chatNormal;
    private Context context;

    public SendToServer(GenerativeModelFutures model, Context context) {
        this.model = model;
        this.context = context;
    }

    public SendToServer(ChatFutures chatNormal, Context context) {
        this.chatNormal = chatNormal;
        this.context = context;
    }

    public void sendToServerFunc(boolean isVision, Content contentUser, ResultCallback callback) {
        if (isVision){
            Executor executor = Executors.newSingleThreadExecutor();
            ListenableFuture<GenerateContentResponse> response = model.generateContent(contentUser);
            Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
                @Override
                public void onSuccess(GenerateContentResponse result) {
                    String resultText = result.getText();
                    callback.onResult(resultText);
                }

                @Override
                public void onFailure(Throwable t) {
                    t.printStackTrace();
                    callback.onResult(t.toString());
                }
            }, executor);
        }
        else {
            Publisher<GenerateContentResponse> response = chatNormal.sendMessageStream(contentUser);
            final String[] fullResponse = {""};

            response.subscribe(new Subscriber<GenerateContentResponse>() {
                @Override
                public void onNext(GenerateContentResponse generateContentResponse) {
                    String chunk = generateContentResponse.getText();
                    fullResponse[0] += chunk;
                }

                @Override
                public void onComplete() {
                    callback.onResult(fullResponse[0]);
                }

                @Override
                public void onError(Throwable t) {
                    t.printStackTrace();
                    callback.onResult(t.toString());
                }

                @Override
                public void onSubscribe(Subscription s) { s.request(10);}
            });
        }
    }

    public interface ResultCallback {
        void onResult(String result);
    }
}
