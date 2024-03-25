package com.example.geminipro.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.os.Bundle;
import android.view.View;
import com.example.geminipro.Adapter.SettingsAdapter;
import com.example.geminipro.R;
import com.example.geminipro.databinding.ActivitySettingsBinding;

public class SettingsActivity extends AppCompatActivity {
    private ActivitySettingsBinding binding;
    private SettingsAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();
        setListener();
    }

    private void init() {
        String[] settingTitle = getResources().getStringArray(R.array.settingsItem);
        String[] settingIcon = getResources().getStringArray(R.array.settingsIcon);
        adapter = new SettingsAdapter(this);
        binding.recyclerViewSetting.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        binding.recyclerViewSetting.setAdapter(adapter);
        adapter.setSettingTitle(settingTitle,settingIcon);
    }

    private void setListener() {
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}