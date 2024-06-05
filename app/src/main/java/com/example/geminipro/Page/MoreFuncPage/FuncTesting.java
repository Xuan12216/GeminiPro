package com.example.geminipro.Page.MoreFuncPage;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.SearchView;

import com.example.geminipro.databinding.MoreFuncTestingBinding;

public class FuncTesting {
    private Context context;
    private Activity activity;
    private MoreFuncTestingBinding binding;

    public FuncTesting(Activity activity, Context context){

        this.context = context;
        this.activity = activity;
    }

    public MoreFuncTestingBinding startRunPage(){
        binding = MoreFuncTestingBinding.inflate(activity.getLayoutInflater());

        setData(binding);
        setListener(binding);
        return binding;
    }

    private void setListener(MoreFuncTestingBinding binding) {
    }

    private void setData(MoreFuncTestingBinding binding) {
    }
}
