package com.example.geminipro.Object;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import com.example.geminipro.R;
import com.example.geminipro.databinding.ActivityMainBinding;

public class ControlShowSuggestions {
    private Handler handler1;
    private final ActivityMainBinding binding;
    private final Context context;

    public ControlShowSuggestions(ActivityMainBinding binding, Context context) {
        this.binding = binding;
        this.context = context;
    }

    public void showSuggestions(boolean isShow) {
        binding.recyclerViewFlex.setVisibility((isShow) ? View.VISIBLE : View.GONE);
        binding.welcomeLayout.setVisibility((isShow) ? View.VISIBLE : View.GONE);

        if ((isShow)) {
            setTitleView("");
            binding.recyclerViewFlex.smoothScrollToPosition(0);
            binding.welcomeText.setText("");

            String text = context.getResources().getString(R.string.welcome_text);
            final int[] currentIndex = {0};

            if (null != handler1) handler1.removeCallbacksAndMessages(null);
            handler1 = new Handler();

            handler1.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (currentIndex[0] < text.length()) {
                        binding.welcomeText.append(String.valueOf(text.charAt(currentIndex[0])));
                        currentIndex[0]++;
                        handler1.postDelayed(this, 50);
                    }
                }
            }, 300);
        }
    }

    public void setTitleView(String text) {
        binding.textviewTitle.setText("");
        final int[] currentIndex = {0};

        if (null != handler1) handler1.removeCallbacksAndMessages(null);
        handler1 = new Handler();
        handler1.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (currentIndex[0] < text.length()) {
                    binding.textviewTitle.append(String.valueOf(text.charAt(currentIndex[0])));
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
