package com.textpop.app;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatus();
    }

    private void setupUI() {
        Button btnOverlay = findViewById(R.id.btn_overlay_permission);
        Button btnAccessibility = findViewById(R.id.btn_accessibility);

        btnOverlay.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        });

        btnAccessibility.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
        });
    }

    private void updateStatus() {
        TextView tvStatus = findViewById(R.id.tv_status);
        boolean overlayOk = Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                || Settings.canDrawOverlays(this);
        boolean accessibilityOk = isAccessibilityEnabled();

        String status = "✂️ TextPop Status\n\n";
        status += overlayOk ? "✅ Floating window: ON\n" : "❌ Floating window: OFF (tap button below)\n";
        status += accessibilityOk ? "✅ Text selection detection: ON\n" : "❌ Text detection: OFF (tap button below)\n";

        if (overlayOk && accessibilityOk) {
            status += "\n🎉 All set! Select any text in any app to see the popup.";
        } else {
            status += "\n⚠️ Please enable both permissions above.";
        }

        tvStatus.setText(status);

        // Start the floating service if permissions granted
        if (overlayOk) {
            Intent serviceIntent = new Intent(this, FloatingWindowService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
        }
    }

    private boolean isAccessibilityEnabled() {
        AccessibilityManager am = (AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE);
        if (am == null) return false;
        List<AccessibilityServiceInfo> enabledServices =
                am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);
        for (AccessibilityServiceInfo info : enabledServices) {
            if (info.getId().contains(getPackageName())) return true;
        }
        return false;
    }
}
