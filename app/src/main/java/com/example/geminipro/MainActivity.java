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
import android.view.View;
import android.widget.ProgressBar;

import com.example.geminipro.Adapter.ImageAdapter;
import com.example.geminipro.Adapter.ModelAdapter;
import com.example.geminipro.Model.GenerativeModelManager;
import com.example.geminipro.Util.ImageResize;
import com.google.ai.client.generativeai.java.ChatFutures;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

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
    private List<Content> history = new ArrayList<>();
    private ChatFutures chat;

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
        pickMedia = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                this::saveImage
        );
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        modelAdapter = new ModelAdapter(context);
        recyclerView.setAdapter(modelAdapter);

        recyclerViewDown.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        adapterDown = new ImageAdapter(context);
        recyclerViewDown.setAdapter(adapterDown);
    }

    private void setListener(){

        textInputLayout.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("TestXuan: "+history.size());
                String text = textInputEditText.getText().toString();
                Content contentUser = null;

                if (imageUris.size() == 0 && text.isEmpty()) return;

                if(imageUris.size() != 0 && !text.isEmpty()){
                    model = GenerativeModelManager.getGenerativeModelVision();

                    Content.Builder builder = new Content.Builder();
                    builder.setRole("user");
                    builder.addText(text);

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
                    contentUser = builder.build();
                }
                else if (!text.isEmpty()){
                    model = GenerativeModelManager.getGenerativeModel();
                    Content.Builder builder = new Content.Builder();
                    builder.setRole("user");
                    builder.addText(text);

                    contentUser = builder.build();
                }

                if (null == contentUser) return;

                setModelAdapter(text, "User");
                setImageAdapter(null, true);
                progressBar.setVisibility(View.VISIBLE);
                textInputEditText.setText("");

                Executor executor = Executors.newSingleThreadExecutor();

                if (null == chat){
                    System.out.println("TestXuan: ChatNull");
                    chat = model.startChat(history);
                }

                ListenableFuture<GenerateContentResponse> response = chat.sendMessage(contentUser);
                Content finalContentUser = contentUser;
                Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
                    @Override
                    public void onSuccess(GenerateContentResponse result) {
                        String resultText = result.getText();
                        setModelAdapter(resultText, "Gemini");

                        Content.Builder builder = new Content.Builder();
                        builder.setRole("model");
                        builder.addText(resultText);
                        Content contentModel = builder.build();
                        history = Arrays.asList(finalContentUser, contentModel);
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        setModelAdapter(t.toString(), "Gemini");
                        System.out.println("TestXuan: "+t.toString());

                        Content.Builder builder = new Content.Builder();
                        builder.setRole("model");
                        builder.addText(t.toString());
                        Content contentModel = builder.build();
                        history = Arrays.asList(finalContentUser, contentModel);
                    }
                }, executor);
            }
        });
    }

    public void setModelAdapter(String resultText, String who){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                modelAdapter.addData(resultText, who);
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void setImageAdapter(Uri compressedUri, boolean isClearList) {
        if (isClearList) imageUris.clear();
        else if (null != compressedUri) imageUris.add(compressedUri);

        adapterDown.setNewImage(imageUris);
    }
}