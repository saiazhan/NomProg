
package com.example.nomprog;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.net.Uri;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ShortsBlockAccessibilityService
        extends AccessibilityService {

    private static final String CHROME = "com.android.chrome";
    private static final String BLOCK_PAGE = "https://www.google.com/";

    private long lastRedirectTime = 0;
    private String lastBlockedUrl = "";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        if (event == null || event.getPackageName() == null) {
            return;
        }

        if (!CHROME.contentEquals(event.getPackageName())) {
            return;
        }

        if (!SiteStorage.isEnabled(this)) {
            return;
        }

        AccessibilityNodeInfo root = getRootInActiveWindow();

        if (root == null) {
            return;
        }

        try {
            List<AccessibilityNodeInfo> nodes =
                    root.findAccessibilityNodeInfosByViewId(
                            "com.android.chrome:id/url_bar"
                    );

            if (nodes == null || nodes.isEmpty()) {
                return;
            }

            AccessibilityNodeInfo urlBar = nodes.get(0);

            if (urlBar.getText() == null) {
                return;
            }

            String url = urlBar.getText().toString()
                    .trim()
                    .toLowerCase(Locale.ROOT);

            Uri uri = Uri.parse(
                    url.startsWith("http") ? url : "https://" + url
            );

            String host = uri.getHost();
            String path = uri.getPath();

            if (host == null) {
                return;
            }

            if (path == null) {
                path = "/";
            }

            ArrayList<String> sites = SiteStorage.getSites(this);

            boolean blocked = false;

            for (String site : sites) {

                Uri blockedUri = Uri.parse("https://" + site);

                String blockedHost = blockedUri.getHost();
                String blockedPath = blockedUri.getPath();

                if (blockedHost == null) {
                    continue;
                }

                if (blockedPath == null) {
                    blockedPath = "/";
                }

                boolean hostMatch =
                        host.equals(blockedHost) ||
                        host.endsWith("." + blockedHost);

                boolean pathMatch =
                        blockedPath.equals("/") ||
                        path.equals(blockedPath) ||
                        path.startsWith(blockedPath + "/");

                if (hostMatch && pathMatch) {
                    blocked = true;
                    break;
                }
            }

            if (!blocked) {
                return;
            }

            long now = System.currentTimeMillis();

            if (url.equals(lastBlockedUrl)
                    && now - lastRedirectTime < 2000) {
                return;
            }

            lastBlockedUrl = url;
            lastRedirectTime = now;

            SiteStorage.recordBlock(this, url);

            Intent intent = new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(BLOCK_PAGE)
            );

            intent.setPackage(CHROME);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            startActivity(intent);

        } finally {
            root.recycle();
        }
    }

    @Override
    public void onInterrupt() {
    }
}
