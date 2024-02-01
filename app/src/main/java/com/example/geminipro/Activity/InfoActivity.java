package com.example.geminipro.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.example.geminipro.R;
import com.example.geminipro.databinding.ActivityInfoBinding;

public class InfoActivity extends AppCompatActivity {
    private ActivityInfoBinding binding;
    private String url;
    private int currentNightMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        webviewSetting();
        setListener();
        loadUrl();
    }

    private void loadUrl() {
        binding.infoWebview.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {}

            @Override
            public void onPageFinished(WebView view, String url) {
                binding.infoProgress.setVisibility(View.GONE);
                String message = getString(R.string.info_load_fail);
                if (view.getProgress() == 100) message = getString(R.string.info_load_success);

                binding.infoSwipeRefreshLayout.setRefreshing(false);
                Toast.makeText(InfoActivity.this, message, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                binding.infoProgress.setVisibility(View.GONE);
                String errorMessage = getString(R.string.info_load_fail);
                binding.infoSwipeRefreshLayout.setRefreshing(false);
                Toast.makeText(InfoActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        binding.infoWebview.loadUrl(url);
    }

    private void setListener() {
        binding.infoSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                binding.infoWebview.reload();
            }
        });
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void webviewSetting() {
        WebSettings webSettings = binding.infoWebview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        currentNightMode = getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;

        url = getResources().getString(R.string.info_url);
        binding.infoProgress.setVisibility(View.VISIBLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) binding.infoWebview.getSettings().setForceDark(WebSettings.FORCE_DARK_AUTO);
            else binding.infoWebview.getSettings().setForceDark(WebSettings.FORCE_DARK_OFF);
        }
    }
}