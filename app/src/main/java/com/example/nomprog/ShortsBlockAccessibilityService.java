package com.example.nomprog;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.List;
import java.util.Locale;

public class ShortsBlockAccessibilityService extends AccessibilityService {
    private static final String CHROME = "com.android.chrome";
    private long lastRedirectTime = 0;
    private String lastBlockedUrl = "";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null || event.getPackageName() == null) return;
        if (!CHROME.contentEquals(event.getPackageName())) return;
        if (!SiteStorage.isEnabled(this)) return;

        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return;

        try {
            List<AccessibilityNodeInfo> nodes =
                    root.findAccessibilityNodeInfosByViewId(
                            "com.android.chrome:id/url_bar");

            if (nodes == null || nodes.isEmpty()) return;

            AccessibilityNodeInfo bar = nodes.get(0);
            CharSequence text = bar.getText();

            if (text == null) text = bar.getContentDescription();
            if (text == null) return;

            String url = text.toString().trim().toLowerCase(Locale.ROOT);
            if (!url.contains(".")) return;
            if (!url.startsWith("http")) url = "https://" + url;

            if (!SiteStorage.matches(this, url)) return;

            long now = System.currentTimeMillis();
            if (url.equals(lastBlockedUrl) && now - lastRedirectTime < 5000) return;

            lastBlockedUrl = url;
            lastRedirectTime = now;
            SiteStorage.recordBlock(this, url);

            Intent intent = new Intent(this, BlockedActivity.class);
            intent.putExtra("site", url);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);

        } finally {
            root.recycle();
        }
    }

    @Override
    public void onInterrupt() {}
}
