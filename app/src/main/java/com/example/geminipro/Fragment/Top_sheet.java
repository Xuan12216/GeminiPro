package com.example.geminipro.Fragment;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.androidbolts.topsheet.TopSheetBehavior;
import com.example.geminipro.Adapter.HistoryAdapter;
import com.example.geminipro.Database.User;
import com.example.geminipro.databinding.TopSheetBinding;

import java.util.List;

public class Top_sheet extends CoordinatorLayout{
    private TopSheetBinding binding;
    private static sheetListener listener;
    public HistoryAdapter historyAdapter;
    private String funcType = "";
    public Top_sheet(@NonNull Context context) {
        super(context);
        init();
    }

    public Top_sheet(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public Top_sheet(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    private void init(){
        binding = TopSheetBinding.inflate(LayoutInflater.from(getContext()), this, true);

        TopSheetCallback callback = new TopSheetCallback();
        TopSheetBehavior.from(binding.topSheet).setTopSheetCallback(callback);
    }

    public void setFuncType(String type, HistoryAdapter.HistoryAdapterListener listener) {
        this.funcType = type;
        binding.recyclerViewTranslateHistory.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        historyAdapter = new HistoryAdapter(getContext(), funcType,listener);
        binding.recyclerViewTranslateHistory.setAdapter(historyAdapter);
    }

    public void setHistoryList(List<User> list) {
        historyAdapter.setSettingTitle(list);
    }

    public void show(){
        TopSheetBehavior.from(binding.topSheet).setState(TopSheetBehavior.STATE_EXPANDED);
    }

    public void hide() {
        TopSheetBehavior.from(binding.topSheet).setState(TopSheetBehavior.STATE_COLLAPSED);
    }

    //==============================

    public void setListener(sheetListener listener){
        Top_sheet.listener = listener;
    }

    public static class TopSheetCallback extends TopSheetBehavior.TopSheetCallback {

        @Override
        public void onStateChanged(@NonNull View bottomSheet, @TopSheetBehavior.State int newState) {
            if (null != listener) listener.onState(newState);
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset, @Nullable Boolean isOpening) {
        }
    }

    public interface sheetListener {
        void onState(int state);
    }
}
