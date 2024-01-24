package com.example.geminipro.Util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;

import com.example.geminipro.Model.GenerativeModelManager;
import com.google.ai.client.generativeai.java.ChatFutures;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GeminiContentBuilder {
    private static ChatFutures chatNormal;
    private List<Content> historyNormal = new ArrayList<>();
    private List<Uri> imageUris = new ArrayList<>();
    private Context context;
    private Lifecycle lifecycle;

    public GeminiContentBuilder(List<Uri> imageUris, Context context, Lifecycle lifecycle){
        this.context = context;
        this.imageUris = imageUris;
        this.lifecycle = lifecycle;
        historyNormal = Arrays.asList(GenerativeModelManager.getUserContent(),GenerativeModelManager.getModelContent());
    }

    public void startGeminiBuilder(String text, boolean isVision, GeminiBuilderCallback callback){
        GenerativeModelFutures model = isVision ? GenerativeModelManager.getGenerativeModelVision()
                : GenerativeModelManager.getGenerativeModel();

        Content.Builder builder = new Content.Builder();
        builder.setRole("user");
        builder.addText(text);

        if (isVision){
            for (Uri uri : imageUris) {
                try {
                    InputStream inputStream = context.getContentResolver().openInputStream(uri);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    assert inputStream != null;
                    inputStream.close();
                    builder.addImage(bitmap);
                }
                catch (IOException e) {e.printStackTrace();throw new RuntimeException(e);}
            }
        }
        else if (null == chatNormal) chatNormal = model.startChat(historyNormal);

        Content contentUser = builder.build();

        SendToServer sendToServer = isVision ? new SendToServer(model, context, lifecycle) : new SendToServer(chatNormal, context, lifecycle);
        sendToServer.sendToServerFunc(isVision, contentUser, new SendToServer.ResultCallback() {
            @Override
            public void onResult(String result) {
                callback.callBackResult(result);
            }
        });
    }

    public static void resetChatNormal() {
        GeminiContentBuilder.chatNormal = null;
    }

    public interface GeminiBuilderCallback{
        void callBackResult(String text);
    }
}
