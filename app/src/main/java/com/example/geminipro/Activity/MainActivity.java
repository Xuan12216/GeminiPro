package com.example.geminipro.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.Toast;
import com.example.geminipro.Adapter.FlexAdapter;
import com.example.geminipro.Adapter.HistoryAdapter;
import com.example.geminipro.Adapter.ImageAdapter;
import com.example.geminipro.Database.User;
import com.example.geminipro.Database.UserRepository;
import com.example.geminipro.Fragment.BottomSheet;
import com.example.geminipro.Model.GenerativeModelManager;
import com.example.geminipro.Object.MainLayout;
import com.example.geminipro.R;
import com.example.geminipro.Util.GeminiContentBuilder;
import com.example.geminipro.Util.PickImageFunc;
import com.example.geminipro.Util.PickImageUsingCamera;
import com.example.geminipro.Util.RecordFunc;
import com.example.geminipro.Util.Utils;
import com.example.geminipro.databinding.ActivityMainBinding;
import com.example.geminipro.enums.DBType;
import com.example.geminipro.enums.FuncType;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private MainLayout layout;
    private List<Uri> imageUris = new ArrayList<>();
    private Context context;
    private boolean isClear = false;
    private RecordFunc recordFunc;
    private PickImageFunc pickImageFunc;
    private PickImageUsingCamera pickImageUsingCamera;
    private UserRepository userRepository;
    private static User forChangeTheme;
    private List<User> usersList = new ArrayList<>();
    private final String funcType = FuncType.normal;
    private Integer index = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        context = this;
        layout = new MainLayout(binding, funcType, context);
        init();
        setListener();
    }

    //===init=====================================================

    private void init() {
        //welcome layout
        layout.setSuggestions();
        layout.showSuggestions(true);
        //database
        userRepository = new UserRepository(context, getLifecycle());
        //Adapter
        layout.initAdapter(historyAdapterListener, flexAdapterListener, imageAdapterListener);
        //解決轉換theme時會被重置的問題
        if (null != forChangeTheme){
            boolean isEmpty = forChangeTheme.getStringUris().isEmpty();
            if (layout.isHaveSuggestions()) layout.showSuggestions(isEmpty);
            if (!isEmpty && layout.isHaveSuggestions()) layout.setSuggestionsTitle(forChangeTheme.getTitle());
            layout.modelAdapter.receiveDataAndShow(forChangeTheme);
            index = layout.modelAdapter.getItemCount();
            resetList();
        }
        else GenerativeModelManager.initializeGenerativeModel(context);
        //語音轉文字
        recordFunc = new RecordFunc(this,context);
        //讀取圖庫 / 拍照
        pickImageFunc = new PickImageFunc(this, context);
        pickImageUsingCamera = new PickImageUsingCamera(this,context);
        //navigationLayout
        layout.setNavigationLayout();
    }

    //===listener=====================================================

    public void openNavigationDrawer(View view) {
        layout.historyAdapter.setSettingTitle(usersList);
        layout.searchEdittextSetText("");
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
        view.postDelayed(() -> layout.drawerLayoutIsOpenDrawer(true), 300);
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

    private void setListener() {
        layout.setDrawerLayoutListener();
        layout.setRecyclerViewListener();
        layout.setFabScrollToBottomListener();
        layout.setTextInputEditTextListener();
        layout.setEditTextEndIconListener(endIconOnClickListener);
        layout.setAddNoteListener(addNoteListener);
        layout.setSearchEdittextTextChangedListener(searchTextWatcher);
    }

    //=====

    public TextWatcher searchTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (!usersList.isEmpty()) {
                Utils.parallelSearch(context, usersList,false, s.toString().toLowerCase(), filteredUsers -> {
                    if (layout.historyAdapter != null) layout.historyAdapter.setSettingTitle(filteredUsers);
                });
            }
        }

        @Override
        public void afterTextChanged(Editable s) {}
    };

    //=====

    public View.OnClickListener endIconOnClickListener = v -> {
        String text = layout.getTextInputEditText();
        if (!layout.isWait && !text.isEmpty()) handleEndIconClick(text);
        else if (!layout.isWait) gotoRecordFunc();
    };

    //=====

    public View.OnClickListener addNoteListener = v -> {
        if (!layout.isWait){
            isClear = true;
            this.index = 0;
            int index = layout.modelAdapter.getItemCount();
            layout.flexAdapter.refreshTitle();
            layout.historyAdapter.setTargeId(-1);

            if (index != 0) {
                resetList();
                saveDataFunc(false);
            }
            if (layout.isHaveSuggestions()) layout.showSuggestions(true);
            Toast.makeText(context, R.string.add_notes_toast,Toast.LENGTH_SHORT).show();
        }
        else Toast.makeText(context, R.string.add_notes_toast1,Toast.LENGTH_SHORT).show();
    };

    //=====

    public HistoryAdapter.HistoryAdapterListener historyAdapterListener = new HistoryAdapter.HistoryAdapterListener() {
        @Override
        public void onChooseHistory(User user) {
            if (layout.isWait) {
                Toast.makeText(context, R.string.add_notes_toast1,Toast.LENGTH_SHORT).show();
                return;
            }
            new Handler().postDelayed(() -> layout.drawerLayoutIsOpenDrawer(false), 200);
            if (layout.isHaveSuggestions()) layout.showSuggestions(false);
            isClear = true;
            saveDataFunc(false);
            layout.historyAdapter.setTargeId(user.getId());

            layout.searchEdittextSetText("");
            if (null != layout.modelAdapter){
                layout.toggleProgressBar(true);

                new Handler().postDelayed(() -> {
                    index = user.getStringUris().size();
                    layout.modelAdapter.receiveDataAndShow(user);
                    layout.toggleProgressBar(false);
                    if (layout.isHaveSuggestions()) layout.setSuggestionsTitle(user.getTitle());
                }, 1000);

            }
        }

        @Override
        public void onChooseHistoryStatus(User user, String status, String oriName) {
            String currentTarget;

            switch (status) {
                case "pin":
                    saveDatabase(DBType.insert, user, "PinUpdate");
                    break;
                case "rename":
                    currentTarget = layout.historyAdapter.getTargetTitle();
                    if (null != oriName && null != currentTarget && !oriName.isEmpty()
                            && !currentTarget.isEmpty() && oriName.equals(currentTarget)){
                        isClear = true;
                        if (layout.isHaveSuggestions()) layout.showSuggestions(true);
                    }
                    saveDatabase(DBType.insert, user, "RenameUpdate");
                    break;
                case "delete":
                    currentTarget = layout.historyAdapter.getTargetTitle();
                    if (user.getTitle().equals(currentTarget)) {
                        isClear = true;
                        if (layout.isHaveSuggestions()) layout.showSuggestions(true);
                    }
                    saveDatabase(DBType.delete, user, "DeleteData");
                    break;
                default:
                    break;
            }
        }
    };

    //===prepare=====================================================

    private void handleEndIconClick(String text) {

        if (imageUris.isEmpty() && text.isEmpty()) return;
        if (Utils.isKeyboardOpen((Activity) context)) Utils.hideKeyboard((Activity) context);

        gotoGeminiBuilder(text, !imageUris.isEmpty() && !text.isEmpty());
        setModelAdapter(text, "user", true);//把資料設定到adapter
        if (layout.isHaveSuggestions()) layout.showSuggestions(false);
        setImageAdapter(null, true);
        layout.toggleProgressBar(true);
        layout.textInputEditTextSetText("");
        layout.textInputEditTextClearFocus();
        layout.isWait = true;
        layout.historyAdapter.setIsWait(true);
        layout.setEndIconDrawable(AppCompatResources.getDrawable(context, R.drawable.baseline_stop_circle_24));
    }

    //=====

    private void gotoRecordFunc() {
        recordFunc.startRecordFunc(result -> layout.textInputEditTextSetText(result));
    }

    //===build and send=====================================================

    private void gotoGeminiBuilder(String text, boolean isVision){
        GeminiContentBuilder builder = new GeminiContentBuilder(imageUris,context, getLifecycle());
        builder.startGeminiBuilder(text, isVision, (result, isFinish) -> setModelAdapter(result, "model", isFinish));
    }

    //===adapter for update data=====================================================

    public void setModelAdapter(String resultText, String who, boolean isFinish){
        runOnUiThread(() -> {
            layout.modelAdapter.addDataWithStreaming(resultText,imageUris, who, index, isFinish);
            if (isFinish) index++;
            layout.toggleProgressBar(false);
            layout.setRecyclerViewScrollToBottom();
            if ("model".equals(who) && isFinish){
                layout.isWait = false;
                layout.historyAdapter.setIsWait(false);
                int size = layout.getTextInputEditText().length();
                layout.setEndIconDrawable(size > 0 ? AppCompatResources.getDrawable(context, R.drawable.baseline_send_24) : AppCompatResources.getDrawable(context, R.drawable.baseline_keyboard_voice_24));
            }
            if (layout.modelAdapter.getItemCount() == 1 && layout.isHaveSuggestions()) layout.setSuggestionsTitle(resultText);
        });
    }

    //=====

    private void setImageAdapter(Uri compressedUri, boolean isClearList) {
        if (isClearList) imageUris.clear();
        else if (null != compressedUri) imageUris.add(compressedUri);

        layout.imageAdapter.setNewImage(imageUris, true);
    }

    //===method=============================================================

    private void resetList(){ forChangeTheme = new User(); }

    //=====

    private void saveDataFunc(boolean isPause) {
        User user = layout.modelAdapter.saveData();// Save adapter data

        if (!user.getStringUris().isEmpty() && !user.getUserOrGemini().isEmpty()){
            if (user.getTitle().isEmpty()) user.setTitle(user.getStringUris().get(0));

            if (isPause) {
                layout.historyAdapter.setTargeId(user.getId());//如果onPause時執行
            }

            Utils.setUserData(user, funcType, (type, printText, newUser) -> saveDatabase(type, newUser, printText));
        }
        else clearDataIfNecessary(true);
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
            layout.modelAdapter.receiveDataAndShow(new User());
            isClear = false;
        }
    }

    //=====

    private void getSaveData() {
        userRepository.getSaveData(userList -> {
            usersList = userList;
            layout.historyAdapter.setSettingTitle(userList);
        });
    }

    //=====

    private void saveDatabase(String type, User user, String printText){
        userRepository.saveDatabase(type, user, printText, () -> handleNextStep(isClear));
    }

    //===build and send=====================================================

    public ImageAdapter.ImageAdapterListener imageAdapterListener = updatedImageUris -> this.imageUris = updatedImageUris;

    //=====

    public FlexAdapter.FlexAdapterListener flexAdapterListener = text -> {
        if (!text.isEmpty()) layout.textInputEditTextSetText(text);
    };

    //=====

    @Override
    protected void onResume() {
        super.onResume();
        if (null != layout.modelAdapter) layout.modelAdapter.checkSharedPreferences();//讀取圖片和名字
        GenerativeModelManager.checkApiKey(this);//檢查有沒有apikey
        if (layout.isHaveNavigationLayout()) layout.navigationGetAndSetProfilePicture();//讀取圖片和名字
        getSaveData();
    }

    //=====

    @Override
    protected void onPause() {
        super.onPause();
        layout.modelAdapter.ttsShutdown();
        saveDataFunc(true);
    }

    //=====

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if (null == forChangeTheme) forChangeTheme = layout.modelAdapter.saveData();
        if (layout.isHaveSuggestions()) layout.resetSuggestionsHandler();
        userRepository.disposeService();
    }
}