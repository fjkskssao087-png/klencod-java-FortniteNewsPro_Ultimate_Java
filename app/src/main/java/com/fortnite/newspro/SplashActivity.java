package com.fortnite.newspro;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.webkit.WebSettings;
import android.webkit.WebView;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION_MS = 5000; // 5 ثواني بالضبط

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        WebView splashWeb = findViewById(R.id.splashWebView);
        WebSettings settings = splashWeb.getSettings();
        settings.setJavaScriptEnabled(true);
        splashWeb.setBackgroundColor(0x00000000);
        splashWeb.loadUrl("file:///android_asset/splash.html");

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            }
        }, SPLASH_DURATION_MS);
    }

    @Override
    public void onBackPressed() {
        // منع الرجوع أثناء شاشة الدخول السينمائية
    }
}
