package com.example.geminipro.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.example.geminipro.Adapter.FlexAdapter;
import com.example.geminipro.Adapter.ImageAdapter;
import com.example.geminipro.Adapter.ModelAdapter;
import com.example.geminipro.Database.AppDatabase;
import com.example.geminipro.Fragment.BottomSheet;
import com.example.geminipro.Model.GenerativeModelManager;
import com.example.geminipro.R;
import com.example.geminipro.Util.GeminiContentBuilder;
import com.example.geminipro.Util.PickImageFunc;
import com.example.geminipro.Util.PickImageUsingCamera;
import com.example.geminipro.Util.RecordFunc;
import com.example.geminipro.databinding.ActivityMainBinding;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import kotlin.Triple;

public class MainActivity extends AppCompatActivity implements ImageAdapter.ImageAdapterListener, FlexAdapter.FlexAdapterListener {

    private ActivityMainBinding binding;
    private List<Uri> imageUris = new ArrayList<>();
    private Context context;
    private Handler handler;
    private ModelAdapter modelAdapter;
    private int index = -1;
    private boolean isWait = false;
    private RecordFunc recordFunc;
    private PickImageFunc pickImageFunc;
    private PickImageUsingCamera pickImageUsingCamera;
    private ImageAdapter imageAdapter;
    public static AppDatabase appDatabase;
    //AdapterModel
    private static List<String> StringUris = new ArrayList<>();
    private static List<String> userOrGemini = new ArrayList<>();
    private static HashMap<Integer,List<Uri>> imageHashMap = new HashMap<Integer,List<Uri>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        context = this;

        init();
        setListener();
    }
    //===init=====================================================
    private void init(){
        appDatabase = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "my-database").build();
        //==============
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        modelAdapter = new ModelAdapter(context);
        binding.recyclerView.setAdapter(modelAdapter);
        if (!StringUris.isEmpty() && !userOrGemini.isEmpty()){
            modelAdapter.receiveDataAndShow(StringUris, userOrGemini, imageHashMap);
            index = StringUris.size() - 1;
            StringUris.clear();
            userOrGemini.clear();
            imageHashMap.clear();
        }
        else GenerativeModelManager.initializeGenerativeModel(context);
        //===============
        binding.recyclerViewDown.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        imageAdapter = new ImageAdapter(context, this);
        binding.recyclerViewDown.setAdapter(imageAdapter);
        //===============
        binding.recyclerViewFlex.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        FlexAdapter flexAdapter = new FlexAdapter(context, this);
        binding.recyclerViewFlex.setAdapter(flexAdapter);
        String[] title = getResources().getStringArray(R.array.flexboxItem);
        flexAdapter.setSettingTitle(title);
        checkShowSuggestionsOrNot();
        //===============
        recordFunc = new RecordFunc(this,context);
        //===============
        pickImageFunc = new PickImageFunc(this, context);
        pickImageUsingCamera = new PickImageUsingCamera(this,context);
    }
    //===listener=====================================================
    public void openNavigationDrawer(View view) {
        binding.drawerLayout.openDrawer(GravityCompat.START);
        View headerView = binding.navigationView.getHeaderView(0);
        ImageView imageView = headerView.findViewById(R.id.avatarImageView);
        TextView textView = headerView.findViewById(R.id.navUserName);

        SharedPreferences preferences = context.getSharedPreferences("gemini_private_prefs", Context.MODE_PRIVATE);
        String userName = preferences.getString("userName", "");
        String storedImagePath = preferences.getString("userImage", "");

        if (!storedImagePath.isEmpty()) Glide.with(context).load(storedImagePath).into(imageView);
        if (!userName.isEmpty()) textView.setText(userName);
    }
    //=====
    public void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view, Gravity.END, 0, R.style.MyPopupMenuStyle);
        popupMenu.setForceShowIcon(true);
        popupMenu.inflate(R.menu.menu_item);

        int size = popupMenu.getMenu().size();
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES){
            for (int i = 0; i < size; i++){
                Drawable iconDrawable = popupMenu.getMenu().getItem(i).getIcon();
                if (iconDrawable != null) {
                    iconDrawable.setTint(getResources().getColor(R.color.white,null));
                }
            }
        }

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.Info) {
                    startActivity(new Intent(MainActivity.this, InfoActivity.class));
                    return true;
                }
                else if (item.getItemId() == R.id.settings){
                    startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                    return true;
                }
                else return false;
            }
        });

        popupMenu.show();
    }
    //=====
    public void AddImage(View view) {

        BottomSheet bottomSheet = new BottomSheet();
        bottomSheet.setCallback(new BottomSheet.BottomSheetCallback() {
            @Override
            public void onCameraClicked() {
                pickImageUsingCamera.startPickImage(new PickImageUsingCamera.onImageResultCallback() {
                    @Override
                    public void onResult(Uri compressedUri) {
                        setImageAdapter(compressedUri, false);
                    }
                });
            }

            @Override
            public void onGalleryClicked() {
                pickImageFunc.startPickImage(new PickImageFunc.onImageResultCallback() {
                    @Override
                    public void onResult(Uri compressedUri) {
                        setImageAdapter(compressedUri, false);
                    }
                });
            }
        });
        bottomSheet.show(getSupportFragmentManager(), bottomSheet.getTag());
    }
    //=====
    private void setListener(){
        binding.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                boolean canScrollDown = recyclerView.canScrollVertically(1);

                if (!canScrollDown) binding.fabScrollToBottom.hide();
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
        //======================================
        binding.addNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (index != -1 && index % 2 != 0 ){
                    StringUris.clear();
                    userOrGemini.clear();
                    imageHashMap.clear();
                    modelAdapter.receiveDataAndShow(StringUris, userOrGemini, imageHashMap);
                    index = - 1;
                    checkShowSuggestionsOrNot();
                    Toast.makeText(context, R.string.add_notes_toast,Toast.LENGTH_SHORT).show();
                }
                else if (index == -1) Toast.makeText(context, R.string.add_notes_toast,Toast.LENGTH_SHORT).show();
                else Toast.makeText(context, R.string.add_notes_toast1,Toast.LENGTH_SHORT).show();
            }
        });
    }
    //===prepare=====================================================
    private void handleEndIconClick(String text) {

        if (imageUris.size() == 0 && text.isEmpty()) return;

        gotoGeminiBuilder(text, imageUris.size() > 0 && !text.isEmpty());

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
        GeminiContentBuilder builder = new GeminiContentBuilder(imageUris,context, getLifecycle());
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
                checkShowSuggestionsOrNot();
            }
        });
    }

    private void checkShowSuggestionsOrNot() {
        if (index == -1) {
            binding.recyclerViewFlex.setVisibility(View.VISIBLE);
            binding.recyclerViewFlex.smoothScrollToPosition(0);
            binding.welcomeLayout.setVisibility(View.VISIBLE);
            binding.welcomeText.setText("");

            String text = getResources().getString(R.string.welcome_text);
            final int[] currentIndex = {0};

            if (null != handler) {
                handler.removeCallbacksAndMessages(null);
                handler = null;
            }

            handler = new Handler();
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    if (currentIndex[0] < text.length()) {
                        binding.welcomeText.append(String.valueOf(text.charAt(currentIndex[0])));
                        currentIndex[0]++;
                        handler.postDelayed(this, 100);
                    }
                }
            };

            handler.postDelayed(runnable, 100);
        }
        else {
            binding.recyclerViewFlex.setVisibility(View.GONE);
            binding.welcomeLayout.setVisibility(View.GONE);
        }
    }

    //=====
    private void setImageAdapter(Uri compressedUri, boolean isClearList) {
        if (isClearList) imageUris.clear();
        else if (null != compressedUri) imageUris.add(compressedUri);

        imageAdapter.setNewImage(imageUris, true);
    }
    //===build and send=====================================================
    @Override
    public void onImageListUpdated(List<Uri> updatedImageUris) {
        this.imageUris = updatedImageUris;
    }

    @Override
    public void onChooseFlex(String text) {
        if (!text.isEmpty()) binding.textInputEditText.setText(text);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (null != modelAdapter) modelAdapter.checkSharedPreferences();
        GenerativeModelManager.checkApiKey(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        //save adapter data
        Triple<List<String>, List<String>, HashMap<Integer, List<Uri>>> result = modelAdapter.saveData();
        StringUris = new ArrayList<>(result.getFirst());
        userOrGemini = new ArrayList<>(result.getSecond());
        imageHashMap = new HashMap<>(result.getThird());
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();

        if (null != handler) {
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }
    }
}