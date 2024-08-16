package com.example.geminipro.Object;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.geminipro.Adapter.FlexAdapter;
import com.example.geminipro.Adapter.HistoryAdapter;
import com.example.geminipro.Adapter.ImageAdapter;
import com.example.geminipro.Adapter.ModelAdapter;
import com.example.geminipro.R;
import com.example.geminipro.Util.Utils;
import com.example.geminipro.databinding.ActivityMainBinding;
import java.util.Objects;

public class MainLayout {
    private final ActivityMainBinding binding;
    private final Context context;
    private ControlShowSuggestions suggestions;
    public ModelAdapter modelAdapter;
    public HistoryAdapter historyAdapter;
    public FlexAdapter flexAdapter;
    public ImageAdapter imageAdapter;
    private final String funcType;
    private NavigationLayout navigationLayout;
    public boolean isWait = false;

    public MainLayout(ActivityMainBinding binding, String funcType, Context context) {
        this.binding = binding;
        this.funcType = funcType;
        this.context = context;
    }

    //=====

    public void setSuggestions() {
        suggestions = new ControlShowSuggestions(context);
        suggestions.setSuggestionView(binding.recyclerViewFlex, binding.welcomeLayout
                , binding.welcomeText, context.getResources().getString(R.string.welcome_text));
        suggestions.setTitleObject(binding.textviewTitle);
    }

    public void setSuggestionsTitle(String title){
        suggestions.setTitleView(title);
    }

    public void showSuggestions(boolean isShow) {
        suggestions.showSuggestions(isShow);
    }

    public boolean isHaveSuggestions() {
        return null != suggestions;
    }

    public void resetSuggestionsHandler() {
        suggestions.resetHandler();
    }

    //=====

    public void toggleProgressBar(boolean show) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    //=====

    public void initAdapter(HistoryAdapter.HistoryAdapterListener hisListener, FlexAdapter.FlexAdapterListener fleListener, ImageAdapter.ImageAdapterListener imgListener) {
        modelAdapter = new ModelAdapter(context);
        initRecyclerView(binding.recyclerView, modelAdapter, new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));

        imageAdapter = new ImageAdapter(context, imgListener);
        initRecyclerView(binding.recyclerViewDown, imageAdapter, new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));

        flexAdapter = new FlexAdapter(context, fleListener);
        String[] title = context.getResources().getStringArray(R.array.flexboxItem);
        flexAdapter.setSettingTitle(title);
        initRecyclerView(binding.recyclerViewFlex, flexAdapter, new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));

        historyAdapter = new HistoryAdapter(context, funcType, hisListener);
        initRecyclerView(binding.recyclerViewHistory, historyAdapter, new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
    }

    private void initRecyclerView(RecyclerView recyclerView, RecyclerView.Adapter adapter, RecyclerView.LayoutManager layoutManager) {
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    public void setRecyclerViewScrollToPosition(int pos) {
        binding.recyclerView.smoothScrollToPosition(pos);
    }

    public void setRecyclerViewScrollToBottom() {
        binding.recyclerView.post(() -> {
            int itemCount = modelAdapter.getItemCount();
            if (itemCount > 0) {
                int lastItemPosition = itemCount - 1;
                RecyclerView.LayoutManager layoutManager = binding.recyclerView.getLayoutManager();
                assert layoutManager != null;
                View lastItemView = layoutManager.findViewByPosition(lastItemPosition);

                if (lastItemView != null) {
                    int scrollOffset = lastItemView.getBottom() - binding.recyclerView.getHeight();
                    if (scrollOffset > 0) {
                        binding.recyclerView.smoothScrollBy(0, scrollOffset);
                    }
                }
            }
        });
    }

    public void setRecyclerViewListener() {
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
                if (Utils.isKeyboardOpen((Activity) context)) Utils.hideKeyboard((Activity) context);
            }
        });
    }

    public void setFabScrollToBottomListener() {
        binding.fabScrollToBottom.setOnClickListener((v) -> {
            int i = modelAdapter.getItemCount() - 1;
            if (i >= 0) binding.recyclerView.smoothScrollToPosition(i);
        });
    }

    //=====

    public void setNavigationLayout() {
        navigationLayout = new NavigationLayout(binding, context);
    }

    public boolean isHaveNavigationLayout() {
        return null != navigationLayout;
    }

    public void navigationGetAndSetProfilePicture() {
        navigationLayout.getAndSetProfilePicture();
    }

    //=====

    public void drawerLayoutIsOpenDrawer(boolean isOpen) {
        if (isOpen) binding.drawerLayout.openDrawer(GravityCompat.START);
        else binding.drawerLayout.closeDrawer(GravityCompat.START);
    }

    public void setDrawerLayoutListener() {
        binding.drawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
                if (Utils.isKeyboardOpen((Activity) context)) Utils.hideKeyboard((Activity) context);
            }
        });
    }

    //=====

    public void searchEdittextSetText(String text) {
        binding.searchEdittext.setText(text);
    }

    public void textInputEditTextSetText(String text) {
        binding.textInputEditText.setText(text);
    }

    public void textInputEditTextClearFocus() {
        binding.textInputEditText.clearFocus();
    }

    public void setTextInputEditTextListener() {
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
    }

    public void setEditTextEndIconListener(View.OnClickListener endIconOnClickListener) {
        binding.textInputLayout.setEndIconOnClickListener(endIconOnClickListener);
    }

    public void setAddNoteListener(View.OnClickListener addNoteListener) {
        binding.addNote.setOnClickListener(addNoteListener);
    }

    public void setSearchEdittextTextChangedListener(TextWatcher searchTextWatcher) {
        binding.searchEdittext.addTextChangedListener(searchTextWatcher);
    }

    public String getTextInputEditText() {
        return Objects.requireNonNull(binding.textInputEditText.getText()).toString().trim();
    }

    public void setEndIconDrawable(Drawable drawable) {
        binding.textInputLayout.setEndIconDrawable(drawable);
    }
}
