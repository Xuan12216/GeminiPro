package com.example.geminipro.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.example.geminipro.Adapter.FlexAdapter;
import com.example.geminipro.Adapter.HistoryAdapter;
import com.example.geminipro.Adapter.ImageAdapter;
import com.example.geminipro.Adapter.ModelAdapter;
import com.example.geminipro.Database.AppDatabase;
import com.example.geminipro.Database.User;
import com.example.geminipro.Database.UserDao;
import com.example.geminipro.Fragment.BottomSheet;
import com.example.geminipro.Model.GenerativeModelManager;
import com.example.geminipro.R;
import com.example.geminipro.Util.GeminiContentBuilder;
import com.example.geminipro.Util.PickImageFunc;
import com.example.geminipro.Util.PickImageUsingCamera;
import com.example.geminipro.Util.RecordFunc;
import com.example.geminipro.databinding.ActivityMainBinding;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import kotlin.Triple;

public class MainActivity extends AppCompatActivity implements ImageAdapter.ImageAdapterListener, FlexAdapter.FlexAdapterListener, HistoryAdapter.HistoryAdapterListener {
    private ActivityMainBinding binding;
    private List<Uri> imageUris = new ArrayList<>();
    private Context context;
    private Handler handler;
    private ModelAdapter modelAdapter;
    private HistoryAdapter historyAdapter;
    private int index = -1;
    private boolean isWait = false;
    private RecordFunc recordFunc;
    private PickImageFunc pickImageFunc;
    private PickImageUsingCamera pickImageUsingCamera;
    private ImageAdapter imageAdapter;
    public static UserDao userDao;
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
        //database
        AppDatabase appDatabase = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "my-database").build();
        userDao = appDatabase.userDao();

        //==============
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        modelAdapter = new ModelAdapter(context);
        binding.recyclerView.setAdapter(modelAdapter);
        //解決轉換theme時會被重置的問題
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
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
        binding.drawerLayout.openDrawer(GravityCompat.START);
    }

    //=====
    public void AddImage(View view) {

        BottomSheet bottomSheet = new BottomSheet();
        bottomSheet.setCallback(new BottomSheet.BottomSheetCallback() {
            @Override
            public void onCameraClicked() {
                pickImageUsingCamera.startPickImage(new PickImageUsingCamera.onImageResultCallback() {
                    @Override
                    public void onResult(Uri compressedUri) { setImageAdapter(compressedUri, false);}
                });
            }

            @Override
            public void onGalleryClicked() {
                pickImageFunc.startPickImage(new PickImageFunc.onImageResultCallback() {
                    @Override
                    public void onResult(Uri compressedUri) { setImageAdapter(compressedUri, false);}
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

        //=====
        binding.fabScrollToBottom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i = modelAdapter.getItemCount() - 1;
                if (i >= 0) binding.recyclerView.smoothScrollToPosition(i);
            }
        });

        //=====
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

        //=====
        binding.textInputLayout.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = Objects.requireNonNull(binding.textInputEditText.getText()).toString();
                if (!isWait && !text.isEmpty()) handleEndIconClick(text);
                else if (!isWait) gotoRecordFunc();
            }
        });

        //=====
        binding.addNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (index != -1 && index % 2 != 0 ){
                    StringUris.clear();
                    userOrGemini.clear();
                    imageHashMap.clear();
                    index = - 1;
                    checkShowSuggestionsOrNot();
                    Toast.makeText(context, R.string.add_notes_toast,Toast.LENGTH_SHORT).show();
                    saveDataFunc(true);
                }
                else if (index == -1) {
                    checkShowSuggestionsOrNot();
                    Toast.makeText(context, R.string.add_notes_toast,Toast.LENGTH_SHORT).show();
                }
                else Toast.makeText(context, R.string.add_notes_toast1,Toast.LENGTH_SHORT).show();
            }
        });
    }

    //=====
    @Override
    public void onChooseHistory(User user) {
        saveDataFunc(true);
        if (null != modelAdapter){
            index = user.getStringUris().size() - 1;
            binding.welcomeLayout.setVisibility(View.GONE);
            binding.recyclerViewFlex.setVisibility(View.GONE);

            binding.drawerLayout.postDelayed(new Runnable() {
                @Override
                public void run() {
                    modelAdapter.receiveDataAndShow(user.getStringUris(), user.getUserOrGemini(), user.getImageHashMap());
                    binding.drawerLayout.closeDrawer(GravityCompat.START);
                }
            }, 300);
        }
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
                    int size = Objects.requireNonNull(binding.textInputEditText.getText()).toString().length();
                    binding.textInputLayout.setEndIconDrawable(size > 0 ? getDrawable(R.drawable.baseline_send_24) : getDrawable(R.drawable.baseline_keyboard_voice_24));
                }
                checkShowSuggestionsOrNot();
            }
        });
    }
    //=====
    private void setImageAdapter(Uri compressedUri, boolean isClearList) {
        if (isClearList) imageUris.clear();
        else if (null != compressedUri) imageUris.add(compressedUri);

        imageAdapter.setNewImage(imageUris, true);
    }
    //===method=============================================================
    private void checkShowSuggestionsOrNot() {
        boolean showSuggestions = (index == -1);
        binding.recyclerViewFlex.setVisibility(showSuggestions ? View.VISIBLE : View.GONE);
        binding.welcomeLayout.setVisibility(showSuggestions ? View.VISIBLE : View.GONE);

        if (showSuggestions) {
            binding.recyclerViewFlex.smoothScrollToPosition(0);
            binding.welcomeText.setText("");

            String text = getResources().getString(R.string.welcome_text);
            final int[] currentIndex = {0};

            if (handler != null) {
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
                        handler.postDelayed(this, 50);
                    }
                }
            };

            handler.postDelayed(runnable, 300);
        }
    }

    //=====
    private void getAndSetProfilePicture() {
        SharedPreferences preferences = context.getSharedPreferences("gemini_private_prefs", Context.MODE_PRIVATE);
        String userName = preferences.getString("userName", "");
        String storedImagePath = preferences.getString("userImage", "");

        if (!storedImagePath.isEmpty()) Glide.with(context).load(storedImagePath).into(binding.navigationDrawerButton);
        else Glide.with(context).load(R.drawable.baseline_person_24).into(binding.navigationDrawerButton);

        View headerView = binding.navigationView.getHeaderView(0);
        ImageView imageView = headerView.findViewById(R.id.avatarImageView);
        TextView textView = headerView.findViewById(R.id.navUserName);
        RecyclerView recyclerView_history = headerView.findViewById(R.id.recyclerView_history);
        ImageView image_more = headerView.findViewById(R.id.imageView_more);

        recyclerView_history.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        historyAdapter = new HistoryAdapter(context, this);
        recyclerView_history.setAdapter(historyAdapter);

        image_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(context, v, Gravity.END, 0, R.style.MyPopupMenuStyle);
                popupMenu.setForceShowIcon(true);
                popupMenu.inflate(R.menu.menu_item);
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
                        return false;
                    }
                });
                popupMenu.show();
            }
        });

        if (!storedImagePath.isEmpty()) Glide.with(context).load(storedImagePath).into(imageView);
        if (!userName.isEmpty()) textView.setText(userName);
    }
    //=====
    private void saveDataFunc(boolean clear) {
        //save adapter data
        Triple<List<String>, List<String>, HashMap<Integer, List<Uri>>> result = modelAdapter.saveData();
        StringUris = new ArrayList<>(result.getFirst());
        userOrGemini = new ArrayList<>(result.getSecond());
        imageHashMap = new HashMap<>(result.getThird());

        if (!StringUris.isEmpty() && !userOrGemini.isEmpty()){
            Date date = new Date();
            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date);
            User user = new User(StringUris.get(0), today, StringUris, userOrGemini, imageHashMap);

            ExecutorService service = Executors.newSingleThreadExecutor();
            service.execute(new Runnable() {
                @Override
                public void run() {
                    User existingUser = userDao.getUserByTitle(user.getTitle());
                    if (existingUser == null) userDao.insertUser(user);
                    else {
                        user.setId(existingUser.getId()); // 設置現有用戶的 ID
                        userDao.updateUser(user); // 使用更新方法更新用戶數據
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            getSaveData();
                            clearDataIfNecessary(clear);
                        }
                    });
                }
            });
        }
        else clearDataIfNecessary(clear);
    }
    //=====
    private void clearDataIfNecessary(boolean clear) {
        if (clear){
            StringUris.clear();
            userOrGemini.clear();
            imageHashMap.clear();
            modelAdapter.receiveDataAndShow(StringUris, userOrGemini, imageHashMap);
        }
    }
    //=====
    private void getSaveData() {
        ExecutorService service = Executors.newSingleThreadExecutor();
        service.execute(new Runnable() {
            @Override
            public void run() {
                List<User> users = userDao.getAllUsersDesc();
                if (!users.isEmpty()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() { historyAdapter.setSettingTitle(users);}
                    });

                }
            }
        });
    }
    //===build and send=====================================================
    @Override
    public void onImageListUpdated(List<Uri> updatedImageUris) {
        this.imageUris = updatedImageUris;
    }
    //=====
    @Override
    public void onChooseFlex(String text) {
        if (!text.isEmpty()) binding.textInputEditText.setText(text);
    }
    //=====
    @Override
    protected void onResume() {
        super.onResume();
        if (null != modelAdapter) modelAdapter.checkSharedPreferences();
        GenerativeModelManager.checkApiKey(this);
        //===============
        getAndSetProfilePicture();

        getSaveData();
    }
    //=====
    @Override
    protected void onPause() {
        super.onPause();

        saveDataFunc(false);
    }
    //=====
    @Override
    protected void onDestroy(){
        super.onDestroy();

        if (null != handler) {
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }
    }
}