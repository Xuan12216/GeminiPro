package com.example.geminipro;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import com.example.geminipro.Adapter.ImageAdapter;
import com.example.geminipro.Adapter.ModelAdapter;
import com.example.geminipro.Model.GenerativeModelManager;
import com.example.geminipro.Util.PickImageFunc;
import com.example.geminipro.Util.RecordFunc;
import com.example.geminipro.Util.SendToServer;
import com.example.geminipro.databinding.ActivityMainBinding;
import com.google.ai.client.generativeai.java.ChatFutures;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ImageAdapter.ImageAdapterListener{

    private ActivityMainBinding binding;
    private GenerativeModelFutures model;
    private List<Uri> imageUris = new ArrayList<>();
    private Context context;
    private ImageAdapter adapterDown;
    private ModelAdapter modelAdapter;
    private List<Content> historyNormal = new ArrayList<>();
    private ChatFutures chatNormal;
    private int index = -1;
    private boolean isWait = false;
    //======
    private RecordFunc recordFunc;
    //======
    private PickImageFunc pickImageFunc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        context = getApplicationContext();

        init();
        setListener();
    }

    private void init(){
        GenerativeModelManager.initializeGenerativeModel();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        modelAdapter = new ModelAdapter(context);
        binding.recyclerView.setAdapter(modelAdapter);
        //===============
        binding.recyclerViewDown.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        adapterDown = new ImageAdapter(context, this);
        binding.recyclerViewDown.setAdapter(adapterDown);
        //===============
        historyNormal = Arrays.asList(GenerativeModelManager.getUserContent(),GenerativeModelManager.getModelContent());
        //===============
        recordFunc = new RecordFunc(this,context);
        //===============
        pickImageFunc = new PickImageFunc(this, context);
    }

    public void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view, Gravity.END, 0, R.style.MyPopupMenuStyle);
        popupMenu.setForceShowIcon(true);
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.menu_item, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.howToUse){
                    return true;
                }
                else if (item.getItemId() == R.id.settings){
                    return true;
                }
                else return false;
            }
        });

        popupMenu.show();
    }

    public void AddImage(View view) {
        pickImageFunc.startPickImage(new PickImageFunc.onImageResultCallback() {
            @Override
            public void onResult(Uri compressedUri) {
                setImageAdapter(compressedUri, false);
            }
        });
    }

    private void setListener(){
        binding.textInputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!isWait){
                    if (s.length() > 0) binding.textInputLayout.setEndIconDrawable(getDrawable(R.drawable.baseline_send_24));
                    else binding.textInputLayout.setEndIconDrawable(getDrawable(R.drawable.baseline_keyboard_voice_24));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        //======================================

        binding.textInputLayout.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = binding.textInputEditText.getText().toString();
                if (!isWait && !text.isEmpty()) handleEndIconClick(text);
                else if (!isWait && text.isEmpty()) gotoRecordFunc();
            }
        });
    }

    private void handleEndIconClick(String text) {

        if (imageUris.size() == 0 && text.isEmpty()) return;

        if(imageUris.size() != 0 && !text.isEmpty()) gotoGeminiBuilder(text, true);
        else if (!text.isEmpty()) gotoGeminiBuilder(text, false);

        setModelAdapter(text, "User");
        setImageAdapter(null, true);
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.textInputEditText.setText("");
        binding.textInputEditText.clearFocus();

        isWait = true;
        binding.textInputLayout.setEndIconDrawable(getDrawable(R.drawable.baseline_stop_circle_24));
    }

    private void gotoRecordFunc() {
        recordFunc.startRecordFunc(new RecordFunc.RecordResultCallback() {
            @Override
            public void onResult(String result) {
                binding.textInputEditText.setText(result);
            }
        });
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
                binding.progressBar.setVisibility(View.GONE);
                binding.recyclerView.smoothScrollToPosition(index);
                if ("Gemini".equals(who)){
                    isWait = false;
                    int size = binding.textInputEditText.getText().toString().length();
                    binding.textInputLayout.setEndIconDrawable(size > 0 ? getDrawable(R.drawable.baseline_send_24)
                            : getDrawable(R.drawable.baseline_keyboard_voice_24));
                }
            }
        });
    }

    private void setImageAdapter(Uri compressedUri, boolean isClearList) {
        if (isClearList) imageUris.clear();
        else if (null != compressedUri) imageUris.add(compressedUri);

        adapterDown.setNewImage(imageUris, true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onImageListUpdated(List<Uri> updatedImageUris) {
        this.imageUris = updatedImageUris;
    }
}