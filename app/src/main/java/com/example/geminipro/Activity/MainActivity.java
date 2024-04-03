package com.example.geminipro.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.Toast;
import com.example.geminipro.Adapter.FlexAdapter;
import com.example.geminipro.Adapter.HistoryAdapter;
import com.example.geminipro.Adapter.ImageAdapter;
import com.example.geminipro.Adapter.ModelAdapter;
import com.example.geminipro.Database.User;
import com.example.geminipro.Database.UserRepository;
import com.example.geminipro.Fragment.BottomSheet;
import com.example.geminipro.Model.GenerativeModelManager;
import com.example.geminipro.Object.ControlShowSuggestions;
import com.example.geminipro.Object.NavigationLayout;
import com.example.geminipro.R;
import com.example.geminipro.Util.GeminiContentBuilder;
import com.example.geminipro.Util.PickImageFunc;
import com.example.geminipro.Util.PickImageUsingCamera;
import com.example.geminipro.Util.RecordFunc;
import com.example.geminipro.Util.Utils;
import com.example.geminipro.databinding.ActivityMainBinding;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import io.reactivex.disposables.CompositeDisposable;

public class MainActivity extends AppCompatActivity implements ImageAdapter.ImageAdapterListener, FlexAdapter.FlexAdapterListener, HistoryAdapter.HistoryAdapterListener {
    private ActivityMainBinding binding;
    private List<Uri> imageUris = new ArrayList<>();
    private Context context;
    private ModelAdapter modelAdapter;
    private HistoryAdapter historyAdapter;
    private FlexAdapter flexAdapter;
    private ImageAdapter imageAdapter;
    private boolean isWait = false, isClickByHistory = false, isClear = false;
    private RecordFunc recordFunc;
    private PickImageFunc pickImageFunc;
    private PickImageUsingCamera pickImageUsingCamera;
    public UserRepository userRepository;
    private User forDefault;
    private static User forChangeTheme;
    private List<User> usersList = new ArrayList<>();
    private CompositeDisposable disposable;
    private NavigationLayout navigationLayout;
    private ControlShowSuggestions suggestions;

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
    private void init() {
        suggestions = new ControlShowSuggestions(binding, context);
        //===============
        suggestions.showSuggestions(true);
        //database
        userRepository = new UserRepository(context, getLifecycle());
        //==============
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        modelAdapter = new ModelAdapter(context);
        binding.recyclerView.setAdapter(modelAdapter);
        if (null != forChangeTheme){//解決轉換theme時會被重置的問題
            boolean isEmpty = forChangeTheme.getStringUris().isEmpty();
            if (null != suggestions) suggestions.showSuggestions(isEmpty);
            if (!isEmpty && null != suggestions) suggestions.setTitleView(forChangeTheme.getTitle());
            modelAdapter.receiveDataAndShow(forChangeTheme);
            resetList();
        }
        else GenerativeModelManager.initializeGenerativeModel(context);
        //===============
        binding.recyclerViewDown.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        imageAdapter = new ImageAdapter(context, this);
        binding.recyclerViewDown.setAdapter(imageAdapter);
        //===============
        binding.recyclerViewFlex.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        flexAdapter = new FlexAdapter(context, this);
        binding.recyclerViewFlex.setAdapter(flexAdapter);
        String[] title = getResources().getStringArray(R.array.flexboxItem);
        flexAdapter.setSettingTitle(title);
        //===============
        recordFunc = new RecordFunc(this,context);
        //===============
        pickImageFunc = new PickImageFunc(this, context);
        pickImageUsingCamera = new PickImageUsingCamera(this,context);
        //===============
        binding.recyclerViewHistory.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        historyAdapter = new HistoryAdapter(context, this);
        binding.recyclerViewHistory.setAdapter(historyAdapter);
        //===============
        navigationLayout = new NavigationLayout(binding, context);
    }
    //===listener=====================================================
    public void openNavigationDrawer(View view) {
        historyAdapter.setSettingTitle(usersList);
        binding.searchEdittext.setText("");
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
        view.postDelayed(() -> binding.drawerLayout.openDrawer(GravityCompat.START), 300);
    }
    //=====
    public void AddImage(View view) {
        BottomSheet bottomSheet = new BottomSheet();
        bottomSheet.setCallback(new BottomSheet.BottomSheetCallback() {
            @Override
            public void onCameraClicked() {
                pickImageUsingCamera.startPickImage((compressedUri) -> setImageAdapter(compressedUri, false));
            }
            @Override
            public void onGalleryClicked() {
                pickImageFunc.startPickImage((compressedUri) -> setImageAdapter(compressedUri, false));
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
        binding.recyclerViewHistory.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                Utils.hideKeyboard((Activity) context);
            }
        });
        //=====
        binding.fabScrollToBottom.setOnClickListener((v) -> {
            int i = modelAdapter.getItemCount() - 1;
            if (i >= 0) binding.recyclerView.smoothScrollToPosition(i);
        });
        //=====
        binding.textInputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!isWait){
                    String text = s.toString().trim();
                    if (!text.isEmpty()) binding.textInputLayout.setEndIconDrawable(AppCompatResources.getDrawable(context, R.drawable.baseline_send_24));
                    else binding.textInputLayout.setEndIconDrawable(AppCompatResources.getDrawable(context, R.drawable.baseline_keyboard_voice_24));
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
        //=====
        binding.textInputLayout.setEndIconOnClickListener(v -> {
            String text = Objects.requireNonNull(binding.textInputEditText.getText()).toString().trim();
            if (!isWait && !text.isEmpty()) handleEndIconClick(text);
            else if (!isWait) gotoRecordFunc();
        });
        //=====
        binding.addNote.setOnClickListener(v -> {
            isClear = true;
            int index = modelAdapter.getItemCount();
            flexAdapter.refreshTitle();
            historyAdapter.setTargetTitle("");

            if (index != 0 && !isWait ){
                resetList();
                if (null != suggestions) suggestions.showSuggestions(true);
                Toast.makeText(context, R.string.add_notes_toast,Toast.LENGTH_SHORT).show();
                saveDataFunc(false);
            }
            else if (index == 0 && !isWait) {
                if (null != suggestions) suggestions.showSuggestions(true);
                Toast.makeText(context, R.string.add_notes_toast,Toast.LENGTH_SHORT).show();
            }
            else Toast.makeText(context, R.string.add_notes_toast1,Toast.LENGTH_SHORT).show();
        });
        //=====
        binding.searchEdittext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!usersList.isEmpty()) {
                    Utils.parallelSearch(context, usersList, s.toString().toLowerCase(), filteredUsers -> {
                        if (historyAdapter != null) historyAdapter.setSettingTitle(filteredUsers);
                    });
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    //=====
    @Override
    public void onChooseHistory(User user) {
        if (isWait) {
            Toast.makeText(context, R.string.add_notes_toast1,Toast.LENGTH_SHORT).show();
            return;
        }

        if (null != suggestions) suggestions.showSuggestions(false);
        isClear = true;
        saveDataFunc(false);
        historyAdapter.setTargetTitle(user.getTitle());

        binding.searchEdittext.setText("");
        if (null != modelAdapter){
            binding.drawerLayout.closeDrawer(GravityCompat.START);
            binding.progressBar.setVisibility(View.VISIBLE);
            if (null != suggestions) suggestions.setTitleView(user.getTitle());

            binding.progressBar.postDelayed(() -> {
                modelAdapter.receiveDataAndShow(user);
                binding.progressBar.setVisibility(View.GONE);
            },1000);
        }
    }
    //=====
    @Override
    public void onChooseHistoryStatus(User user, String status, String oriName) {
        String currentTarget;

        switch (status) {
            case "pin":
                saveDatabase("update", user, "PinUpdate");
                break;
            case "rename":
                currentTarget = historyAdapter.getTargetTitle();
                if (null != oriName && null != currentTarget && !oriName.isEmpty()
                        && !currentTarget.isEmpty() && oriName.equals(currentTarget)){
                    isClear = true;
                    if (null != suggestions) suggestions.showSuggestions(true);
                }
                saveDatabase("update", user, "RenameUpdate");
                break;
            case "delete":
                currentTarget = historyAdapter.getTargetTitle();
                if (user.getTitle().equals(currentTarget)) {
                    isClear = true;
                    if (null != suggestions) suggestions.showSuggestions(true);
                }
                saveDatabase("delete", user, "DeleteData");
                break;
            default:
                break;
        }
    }
    //===prepare=====================================================
    private void handleEndIconClick(String text) {

        if (imageUris.isEmpty() && text.isEmpty()) return;

        gotoGeminiBuilder(text, !imageUris.isEmpty() && !text.isEmpty());
        setModelAdapter(text, "user");//把資料設定到adapter
        if (null != suggestions) suggestions.showSuggestions(false);
        setImageAdapter(null, true);
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.textInputEditText.setText("");
        binding.textInputEditText.clearFocus();
        isWait = true;
        binding.textInputLayout.setEndIconDrawable(AppCompatResources.getDrawable(context, R.drawable.baseline_stop_circle_24));
    }
    //=====
    private void gotoRecordFunc() {
        recordFunc.startRecordFunc(result -> binding.textInputEditText.setText(result));
    }
    //===build and send=====================================================
    private void gotoGeminiBuilder(String text, boolean isVision){
        GeminiContentBuilder builder = new GeminiContentBuilder(imageUris,context, getLifecycle());
        builder.startGeminiBuilder(text, isVision, result -> setModelAdapter(result, "model"));
    }
    //===adapter for update data=====================================================
    public void setModelAdapter(String resultText, String who){
        runOnUiThread(() -> {
            modelAdapter.addData(resultText,imageUris, who, modelAdapter.getItemCount());
            binding.progressBar.setVisibility(View.GONE);
            binding.recyclerView.smoothScrollToPosition(modelAdapter.getItemCount());
            if ("model".equals(who)){
                isWait = false;
                int size = Objects.requireNonNull(binding.textInputEditText.getText()).toString().trim().length();
                binding.textInputLayout.setEndIconDrawable(size > 0 ? AppCompatResources.getDrawable(context, R.drawable.baseline_send_24) : AppCompatResources.getDrawable(context, R.drawable.baseline_keyboard_voice_24));
            }
            if (modelAdapter.getItemCount() == 1 && null != suggestions) suggestions.setTitleView(resultText);
        });
    }
    //=====
    private void setImageAdapter(Uri compressedUri, boolean isClearList) {
        if (isClearList) imageUris.clear();
        else if (null != compressedUri) imageUris.add(compressedUri);

        imageAdapter.setNewImage(imageUris, true);
    }
    //===method=============================================================
    private void resetList(){ forChangeTheme = null; }
    //=====
    @SuppressWarnings("all")
    private void saveDataFunc(boolean isPause) {
        forDefault = modelAdapter.saveData();// Save adapter data

        if (!forDefault.getStringUris().isEmpty() && !forDefault.getUserOrGemini().isEmpty()){
            if (forDefault.getTitle().isEmpty()) forDefault.setTitle(forDefault.getStringUris().get(0));
            else isClickByHistory = true;//只有新的記錄才會沒有title

            if (isPause) historyAdapter.setTargetTitle(forDefault.getTitle());//如果onPause時執行

            if (null != disposable) {// 在異步線程中從數據庫中查詢用戶信息
                disposable.dispose();
                disposable = null;
            }
            disposable = new CompositeDisposable();
            disposable.add(userRepository.getUserByTitle(forDefault.getTitle())
                    .subscribe(this::matchTitle));
        }
        else clearDataIfNecessary(true);
    }
    //=====
    private void matchTitle(User data, Throwable object1) {
        Date date = new Date();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date);
        User user = new User(forDefault.getTitle(), today, forDefault.getStringUris(), forDefault.getUserOrGemini(), forDefault.getImageHashMap(), false);

        if (data == null) saveDatabase("insert", user, "Insert Complete");// 如果用戶不存在，則插入新用戶信息
        else {
            if (isClickByHistory) { // 如果是來自歷史點擊，則更新用戶信息
                isClickByHistory = false;
                user.setId(data.getId()); // 設置現有用戶的 ID
                user.setPin(data.isPin());
                user.setTitle(data.getTitle());
                user.setDate(today);
                saveDatabase("update", user, "saveUpdate for history");
            }
            else {// 如果是在輸入框輸入的，如果有重複的title則合併新數據到現有的數據
                HashMap<Integer, List<Uri>> tempMap = new HashMap<>();
                int newSize = data.getStringUris().size();
                for (Map.Entry<Integer, List<Uri>> entry : user.getImageHashMap().entrySet()){
                    tempMap.put(newSize + entry.getKey(), entry.getValue());
                }

                data.getStringUris().addAll(user.getStringUris());
                data.getUserOrGemini().addAll(user.getUserOrGemini());
                data.getImageHashMap().putAll(tempMap);
                data.setDate(today);
                saveDatabase("update", data, "saveUpdate for repeat title");
            }
        }
    }
    //=====
    private void handleNextStep(boolean clear) {
        getSaveData();
        clearDataIfNecessary(clear);
    }
    //=====
    private void clearDataIfNecessary(boolean clear) {
        if (clear){
            resetList();
            User user = new User("", "", new ArrayList<>(), new ArrayList<>(), new HashMap<>(), false);
            modelAdapter.receiveDataAndShow(user);
            isClear = false;
        }
    }
    //=====
    private void getSaveData() {
        userRepository.getSaveData(userList -> {
            usersList = userList;
            historyAdapter.setSettingTitle(userList);
        });
    }
    //=====
    private void saveDatabase(String type, User user, String printText){
        userRepository.saveDatabase(type, user, printText, () -> handleNextStep(isClear));
    }
    //===build and send=====================================================
    @Override
    public void onImageListUpdated(List<Uri> updatedImageUris) { this.imageUris = updatedImageUris; }
    //=====
    @Override
    public void onChooseFlex(String text) { if (!text.isEmpty()) binding.textInputEditText.setText(text); }
    //=====
    @Override
    protected void onResume() {
        super.onResume();
        if (null != modelAdapter) modelAdapter.checkSharedPreferences();//讀取圖片和名字
        GenerativeModelManager.checkApiKey(this);//檢查有沒有apikey
        if (null !=navigationLayout) navigationLayout.getAndSetProfilePicture();//讀取圖片和名字
        getSaveData();
    }
    //=====
    @Override
    protected void onPause() {
        super.onPause();
        modelAdapter.ttsShutdown();
        saveDataFunc(true);
    }
    //=====
    @Override
    protected void onDestroy(){
        super.onDestroy();
        if (null == forChangeTheme) forChangeTheme = modelAdapter.saveData();
        if (null != suggestions) suggestions.resetHandler();
        if (null != disposable){
            disposable.dispose();
            disposable = null;
        }
        userRepository.disposeService();
    }
}