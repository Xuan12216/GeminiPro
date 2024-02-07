package com.example.geminipro.Util;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import com.google.ai.client.generativeai.java.ChatFutures;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.uber.autodispose.AutoDispose;
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class SendToServer {
    private GenerativeModelFutures modelVision;
    private ChatFutures chatNormal;
    private final Context context;
    private final Lifecycle lifecycle;

    public SendToServer(GenerativeModelFutures modelVision, Context context, Lifecycle lifecycle) {
        this.modelVision = modelVision;
        this.context = context;
        this.lifecycle = lifecycle;
    }

    public SendToServer(ChatFutures chatNormal, Context context, Lifecycle lifecycle) {
        this.chatNormal = chatNormal;
        this.context = context;
        this.lifecycle = lifecycle;
    }

    public void sendToServerFunc(boolean isVision, Content contentUser, ResultCallback callback) {
        ListenableFuture<GenerateContentResponse> response = isVision ? modelVision.generateContent(contentUser) : chatNormal.sendMessage(contentUser);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            Single<GenerateContentResponse> single = Single.create(
                    e -> Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
                        @Override
                        public void onSuccess(GenerateContentResponse result) {
                            e.onSuccess(result);
                            String resultText = result.getText();
                            callback.onResult(resultText);
                        }

                        @Override
                        public void onFailure(@NonNull Throwable throwable) {
                            e.onError(throwable);
                            if (throwable.toString().contains("PromptBlockedException")) {
                                callback.onResult(throwable.toString());
                            }
                            else tryToUseStreamSendToServer(contentUser, callback, isVision);
                        }
                    }, context.getMainExecutor()));

            single.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .as(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(lifecycle)))
                    .subscribe(result -> {}, throwable -> {});
        }
        else tryToUseStreamSendToServer(contentUser, callback, isVision);
    }

    private void tryToUseStreamSendToServer(Content contentUser, ResultCallback callback, boolean isVision) {
        Publisher<GenerateContentResponse> response = isVision ? modelVision.generateContentStream(contentUser) : chatNormal.sendMessageStream(contentUser);
        final String[] fullResponse = {""};
        Single<GenerateContentResponse> single = Single.create(
                emitter -> response.subscribe(new Subscriber<GenerateContentResponse>() {
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
                })
        );

        single.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .as(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(lifecycle)))
                .subscribe();
    }

    public interface ResultCallback {
        void onResult(String result);
    }
}
