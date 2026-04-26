package com.textpop.app;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.os.Build;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextSelectionAccessibilityService extends AccessibilityService {

    // JAV code regex pattern (ported from your userscript)
    private static final Pattern JAV_PATTERN = Pattern.compile(
            "FC2[-_]?PPV[-_]?(?:\\d{5,8}[A-Z]?)" +
            "|HEYZO[-_]?\\d{4}" +
            "|(?:CARIBBEANCOM|CARIB)[-_]\\d{6}[-_]\\d{3}" +
            "|\\d{6}[-_]\\d{3}[-_](?:CARIBBEANCOM|CARIB)" +
            "|(?:1PONDO|1PON)[-_]\\d{6}[-_]\\d{3}" +
            "|\\d{6}[-_]\\d{3}[-_](?:1PONDO|1PON)" +
            "|(?:10MUSUME|10MU)[-_]\\d{6}[-_]\\d{3}" +
            "|\\d{6}[-_]\\d{3}[-_](?:10MUSUME|10MU)" +
            "|H4610[-_]\\d{6}[-_]\\d{3}" +
            "|H0930[-_]\\d{6}[-_]\\d{3}" +
            "|(?:PACOPACOMAMA|PACO)[-_]\\d{6}[-_]\\d{3}" +
            "|legsjapan[-_]\\d{3,5}" +
            "|(?<!\\d)\\d{6}[-_]\\d{3}(?!\\d)" +
            "|(?<![A-Za-z])n\\d{4}(?!\\d)" +
            "|h_\\d{1,5}[a-z]{2,8}\\d{3,5}" +
            "|parathd[-_]?\\d{3,5}" +
            "|KIN8[-_]?\\d{3,5}" +
            "|[A-Z]{2,6}VR[-_]?\\d{3,5}" +
            "|[A-Z]{5,6}[-_]\\d{2,5}" +
            "|[A-Z]{4}[-_]\\d{2,5}" +
            "|[A-Z]{3}[-_]\\d{2,5}" +
            "|[A-Z]{2}[-_]\\d{3,5}" +
            "|[A-Z]{3,6}\\d{3,5}[A-Z]?",
            Pattern.CASE_INSENSITIVE
    );

    private int lastX = 200, lastY = 400;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null) return;

        int eventType = event.getEventType();

        // Track touch position from window state changes
        if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            // Reset when app changes
            if (FloatingWindowService.instance != null) {
                FloatingWindowService.instance.hideMenu();
            }
            return;
        }

        // Text selection changed
        if (eventType == AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED) {
            AccessibilityNodeInfo source = event.getSource();
            if (source == null) return;

            CharSequence text = source.getText();
            if (text == null || text.length() == 0) {
                if (FloatingWindowService.instance != null) {
                    FloatingWindowService.instance.hideMenu();
                }
                source.recycle();
                return;
            }

            int selStart = source.getTextSelectionStart();
            int selEnd = source.getTextSelectionEnd();

            if (selStart < 0 || selEnd < 0 || selStart >= selEnd) {
                if (FloatingWindowService.instance != null) {
                    FloatingWindowService.instance.hideMenu();
                }
                source.recycle();
                return;
            }

            String selectedText = text.toString().substring(
                    Math.min(selStart, text.length()),
                    Math.min(selEnd, text.length())
            ).trim();

            if (!selectedText.isEmpty()) {
                // Get position from node bounds
                android.graphics.Rect bounds = new android.graphics.Rect();
                source.getBoundsInScreen(bounds);
                int x = bounds.centerX();
                int y = bounds.bottom;
                lastX = x;
                lastY = y;

                ensureServiceRunning();
                if (FloatingWindowService.instance != null) {
                    FloatingWindowService.instance.showMenu(selectedText, x, y);
                }
            }

            source.recycle();
            return;
        }

        // Window content changed — check for selection in the focused node
        if (eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            // Hide if nothing selected (handled by selectionchange above mostly)
        }
    }

    /**
     * Try to detect a JAV code in the given text near a cursor offset.
     * Returns the best matching code or null.
     */
    public static String findJavCode(String text) {
        if (text == null || text.isEmpty()) return null;
        Matcher m = JAV_PATTERN.matcher(text);
        if (m.find()) return m.group();
        return null;
    }

    private void ensureServiceRunning() {
        if (FloatingWindowService.instance == null) {
            Intent intent = new Intent(this, FloatingWindowService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent);
            } else {
                startService(intent);
            }
        }
    }

    @Override
    public void onInterrupt() {
        if (FloatingWindowService.instance != null) {
            FloatingWindowService.instance.hideMenu();
        }
    }
}
