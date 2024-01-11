package com.example.geminipro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Context;
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
import com.example.geminipro.Util.GeminiContentBuilder;
import com.example.geminipro.Util.PickImageFunc;
import com.example.geminipro.Util.RecordFunc;
import com.example.geminipro.databinding.ActivityMainBinding;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ImageAdapter.ImageAdapterListener{

    private ActivityMainBinding binding;
    private List<Uri> imageUris = new ArrayList<>();
    private Context context;
    private ImageAdapter adapterDown;
    private ModelAdapter modelAdapter;
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
    //===init=====================================================
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
        recordFunc = new RecordFunc(this,context);
        //===============
        pickImageFunc = new PickImageFunc(this, context);
    }
    //===listener=====================================================
    public void openNavigationDrawer(View view) {
        binding.drawerLayout.openDrawer(GravityCompat.START);
    }

    public void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view, Gravity.END, 0, R.style.MyPopupMenuStyle);
        popupMenu.setForceShowIcon(true);
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.menu_item, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.howToUse) return true;
                else if (item.getItemId() == R.id.settings) return true;
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
        binding.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy >= 0) binding.fabScrollToBottom.hide();
                else binding.fabScrollToBottom.show();
            }
        });
        //===============================================
        binding.fabScrollToBottom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i = modelAdapter.getItemCount() - 1;
                if (i >= 0) binding.recyclerView.smoothScrollToPosition(i);
            }
        });
        //===============================================
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
    //===prepare=====================================================
    private void handleEndIconClick(String text) {

        if (imageUris.size() == 0 && text.isEmpty()) return;

        gotoGeminiBuilder(text, imageUris.size() != 0 && !text.isEmpty());

        setModelAdapter(text, "User");
        setImageAdapter(null, true);
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.textInputEditText.setText("");
        binding.textInputEditText.clearFocus();

        isWait = true;
        binding.textInputLayout.setEndIconDrawable(getDrawable(R.drawable.baseline_stop_circle_24));
    }
    //=====
    private void gotoRecordFunc() {
        recordFunc.startRecordFunc(new RecordFunc.RecordResultCallback() {
            @Override
            public void onResult(String result) {
                binding.textInputEditText.setText(result);
            }
        });
    }
    //===build and send=====================================================
    private void gotoGeminiBuilder(String text, boolean isVision){
        GeminiContentBuilder builder = new GeminiContentBuilder(imageUris,context);
        builder.startGeminiBuilder(text, isVision, new GeminiContentBuilder.GeminiBuilderCallback() {
            @Override
            public void callBackResult(String result) {
                setModelAdapter(result, "Gemini");
            }
        });
    }
    //===adapter for update data=====================================================
    public void setModelAdapter(String resultText, String who){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                modelAdapter.addData(resultText,imageUris, who, ++index);
                binding.progressBar.setVisibility(View.GONE);
                binding.recyclerView.smoothScrollToPosition(index);
                if ("Gemini".equals(who)){
                    isWait = false;
                    int size = binding.textInputEditText.getText().toString().length();
                    binding.textInputLayout.setEndIconDrawable(size > 0 ? getDrawable(R.drawable.baseline_send_24) : getDrawable(R.drawable.baseline_keyboard_voice_24));
                }
            }
        });
    }

    private void setImageAdapter(Uri compressedUri, boolean isClearList) {
        if (isClearList) imageUris.clear();
        else if (null != compressedUri) imageUris.add(compressedUri);

        adapterDown.setNewImage(imageUris, true);
    }
    //===build and send=====================================================
    @Override
    public void onImageListUpdated(List<Uri> updatedImageUris) {
        this.imageUris = updatedImageUris;
    }
}