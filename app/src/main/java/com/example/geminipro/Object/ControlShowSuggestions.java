package com.example.geminipro.Object;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.geminipro.R;

public class ControlShowSuggestions {
    private Handler handler1;
    private final Context context;
    private RecyclerView recyclerViewFlex;
    private View welcomeLayout;
    private TextView welcomeText, textviewTitle;
    private String suggestionTitle;

    public ControlShowSuggestions(Context context) {
        this.context = context;
    }

    public void setSuggestionView(RecyclerView rv, View v, TextView tv, String t){
        this.recyclerViewFlex = rv;
        this.welcomeLayout = v;
        this.welcomeText = tv;
        this.suggestionTitle = t;
    }

    public void showSuggestions(boolean isShow) {
        if (null != recyclerViewFlex) recyclerViewFlex.setVisibility((isShow) ? View.VISIBLE : View.GONE);
        if (null != welcomeLayout) welcomeLayout.setVisibility((isShow) ? View.VISIBLE : View.GONE);

        if ((isShow) && null != suggestionTitle && !suggestionTitle.isEmpty()) {
            setTitleView("");
            if (null != recyclerViewFlex) recyclerViewFlex.smoothScrollToPosition(0);
            if (null != welcomeText) welcomeText.setText("");

            String text = suggestionTitle;
            final int[] currentIndex = {0};

            if (null != handler1) handler1.removeCallbacksAndMessages(null);
            handler1 = new Handler();

            handler1.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (currentIndex[0] < text.length()) {
                        if (null != welcomeText) welcomeText.append(String.valueOf(text.charAt(currentIndex[0])));
                        currentIndex[0]++;
                        handler1.postDelayed(this, 50);
                    }
                }
            }, 300);
        }
    }

    public void setTitleObject(TextView textView){
        this.textviewTitle = textView;
    }

    public void setTitleView(String text) {
        if (null != textviewTitle) textviewTitle.setText("");
        final int[] currentIndex = {0};

        if (null != handler1) handler1.removeCallbacksAndMessages(null);
        handler1 = new Handler();
        handler1.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (currentIndex[0] < text.length()) {
                    if (null != textviewTitle) textviewTitle.append(String.valueOf(text.charAt(currentIndex[0])));
                    currentIndex[0]++;
                    handler1.postDelayed(this, 50);
                }
            }
        }, 300);
    }

    public void resetHandler(){
        if (null != handler1) handler1.removeCallbacksAndMessages(null);
        handler1 = null;
    }
}
