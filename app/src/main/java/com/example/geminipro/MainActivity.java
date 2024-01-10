package com.example.geminipro;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ProgressBar;

import com.example.geminipro.Adapter.ImageAdapter;
import com.example.geminipro.Adapter.ModelAdapter;
import com.example.geminipro.Model.GenerativeModelManager;
import com.example.geminipro.Util.ImageResize;
import com.example.geminipro.Util.SendToServer;
import com.google.ai.client.generativeai.java.ChatFutures;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private GenerativeModelFutures model;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;
    private final List<Uri> imageUris = new ArrayList<>();
    private Context context;
    private ImageResize imageResize;
    private RecyclerView recyclerView,recyclerViewDown;
    private ImageAdapter adapterDown;
    private ModelAdapter modelAdapter;
    private TextInputLayout textInputLayout;
    private TextInputEditText textInputEditText;
    private ProgressBar progressBar;
    private List<Content> historyNormal = new ArrayList<>();
    private ChatFutures chatNormal;
    private int index = -1;
    private boolean isWait = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = getApplicationContext();

        progressBar = findViewById(R.id.progressBar);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerViewDown = findViewById(R.id.recyclerViewDown);
        textInputLayout = findViewById(R.id.textInputLayout);
        textInputEditText = findViewById(R.id.textInputEditText);

        init();
        setListener();
    }

    public void AddImage(View view) {
        ActivityResultContracts.PickVisualMedia.VisualMediaType mediaType = (ActivityResultContracts.PickVisualMedia.VisualMediaType) ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE;
        PickVisualMediaRequest request = new PickVisualMediaRequest.Builder()
                .setMediaType(mediaType)
                .build();
        pickMedia.launch(request);
    }

    private void saveImage(Uri imageUri){
        if (imageUri != null) {
            imageResize.imgResize(imageUri, new ImageResize.ImageResizeCallback() {
                @Override
                public void onImageResized(Uri compressedUri) {
                    setImageAdapter(compressedUri, false);
                }
            });
        }
    }

    private void init(){
        imageResize = new ImageResize(context);
        GenerativeModelManager.initializeGenerativeModel();
        pickMedia = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), this::saveImage);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        modelAdapter = new ModelAdapter(context);
        recyclerView.setAdapter(modelAdapter);

        recyclerViewDown.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        adapterDown = new ImageAdapter(context);
        recyclerViewDown.setAdapter(adapterDown);

        Content.Builder userContentBuilder = new Content.Builder();
        userContentBuilder.setRole("user");
        userContentBuilder.addText("Hello.");
        Content userContent = userContentBuilder.build();

        Content.Builder modelContentBuilder = new Content.Builder();
        modelContentBuilder.setRole("model");
        modelContentBuilder.addText("Great to meet you.");
        Content modelContent = modelContentBuilder.build();

        historyNormal = Arrays.asList(userContent,modelContent);
    }

    private void setListener(){
        textInputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!isWait){
                    if (s.length() > 0) textInputLayout.setEndIconDrawable(getDrawable(R.drawable.baseline_send_24));
                    else textInputLayout.setEndIconDrawable(getDrawable(R.drawable.baseline_keyboard_voice_24));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        //======================================

        textInputLayout.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isWait) handleEndIconClick();
            }
        });
    }

    private void handleEndIconClick() {
        String text = textInputEditText.getText().toString();

        if (imageUris.size() == 0 && text.isEmpty()) return;

        if(imageUris.size() != 0 && !text.isEmpty()) gotoGeminiBuilder(text, true);
        else if (!text.isEmpty()) gotoGeminiBuilder(text, false);

        setModelAdapter(text, "User");
        setImageAdapter(null, true);
        progressBar.setVisibility(View.VISIBLE);
        textInputEditText.setText("");
        textInputEditText.clearFocus();

        isWait = true;
        textInputLayout.setEndIconDrawable(getDrawable(R.drawable.baseline_stop_circle_24));
    }

    //====================================================

    private void gotoGeminiBuilder(String text, boolean isVision){
        model = isVision ? GenerativeModelManager.getGenerativeModelVision()
                : GenerativeModelManager.getGenerativeModel();

        Content.Builder builder = new Content.Builder();
        builder.setRole("user");
        builder.addText(text);

        if (isVision){
            for (Uri uri : imageUris) {
                try {
                    InputStream inputStream = getContentResolver().openInputStream(uri);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    inputStream.close();
                    builder.addImage(bitmap);
                }
                catch (FileNotFoundException e) {throw new RuntimeException(e);}
                catch (IOException e) {throw new RuntimeException(e);}
            }
        }
        else if (!isVision && null == chatNormal) chatNormal = model.startChat(historyNormal);

        Content contentUser = builder.build();

        SendToServer sendToServer = isVision ? new SendToServer(model) : new SendToServer(chatNormal);
        sendToServer.sendToServerFunc(isVision ? true : false, contentUser, new SendToServer.ResultCallback() {
            @Override
            public void onResult(String result) {
                setModelAdapter(result, "Gemini");
            }
        });
    }

    //==========================================

    public void setModelAdapter(String resultText, String who){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                index++;
                modelAdapter.addData(resultText,imageUris, who, index);
                progressBar.setVisibility(View.GONE);
                recyclerView.smoothScrollToPosition(index);
                if ("Gemini".equals(who)){
                    isWait = false;
                    int size = textInputEditText.getText().toString().length();
                    textInputLayout.setEndIconDrawable(size > 0 ? getDrawable(R.drawable.baseline_send_24)
                            : getDrawable(R.drawable.baseline_keyboard_voice_24));
                }
            }
        });
    }

    private void setImageAdapter(Uri compressedUri, boolean isClearList) {
        if (isClearList) imageUris.clear();
        else if (null != compressedUri) imageUris.add(compressedUri);

        adapterDown.setNewImage(imageUris);
    }
}