package com.example.geminipro.Page.MoreFuncPage;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.geminipro.Adapter.HistoryAdapter;
import com.example.geminipro.Adapter.ImageAdapter;
import com.example.geminipro.Adapter.ModelAdapter;
import com.example.geminipro.Adapter.Spinner.CustomSpinnerAdapter;
import com.example.geminipro.Database.User;
import com.example.geminipro.Database.UserRepository;
import com.example.geminipro.Fragment.BottomSheet;
import com.example.geminipro.Model.GenerativeModelManager;
import com.example.geminipro.Object.ControlShowSuggestions;
import com.example.geminipro.R;
import com.example.geminipro.Util.CustomAnimation;
import com.example.geminipro.Util.GeminiContentBuilder;
import com.example.geminipro.Util.PickImageFunc;
import com.example.geminipro.Util.PickImageUsingCamera;
import com.example.geminipro.Util.RecordFunc;
import com.example.geminipro.Util.Utils;
import com.example.geminipro.databinding.MoreFuncTranslateBinding;
import com.example.geminipro.enums.DBType;
import com.example.geminipro.enums.FuncType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class FuncTranslate implements ImageAdapter.ImageAdapterListener{
    private Context context;
    private final Activity activity;
    private String spinner1Language = "", spinner2Language = "";
    private String[] languageArray;
    private MoreFuncTranslateBinding binding;
    private CustomAnimation customAnimation;
    private ModelAdapter modelAdapter;
    private List<Uri> imageUris = new ArrayList<>();
    private boolean isWait = false, isClickByHistory = false, isClear = false;;
    private ControlShowSuggestions suggestions;
    private ImageAdapter imageAdapter;
    private HistoryAdapter historyAdapter;
    private PickImageFunc pickImageFunc;
    private PickImageUsingCamera pickImageUsingCamera;
    private FragmentManager fragmentManager;
    private RecordFunc recordFunc;
    private Lifecycle lifecycle;
    private String funcType = FuncType.translate;
    private UserRepository userRepository;
    private List<User> usersList = new ArrayList<>();
    private User forDefault;
    private static User forChangeTheme;
    private HistoryAdapter.HistoryAdapterListener historyAdapterListener;

    public FuncTranslate(Activity activity, Context context, FragmentManager fragmentManager, Lifecycle lifecycle){

        this.context = context;
        this.activity = activity;
        this.fragmentManager = fragmentManager;
        this.lifecycle = lifecycle;
    }

    public MoreFuncTranslateBinding startRunPage(){
        binding = MoreFuncTranslateBinding.inflate(activity.getLayoutInflater());

        setData(binding);
        setListener(binding);
        return binding;
    }

    //=======================================
    //listener

    private void setListener(MoreFuncTranslateBinding binding) {
        historyAdapterListener = new HistoryAdapter.HistoryAdapterListener() {
            @Override
            public void onChooseHistory(User user) {
                if (isWait) {
                    Toast.makeText(context, R.string.add_notes_toast1,Toast.LENGTH_SHORT).show();
                    return;
                }

                binding.searchView.hide();
                if (null != suggestions) suggestions.showSuggestions(false);
                isClear = true;
                saveDataFunc(false);
                historyAdapter.setTargetTitle(user.getTitle());

                binding.translateTextInputEditText.setText("");
                if (null != modelAdapter){
                    binding.progressBar.setVisibility(View.VISIBLE);

                    new Handler().postDelayed(() -> {
                        modelAdapter.receiveDataAndShow(user);
                        binding.progressBar.setVisibility(View.GONE);
                        if (null != suggestions) suggestions.setTitleView(user.getTitle());
                        binding.newTranslationBtn.show();
                    }, 1000);

                }
            }

            @Override
            public void onChooseHistoryStatus(User user, String status, String oriName) {
                String currentTarget = "";

                switch (status) {
                    case "pin":
                        saveDatabase(DBType.update, user, "PinUpdate");
                        break;
                    case "rename":
                        currentTarget = historyAdapter.getTargetTitle();
                        if (null != oriName && null != currentTarget && !oriName.isEmpty()
                                && !currentTarget.isEmpty() && oriName.equals(currentTarget)){
                            isClear = true;
                            if (null != suggestions) suggestions.showSuggestions(true);
                        }
                        saveDatabase(DBType.update, user, "RenameUpdate");
                        break;
                    case "delete":
                        currentTarget = historyAdapter.getTargetTitle();
                        if (user.getTitle().equals(currentTarget)) {
                            isClear = true;
                            if (null != suggestions) suggestions.showSuggestions(true);
                        }
                        saveDatabase(DBType.delete, user, "DeleteData");
                        break;
                    default:
                        break;
                }
            }
        };
        if (null != historyAdapter) historyAdapter.setListener(historyAdapterListener);
        //=====
        binding.backBtn.setOnClickListener(v -> {
            if (activity != null) activity.finish();
        });
        //=====
        binding.translateRecyclerHistory.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (Utils.isKeyboardOpen((Activity) context)) Utils.hideKeyboard((Activity) context);
            }
        });
        //=====
        binding.searchView.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!usersList.isEmpty()) {
                    Utils.parallelSearch(context, usersList,false, s.toString().toLowerCase(), filteredUsers -> {
                        if (historyAdapter != null) historyAdapter.setSettingTitle(filteredUsers);
                    });
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
        //=====
        binding.spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                spinner1Language = parentView.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {}
        });

        binding.spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                spinner2Language = parentView.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {}
        });

        binding.ChangeLan1.setOnClickListener(changeClickListener);
        //=====
        binding.fabScrollToBottom.setOnClickListener((v) -> {
            int i = modelAdapter.getItemCount() - 1;
            if (i >= 0) binding.recyclerViewTranslate.smoothScrollToPosition(i);
        });
        //=====
        binding.recyclerViewTranslate.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                boolean canScrollDown = recyclerView.canScrollVertically(1);
                if (!canScrollDown) binding.fabScrollToBottom.hide();
                else binding.fabScrollToBottom.hide();

                if (dy > 0) binding.newTranslationBtn.shrink();//up
                else binding.newTranslationBtn.extend();//down
            }
        });
        //=====
        binding.newTranslationBtn.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            isClear = true;
            int index = modelAdapter.getItemCount();

            if (!isWait ) {
                if (index != 0) {
                    resetList();
                    saveDataFunc(false);
                    binding.newTranslationBtn.hide();
                }
                historyAdapter.setTargetTitle("");
                if (null != suggestions) suggestions.showSuggestions(true);
                Toast.makeText(context, R.string.add_notes_toast,Toast.LENGTH_SHORT).show();
            }
            else Toast.makeText(context, R.string.add_notes_toast1,Toast.LENGTH_SHORT).show();
        });
        //=====
        binding.imageCardView.setOnClickListener(v ->{
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
            bottomSheet.show(fragmentManager, bottomSheet.getTag());
        });
        //=====
        binding.translateTextInputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!isWait){
                    String text = s.toString().trim();
                    if (!text.isEmpty()) binding.translateTextInputLayout.setEndIconDrawable(AppCompatResources.getDrawable(context, R.drawable.baseline_send_24));
                    else binding.translateTextInputLayout.setEndIconDrawable(AppCompatResources.getDrawable(context, R.drawable.baseline_keyboard_voice_24));
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
        //=====
        binding.translateTextInputLayout.setEndIconOnClickListener(v -> {
            String text = Objects.requireNonNull(binding.translateTextInputEditText.getText()).toString().trim();
            if (!isWait && !text.isEmpty()) handleEndIconClick(text);
            else if (!isWait) gotoRecordFunc();
        });
    }

    private final View.OnClickListener changeClickListener = v -> {
        v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
        if (!spinner1Language.equals(languageArray[0]) && !spinner2Language.equals(languageArray[0])){
            String temp = spinner2Language;
            spinner2Language = spinner1Language;
            spinner1Language = temp;

            updateSpinnerSelection(); // 更新 Spinner 选中项
            customAnimation.swapAnimation(binding.spinnerCard1, binding.spinnerCard2);// 执行交换动画
        }
        else Toast.makeText(context, R.string.language_cannot_change, Toast.LENGTH_SHORT).show();
    };

    @Override
    public void onImageListUpdated(List<Uri> updatedImageUris) { this.imageUris = updatedImageUris; }

    //===========================
    //init

    private void setData(MoreFuncTranslateBinding binding) {
        //database
        userRepository = new UserRepository(context, lifecycle);
        //=====
        binding.newTranslationBtn.hide();
        //=====
        recordFunc = new RecordFunc((ComponentActivity) activity, context);
        //=====
        pickImageFunc = new PickImageFunc((ComponentActivity) activity, context);
        pickImageUsingCamera = new PickImageUsingCamera((ComponentActivity) activity,context);
        //=====
        imageAdapter = new ImageAdapter(context, this);
        binding.recyclerViewImage.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerViewImage.setAdapter(imageAdapter);
        //=====
        historyAdapter = new HistoryAdapter(context, funcType,historyAdapterListener);
        binding.translateRecyclerHistory.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        binding.translateRecyclerHistory.setAdapter(historyAdapter);
        //=====
        suggestions = new ControlShowSuggestions(context);
        suggestions.setSuggestionView(null, binding.welcomeLayout, binding.welcomeText,
                context.getResources().getString(R.string.translate_welcome_text));
        suggestions.showSuggestions(true);
        //=====
        customAnimation = new CustomAnimation(context);
        binding.spinner1.setTitle(context.getString(R.string.spinner1_title));
        binding.spinner2.setTitle(context.getString(R.string.spinner2_title));
        binding.spinner1.setPositiveButton(context.getString(R.string.spinner_btn));
        binding.spinner2.setPositiveButton(context.getString(R.string.spinner_btn));

        languageArray = context.getResources().getStringArray(R.array.languages);
        List<String> tempList1 = new ArrayList<>(Arrays.asList(languageArray));
        List<String> tempList2 = new ArrayList<>(Arrays.asList(languageArray));
        tempList2.remove(0);

        CustomSpinnerAdapter adapter1 = new CustomSpinnerAdapter(context, R.layout.spinner_item, tempList1);
        CustomSpinnerAdapter adapter2 = new CustomSpinnerAdapter(context, R.layout.spinner_item, tempList2);

        binding.spinner1.setAdapter(adapter1);
        binding.spinner2.setAdapter(adapter2);
        //==========
        modelAdapter = new ModelAdapter(context);
        modelAdapter.setIsShowSoundAndGoogle(true);
        binding.recyclerViewTranslate.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        binding.recyclerViewTranslate.setAdapter(modelAdapter);
        //=====
        binding.searchView.setAutoShowKeyboard(false);
    }

    //==============================
    //Method

    private void updateSpinnerSelection() {
        ArrayAdapter<String> adapter1 = (ArrayAdapter<String>) binding.spinner1.getAdapter();
        ArrayAdapter<String> adapter2 = (ArrayAdapter<String>) binding.spinner2.getAdapter();
        int index1 = adapter1.getPosition(spinner1Language);
        int index2 = adapter2.getPosition(spinner2Language);
        binding.spinner1.setSelection(index1);
        binding.spinner2.setSelection(index2);
    }

    public void setModelAdapter(String resultText, String who){
        activity.runOnUiThread(() -> {
            binding.newTranslationBtn.show();
            modelAdapter.addData(resultText,imageUris, who, modelAdapter.getItemCount());
            binding.progressBar.setVisibility(View.GONE);
            binding.recyclerViewTranslate.smoothScrollToPosition(modelAdapter.getItemCount());
            if ("model".equals(who)){
                isWait = false;
                int size = Objects.requireNonNull(binding.translateTextInputEditText.getText()).toString().trim().length();
                binding.translateTextInputLayout.setEndIconDrawable(size > 0 ? AppCompatResources.getDrawable(context, R.drawable.baseline_send_24) : AppCompatResources.getDrawable(context, R.drawable.baseline_keyboard_voice_24));
            }
        });
    }

    private void setImageAdapter(Uri compressedUri, boolean isClearList) {
        if (isClearList) imageUris.clear();
        else if (null != compressedUri) imageUris.add(compressedUri);

        imageAdapter.setNewImage(imageUris, true);
    }

    private void gotoRecordFunc() {
        recordFunc.startRecordFunc(result -> binding.translateTextInputEditText.setText(result));
    }

    private void handleEndIconClick(String text) {

        if (imageUris.isEmpty() && text.isEmpty()) return;
        if (Utils.isKeyboardOpen(activity)) Utils.hideKeyboard(activity);

        String title1 = context.getString(R.string.spinner1_title);
        String title2 = context.getString(R.string.spinner2_title);
        String translate_context = context.getString(R.string.translate_context1);
        String finalText = translate_context + " " + title1 + ": " + spinner1Language + " \u2192 " + title2 + "： " + spinner2Language + ", Content : " + text;
        String modelText = spinner1Language + " \u2192 " + spinner2Language + "\n" + title1 + ": " + text;

        gotoGeminiBuilder(finalText, !imageUris.isEmpty() && !text.isEmpty());
        setModelAdapter(modelText, "user");//把資料設定到adapter
        if (null != suggestions) suggestions.showSuggestions(false);
        setImageAdapter(null, true);
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.translateTextInputEditText.setText("");
        binding.translateTextInputEditText.clearFocus();
        isWait = true;
        binding.translateTextInputLayout.setEndIconDrawable(AppCompatResources.getDrawable(context, R.drawable.baseline_stop_circle_24));
    }

    private void gotoGeminiBuilder(String text, boolean isVision){
        GeminiContentBuilder builder = new GeminiContentBuilder(imageUris,context, lifecycle);
        builder.startGeminiBuilder(text, isVision, result -> setModelAdapter(result, "model"));
    }

    //database========================

    private void getSaveData() {
        userRepository.getSaveData(userList -> {
            usersList = userList;
            historyAdapter.setSettingTitle(userList);
        });
    }

    private void saveDataFunc(boolean isPause) {
        forDefault = modelAdapter.saveData();// Save adapter data

        if (!forDefault.getStringUris().isEmpty() && !forDefault.getUserOrGemini().isEmpty()){
            if (forDefault.getTitle().isEmpty()) forDefault.setTitle(forDefault.getStringUris().get(0));
            else isClickByHistory = true;//只有新的記錄才會沒有title
            if (isPause) {
                historyAdapter.setTargetTitle(forDefault.getTitle());//如果onPause時執行
                isClickByHistory = true;
            }

            Utils.parallelSearch(context, usersList,true, forDefault.getTitle().toLowerCase(), filteredUsers -> {
                boolean isMatch = false;
                for (User user : filteredUsers){
                    String title = user.getTitle();
                    if (!title.isEmpty() && title.equals(forDefault.getTitle())) {
                        isMatch = true;
                        matchTitle(user);
                        break;
                    }
                }
                if (!isMatch) matchTitle(null);
            });
        }
        else clearDataIfNecessary(true);
    }

    private void matchTitle(User matchData) {
        Utils.matchTitle(matchData, forDefault, isClickByHistory, funcType, (type, printText, user)
                -> saveDatabase(type, user, printText));

        if (isClickByHistory) isClickByHistory = false;
    }

    private void saveDatabase(String type, User user, String printText){
        userRepository.saveDatabase(type, user, printText, () -> handleNextStep(isClear));
    }

    private void handleNextStep(boolean clear) {
        getSaveData();
        clearDataIfNecessary(clear);
    }

    private void clearDataIfNecessary(boolean clear) {
        if (clear){
            resetList();
            User user = new User("", "", new ArrayList<>(), new ArrayList<>(), new HashMap<>(), false, funcType);
            modelAdapter.receiveDataAndShow(user);
            isClear = false;
        }
    }

    private void resetList(){ forChangeTheme = null; }

    //================================

    public void onResume() {
        if (null != modelAdapter) modelAdapter.checkSharedPreferences();//讀取圖片和名字
        GenerativeModelManager.checkApiKey(context);//檢查有沒有apikey
        getSaveData();
        if (null != historyAdapter) historyAdapter.setTargetTitle("");
    }

    public void onPause() {
        modelAdapter.ttsShutdown();
        saveDataFunc(true);
    }

    public void onDestroy() {
        if (null == forChangeTheme) forChangeTheme = modelAdapter.saveData();
        if (null != suggestions) suggestions.resetHandler();
        userRepository.disposeService();
    }
}
