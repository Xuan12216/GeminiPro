package com.example.geminipro.Util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import androidx.lifecycle.Lifecycle;
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
    private static List<Content> historyNormal = new ArrayList<>();
    private final List<Uri> imageUris;
    private final Context context;
    private final Lifecycle lifecycle;

    public GeminiContentBuilder(List<Uri> imageUris, Context context, Lifecycle lifecycle){
        this.context = context;
        this.imageUris = imageUris;
        this.lifecycle = lifecycle;
        historyNormal = Arrays.asList(GenerativeModelManager.getUserContent(),GenerativeModelManager.getModelContent());
    }

    public void startGeminiBuilder(String text, boolean isVision, GeminiBuilderCallback callback) {
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
                catch (IOException e) {throw new RuntimeException(e);}
            }
        }
        else if (null == chatNormal) {
            chatNormal = model.startChat(historyNormal);
        }

        Content contentUser = builder.build();

        SendToServer sendToServer = isVision ? new SendToServer(model, context, lifecycle) : new SendToServer(chatNormal, context, lifecycle);
        sendToServer.sendToServerFunc(isVision, contentUser, callback::callBackResult);
    }

    public static void resetChatNormal() {
        chatNormal = null;
    }

    public static void setHistoryNormalList(List<String> roleList, List<String> contentList){
        GenerativeModelFutures model = GenerativeModelManager.getGenerativeModel();

        if (roleList.isEmpty() || contentList.isEmpty()) {
            historyNormal = Arrays.asList(GenerativeModelManager.getUserContent(),GenerativeModelManager.getModelContent());
            return;
        }
        historyNormal = new ArrayList<>();
        String lastRole = "";

        for (int i = 0; i < roleList.size(); i++){
            String role = roleList.get(i);
            String content = contentList.get(i);

            if (!role.isEmpty() && !content.isEmpty() && !lastRole.equals(role)){
                Content contentTemp;
                Content.Builder userContentBuilder = new Content.Builder();
                userContentBuilder.setRole(role);
                lastRole = role;
                userContentBuilder.addText(content);
                contentTemp = userContentBuilder.build();
                historyNormal.add(contentTemp);
            }
        }

        chatNormal = model.startChat(historyNormal);
    }

    public interface GeminiBuilderCallback{
        void callBackResult(String text);
    }
}
