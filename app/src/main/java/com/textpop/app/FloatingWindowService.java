package com.textpop.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;
import androidx.core.app.NotificationCompat;

public class FloatingWindowService extends Service {

    private static final String CHANNEL_ID = "textpop_channel";
    private WindowManager windowManager;
    private View floatingView;
    private boolean isShowing = false;
    private String pendingText = "";

    // Singleton reference so AccessibilityService can call it
    public static FloatingWindowService instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        createNotificationChannel();
        startForeground(1, buildNotification());
        setupFloatingView();
    }

    private void setupFloatingView() {
        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_menu, null);

        int layoutFlag;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutFlag = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutFlag = WindowManager.LayoutParams.TYPE_PHONE;
        }

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layoutFlag,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 100;
        params.y = 200;

        // Copy button
        Button btnCopy = floatingView.findViewById(R.id.btn_copy);
        btnCopy.setOnClickListener(v -> {
            if (!pendingText.isEmpty()) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("TextPop", pendingText);
                if (clipboard != null) {
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(this, "✓ Copied", Toast.LENGTH_SHORT).show();
                }
            }
            hideMenu();
        });

        // Google button
        Button btnGoogle = floatingView.findViewById(R.id.btn_google);
        btnGoogle.setOnClickListener(v -> {
            if (!pendingText.isEmpty()) {
                String url = "https://www.google.com/search?q=" + Uri.encode(pendingText);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
            hideMenu();
        });

        // MV button
        Button btnMv = floatingView.findViewById(R.id.btn_mv);
        btnMv.setOnClickListener(v -> {
            if (!pendingText.isEmpty()) {
                String url = "https://missav.live/en/search/" + Uri.encode(pendingText);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
            hideMenu();
        });

        // Outside touch — hide menu
        floatingView.setOnClickListener(v -> hideMenu());

        floatingView.setVisibility(View.GONE);
        windowManager.addView(floatingView, params);
    }

    public void showMenu(String selectedText, int x, int y) {
        if (selectedText == null || selectedText.trim().isEmpty()) return;
        pendingText = selectedText.trim();

        floatingView.post(() -> {
            if (windowManager == null || floatingView == null) return;

            WindowManager.LayoutParams params =
                    (WindowManager.LayoutParams) floatingView.getLayoutParams();

            // Position above/below selection point
            int screenHeight = getResources().getDisplayMetrics().heightPixels;
            params.x = Math.max(8, Math.min(x - 120, getResources().getDisplayMetrics().widthPixels - 260));
            params.y = y > screenHeight / 2 ? Math.max(0, y - 160) : y + 60;

            if (!isShowing) {
                floatingView.setVisibility(View.VISIBLE);
                isShowing = true;
            }
            windowManager.updateViewLayout(floatingView, params);
        });
    }

    public void hideMenu() {
        if (floatingView != null && isShowing) {
            floatingView.setVisibility(View.GONE);
            isShowing = false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        instance = null;
        if (floatingView != null && windowManager != null) {
            windowManager.removeView(floatingView);
        }
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "TextPop Service", NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Keeps the text selection popup running");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    private Notification buildNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("✂️ TextPop Active")
                .setContentText("Select any text to Copy / Google / MV")
                .setSmallIcon(android.R.drawable.ic_menu_edit)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }
}
