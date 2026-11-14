package com.fortnite.newspro;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private SwipeRefreshLayout swipeRefresh;
    private SharedPreferences prefs;
    private boolean isRooted = false;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences("FortniteProPrefs", MODE_PRIVATE);
        webView = findViewById(R.id.mainWebView);
        swipeRefresh = findViewById(R.id.swipeRefreshLayout);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setDatabaseEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);

        webView.addJavascriptInterface(new NativeBridge(), "AndroidNative");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                swipeRefresh.setRefreshing(false);
                syncStateToWeb();
            }
        });
        webView.setWebChromeClient(new WebChromeClient());

        swipeRefresh.setColorSchemeColors(0xFF3B82F6, 0xFF8B5CF6, 0xFFF59E0B);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                vibrateLight();
                webView.evaluateJavascript("if(window.refreshAllData){ refreshAllData(); }", null);
                swipeRefresh.setRefreshing(false);
            }
        });

        webView.loadUrl("file:///android_asset/index.html");

        // طلب صلاحيات الروت تلقائياً إذا كان مفعلاً في الإعدادات (مفعل افتراضياً)
        if (prefs.getBoolean("auto_root", true)) {
            requestMagiskRoot();
        }
    }

    private void requestMagiskRoot() {
        RootHelper.requestRoot(new RootHelper.RootCallback() {
            @Override
            public void onResult(final boolean granted, final String details) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        isRooted = granted;
                        syncStateToWeb();
                        if (granted) {
                            Toast.makeText(MainActivity.this, "⚡ تم تفعيل صلاحيات Magisk Root بنجاح", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    private void syncStateToWeb() {
        String lang = prefs.getString("lang", "ar");
        boolean autoRoot = prefs.getBoolean("auto_root", true);
        boolean haptics = prefs.getBoolean("haptics", true);
        boolean highQuality = prefs.getBoolean("high_quality", true);
        String deviceModel = Build.MANUFACTURER + " " + Build.MODEL;

        String js = String.format(
            "if(window.onNativeSync){ onNativeSync({isRooted:%b, lang:'%s', autoRoot:%b, haptics:%b, highQuality:%b, device:'%s'}); }",
            isRooted, lang, autoRoot, haptics, highQuality, deviceModel.replace("'", "")
        );
        webView.evaluateJavascript(js, null);
    }

    private void vibrateLight() {
        if (!prefs.getBoolean("haptics", true)) return;
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (v != null && v.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(25, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                v.vibrate(25);
            }
        }
    }

    public class NativeBridge {
        @JavascriptInterface
        public void triggerRoot() {
            vibrateLight();
            runOnUiThread(() -> requestMagiskRoot());
        }

        @JavascriptInterface
        public void setPullToRefreshEnabled(final boolean enabled) {
            runOnUiThread(() -> swipeRefresh.setEnabled(enabled));
        }

        @JavascriptInterface
        public void saveSetting(String key, String value) {
            vibrateLight();
            SharedPreferences.Editor editor = prefs.edit();
            if ("true".equals(value) || "false".equals(value)) {
                editor.putBoolean(key, Boolean.parseBoolean(value));
            } else {
                editor.putString(key, value);
            }
            editor.apply();
            runOnUiThread(() -> syncStateToWeb());
        }

        @JavascriptInterface
        public void hapticTap() {
            vibrateLight();
        }

        @JavascriptInterface
        public void shareText(String title, String body) {
            vibrateLight();
            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            sendIntent.setType("text/plain");
            sendIntent.putExtra(Intent.EXTRA_SUBJECT, title);
            sendIntent.putExtra(Intent.EXTRA_TEXT, title + "\n\n" + body + "\n\n— عبر تطبيق Fortnite News Pro");
            startActivity(Intent.createChooser(sendIntent, "مشاركة الخبر عبر"));
        }

        @JavascriptInterface
        public void clearAppCache() {
            vibrateLight();
            runOnUiThread(() -> {
                webView.clearCache(true);
                deleteDir(getCacheDir());
                Toast.makeText(MainActivity.this, "تم تنظيف الذاكرة المؤقتة بنجاح ✨", Toast.LENGTH_SHORT).show();
            });
        }

        @JavascriptInterface
        public void runRootBoost() {
            vibrateLight();
            if (!isRooted) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "يرجى منح صلاحيات الروت أولاً!", Toast.LENGTH_SHORT).show());
                return;
            }
            RootHelper.executeShellCommand("sync; echo 3 > /proc/sys/vm/drop_caches", new RootHelper.RootCallback() {
                @Override
                public void onResult(boolean granted, String details) {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "🚀 تم تحرير الرام وتسريع النظام بصلاحيات الروت!", Toast.LENGTH_SHORT).show());
                }
            });
        }
    }

    private boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            if (children != null) {
                for (String child : children) {
                    if (!deleteDir(new File(dir, child))) return false;
                }
            }
            return dir.delete();
        } else if (dir != null && dir.isFile()) {
            return dir.delete();
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        webView.evaluateJavascript("handleAndroidBack()", value -> {
            if ("false".equals(value)) {
                super.onBackPressed();
            }
        });
    }
}