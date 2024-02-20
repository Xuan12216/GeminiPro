package com.example.geminipro.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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
import com.example.geminipro.Database.User;
import com.example.geminipro.Database.UserRepository;
import com.example.geminipro.Fragment.BottomSheet;
import com.example.geminipro.Model.GenerativeModelManager;
import com.example.geminipro.R;
import com.example.geminipro.Util.GeminiContentBuilder;
import com.example.geminipro.Util.ImageDialog;
import com.example.geminipro.Util.PickImageFunc;
import com.example.geminipro.Util.PickImageUsingCamera;
import com.example.geminipro.Util.RecordFunc;
import com.example.geminipro.databinding.ActivityMainBinding;
import com.uber.autodispose.AutoDispose;
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

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
    public UserRepository userRepository;
    //AdapterModel
    private static List<String> StringUris = new ArrayList<>();
    private static List<String> userOrGemini = new ArrayList<>();
    private static HashMap<Integer,List<Uri>> imageHashMap = new HashMap<Integer,List<Uri>>();
    private String title = "";
    //NavigationView
    private List<User> usersList = new ArrayList<>();
    //Database
    private boolean isClickByHistory = false;
    private boolean isPause = false;
    private boolean isClear = false;


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
        userRepository = new UserRepository(context, getLifecycle());
        
        //==============
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        modelAdapter = new ModelAdapter(context);
        binding.recyclerView.setAdapter(modelAdapter);
        //解決轉換theme時會被重置的問題
        if (!StringUris.isEmpty() && !userOrGemini.isEmpty()){
            User user = new User("", "", StringUris, userOrGemini, imageHashMap, false);

            modelAdapter.receiveDataAndShow(user);
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

        //===============
        binding.recyclerViewHistory.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        historyAdapter = new HistoryAdapter(context, this);
        binding.recyclerViewHistory.setAdapter(historyAdapter);
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
                if (index != -1 && !isWait ){
                    StringUris.clear();
                    userOrGemini.clear();
                    imageHashMap.clear();
                    index = - 1;
                    checkShowSuggestionsOrNot();
                    Toast.makeText(context, R.string.add_notes_toast,Toast.LENGTH_SHORT).show();
                    saveDataFunc(true);
                    historyAdapter.setTargetTitle("");
                }
                else if (index == -1 && !isWait) {
                    checkShowSuggestionsOrNot();
                    Toast.makeText(context, R.string.add_notes_toast,Toast.LENGTH_SHORT).show();
                }
                else Toast.makeText(context, R.string.add_notes_toast1,Toast.LENGTH_SHORT).show();
            }
        });

        //=====
        binding.searchEdittext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (usersList.size() > 0){
                    List<User> filteredUsers = new ArrayList<>();
                    String keyword = s.toString().toLowerCase();

                    for (User user : usersList){
                        String userTitle = user.getTitle();
                        if (userTitle.toLowerCase().contains(keyword)) filteredUsers.add(user);
                        else {
                            for (String uri : user.getStringUris()) {
                                if (uri.toLowerCase().contains(keyword)) {
                                    filteredUsers.add(user);
                                    break;
                                }
                            }
                        }
                    }

                    if (null != historyAdapter) historyAdapter.setSettingTitle(filteredUsers);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    //=====
    @Override
    public void onChooseHistory(User user) {
        if (isWait) {
            Toast.makeText(context, R.string.add_notes_toast1,Toast.LENGTH_SHORT).show();
            return;
        }

        saveDataFunc(true);
        binding.searchEdittext.setText("");
        if (null != modelAdapter){
            index = user.getStringUris().size() - 1;
            binding.welcomeLayout.setVisibility(View.GONE);
            binding.recyclerViewFlex.setVisibility(View.GONE);
            binding.drawerLayout.closeDrawers();
            binding.progressBar.setVisibility(View.VISIBLE);

            binding.drawerLayout.postDelayed(new Runnable() {
                @Override
                public void run() {
                    modelAdapter.receiveDataAndShow(user);
                    isClickByHistory = true;
                    binding.progressBar.setVisibility(View.GONE);
                }
            }, 1000);
        }
    }

    //=====
    @Override
    public void onChooseHistoryStatus(User user, String status) {
        isClear = false;

        switch (status) {
            case "pin":
                saveDatabase("update", user, "PinUpdate");
                break;
            case "rename":
                isClear = true;
                saveDatabase("update", user, "RenameUpdate");
                break;
            case "delete":
                String currentTarget = historyAdapter.getTargetTitle();
                if (user.getTitle().equals(currentTarget)) isClear = true;
                saveDatabase("delete", user, "DeleteData");
                break;
            default:
                break;
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
        ImageView image_more = headerView.findViewById(R.id.imageView_more);

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

        if (!storedImagePath.isEmpty()) {
            Glide.with(context).load(storedImagePath).into(imageView);
            List<Uri> list = new ArrayList<>();
            list.add(Uri.parse(storedImagePath));
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                    ImageDialog imageDialog = new ImageDialog(context, list, 0);
                    imageDialog.show();
                }
            });

        }
        if (!userName.isEmpty()) textView.setText(userName);

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                Intent intent = new Intent(MainActivity.this, SettingMainActivity.class);
                intent.putExtra("id", "0");
                startActivity(intent);
            }
        });
    }
    //=====

    private void saveDataFunc(boolean clear) {
        System.out.println("TestXuan: saveDataFunc");
        // Save adapter data
        isClear = clear;
        User saveData = modelAdapter.saveData();
        StringUris = new ArrayList<>(saveData.getStringUris());
        userOrGemini = new ArrayList<>(saveData.getUserOrGemini());
        imageHashMap = new HashMap<>(saveData.getImageHashMap());
        title = saveData.getTitle();

        if (!StringUris.isEmpty() && !userOrGemini.isEmpty()){

            if (title.isEmpty()) title = StringUris.get(0);
            System.out.println("TestXuan: title: "+title+": "+StringUris.size());

            // 在異步線程中從數據庫中查詢用戶信息
            userRepository.getUserByTitle(title)
                    .as(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(getLifecycle())))
                    .subscribe(this::matchTitle);

        }
        else clearDataIfNecessary(clear);
    }

    private void matchTitle(User data, Throwable object1) {
        Date date = new Date();
        String today = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(date);
        User user = new User(title, today, StringUris, userOrGemini, imageHashMap, false);
        title = "";
        System.out.println("TestXuan: matchTitle");

        if (data == null) {
            // 如果用戶不存在，則插入新用戶信息
            saveDatabase("insert", user, "Insert Complete");
        }
        else {
            if (isClickByHistory) {
                // 如果是來自歷史點擊，則更新用戶信息
                user.setId(data.getId()); // 設置現有用戶的 ID
                user.setPin(data.isPin());
                user.setTitle(data.getTitle());
                user.setDate(today);
                saveDatabase("update", user, "saveUpdate1");

            } else {
                // 如果是在輸入框輸入的，如果有重複的title則合併新數據到現有的數據
                data.getStringUris().addAll(user.getStringUris());
                data.getUserOrGemini().addAll(user.getUserOrGemini());
                data.getImageHashMap().putAll(user.getImageHashMap());
                data.setDate(today);
                saveDatabase("update", data, "saveUpdate2");
            }
        }
    }

    // 在UI線程中處理下一步操作
    private void handleNextStep(boolean clear) {
        if (!isPause) isClickByHistory = false;
        else isPause = false;
        getSaveData();
        clearDataIfNecessary(clear);
    }
    //=====
    private void clearDataIfNecessary(boolean clear) {
        if (clear){
            StringUris.clear();
            userOrGemini.clear();
            imageHashMap.clear();
            User user = new User("", "", StringUris, userOrGemini, imageHashMap, false);
            modelAdapter.receiveDataAndShow(user);
        }
    }
    //=====
    private void getSaveData() {
        userRepository.getAllUsersDesc()
                .as(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(getLifecycle())))
                .subscribe(userList -> {
                    System.out.println("TestXuan: getSaveData");
                    usersList = userList;
                    historyAdapter.setSettingTitle(userList);
                }, Throwable::printStackTrace);
    }

    //=====
    private void saveDatabase(String type, User user, String printText){
        userRepository.saveDatabase(type, user, printText, new UserRepository.onDoneCallback() {
            @Override
            public void onDone() {
                handleNextStep(isClear);
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

        if (!isWait) saveDataFunc(false);
        isPause = true;
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