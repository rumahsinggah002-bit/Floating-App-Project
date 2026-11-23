package com.example.googlefloat;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.webkit.JavascriptInterface;
import android.widget.Toast;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.graphics.Color;
import android.view.Gravity;

public class MainActivity extends Activity {
    private static final int OVERLAY_REQUEST = 501;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(24, 40, 24, 24);
        root.setBackgroundColor(Color.rgb(246,249,255));

        WebView web = new WebView(this);
        web.getSettings().setJavaScriptEnabled(true);
        web.addJavascriptInterface(new HomeBridge(), "Android");
        web.loadUrl("file:///android_asset/index.html");

        root.addView(web, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);
    }

    private class HomeBridge {
        @JavascriptInterface public void startFloating() {
            runOnUiThread(() -> {
                if (!Settings.canDrawOverlays(MainActivity.this)) {
                    Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                    startActivityForResult(i, OVERLAY_REQUEST);
                    Toast.makeText(MainActivity.this, "Aktifkan izin tampil di atas aplikasi lain", Toast.LENGTH_LONG).show();
                } else startOverlay();
            });
        }
    }

    private void startOverlay() {
        Intent service = new Intent(this, FloatingService.class);
        if (Build.VERSION.SDK_INT >= 26) startForegroundService(service);
        else startService(service);
        Toast.makeText(this, "GOOGLE FLOAT aktif", Toast.LENGTH_SHORT).show();
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OVERLAY_REQUEST && Settings.canDrawOverlays(this)) startOverlay();
    }
}
