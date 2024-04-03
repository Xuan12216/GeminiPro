package com.example.geminipro.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.example.geminipro.Adapter.SettingsAdapter;
import com.example.geminipro.R;
import com.example.geminipro.databinding.ActivitySettingsBinding;

public class SettingsActivity extends AppCompatActivity {
    private ActivitySettingsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        boolean state = intent.getBooleanExtra("state", false);

        init(state);//true = moreFunction ; false = Setting
        setListener();
    }

    private void init(boolean state) {
        if (state) binding.textView.setText(R.string.moreFuncTitle);
        String[] settingTitle = getResources().getStringArray(state ? R.array.moreFuncItem : R.array.settingsItem);
        String[] settingIcon = getResources().getStringArray(state ? R.array.moreFuncIcon : R.array.settingsIcon);
        SettingsAdapter adapter = new SettingsAdapter(this, state);
        binding.recyclerViewSetting.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        binding.recyclerViewSetting.setAdapter(adapter);
        adapter.setSettingTitle(settingTitle,settingIcon);
    }

    private void setListener() {
        binding.backBtn.setOnClickListener(v -> finish());
    }
}