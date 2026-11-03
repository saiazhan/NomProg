package com.example.nomprog;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import java.text.SimpleDateFormat;
import java.util.*;

public class SiteStorage {
    private static final String PREF = "nomprog_data";

    private static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    public static ArrayList<String> getSites(Context c) {
        Set<String> data = prefs(c).getStringSet("sites", new HashSet<String>());
        return new ArrayList<String>(data);
    }

    public static void saveSites(Context c, ArrayList<String> sites) {
        prefs(c).edit().putStringSet("sites", new HashSet<String>(sites)).apply();
    }

    public static void setEnabled(Context c, boolean enabled) {
        prefs(c).edit().putBoolean("enabled", enabled).apply();
    }

    public static boolean isEnabled(Context c) {
        return prefs(c).getBoolean("enabled", false);
    }

    public static String normalize(String input) {
        String value = input.trim().toLowerCase(Locale.ROOT);
        if (!value.contains("://")) value = "https://" + value;

        Uri uri = Uri.parse(value);
        String host = uri.getHost();
        if (host == null) return null;

        String path = uri.getPath();
        return host + (path == null || path.equals("/") ? "" : path);
    }

    public static boolean matches(Context c, String url) {
        Uri current = Uri.parse(url);
        String host = current.getHost();
        String path = current.getPath();

        if (host == null) return false;
        if (path == null) path = "/";

        for (String site : getSites(c)) {
            Uri blocked = Uri.parse("https://" + site);
            String blockedHost = blocked.getHost();
            String blockedPath = blocked.getPath();

            if (blockedHost == null) continue;

            boolean hostMatch = host.equals(blockedHost)
                    || host.endsWith("." + blockedHost);

            if (!hostMatch) continue;

            if (blockedPath == null || blockedPath.equals("/")
                    || path.equals(blockedPath)
                    || path.startsWith(blockedPath + "/")) {
                return true;
            }
        }
        return false;
    }

    public static void recordBlock(Context c, String site) {
        SharedPreferences p = prefs(c);
        String today = new SimpleDateFormat("yyyyMMdd", Locale.US)
                .format(new Date());

        int count = p.getInt("today_count", 0);
        if (!today.equals(p.getString("day", ""))) count = 0;

        p.edit()
                .putInt("total_count", p.getInt("total_count", 0) + 1)
                .putInt("today_count", count + 1)
                .putString("day", today)
                .putString("last_site", site)
                .apply();
    }

    public static int getTotal(Context c) {
        return prefs(c).getInt("total_count", 0);
    }

    public static int getToday(Context c) {
        SharedPreferences p = prefs(c);
        String today = new SimpleDateFormat("yyyyMMdd", Locale.US)
                .format(new Date());

        return today.equals(p.getString("day", ""))
                ? p.getInt("today_count", 0) : 0;
    }

    public static String getLastSite(Context c) {
        return prefs(c).getString("last_site", "Пока нет");
    }
}
