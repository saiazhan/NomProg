package com.example.nomprog;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.provider.Settings;
import android.widget.*;
import java.util.ArrayList;

public class MainActivity extends Activity {
    LinearLayout root, siteList;
    Switch blockSwitch;
    TextView statistics;
    EditText siteInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ScrollView scroll = new ScrollView(this);
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(30, 30, 30, 30);
        scroll.addView(root);
        setContentView(scroll);

        TextView title = new TextView(this);
        title.setText("NomProg");
        title.setTextSize(30);
        root.addView(title);

        blockSwitch = new Switch(this);
        blockSwitch.setText("Блокировка сайтов");
        blockSwitch.setChecked(SiteStorage.isEnabled(this));
        root.addView(blockSwitch);

        blockSwitch.setOnCheckedChangeListener((button, checked) -> {
            SiteStorage.setEnabled(this, checked);
            if (checked) {
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
            }
        });

        Button access = new Button(this);
        access.setText("Настроить Accessibility");
        root.addView(access);
        access.setOnClickListener(v -> startActivity(
                new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));

        TextView heading = new TextView(this);
        heading.setText("\nСтатистика");
        heading.setTextSize(22);
        root.addView(heading);

        statistics = new TextView(this);
        statistics.setTextSize(17);
        root.addView(statistics);

        TextView sitesHeading = new TextView(this);
        sitesHeading.setText("\nЗаблокированные сайты");
        sitesHeading.setTextSize(22);
        root.addView(sitesHeading);

        siteInput = new EditText(this);
        siteInput.setSingleLine(true);
        siteInput.setHint("youtube.com/shorts");
        root.addView(siteInput);

        Button add = new Button(this);
        add.setText("Добавить сайт");
        root.addView(add);

        siteList = new LinearLayout(this);
        siteList.setOrientation(LinearLayout.VERTICAL);
        root.addView(siteList);

        add.setOnClickListener(v -> {
            String site = SiteStorage.normalize(siteInput.getText().toString());
            if (site == null) {
                Toast.makeText(this, "Неверный адрес", Toast.LENGTH_SHORT).show();
                return;
            }

            ArrayList<String> sites = SiteStorage.getSites(this);
            if (!sites.contains(site)) {
                sites.add(site);
                SiteStorage.saveSites(this, sites);
            }
            siteInput.setText("");
            renderSites();
        });

        renderSites();
        updateStatistics();
    }

    private void renderSites() {
        siteList.removeAllViews();
        for (String site : SiteStorage.getSites(this)) {
            LinearLayout row = new LinearLayout(this);

            TextView name = new TextView(this);
            name.setText(site);
            name.setTextSize(16);
            name.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1));

            Button delete = new Button(this);
            delete.setText("Удалить");
            delete.setOnClickListener(v -> {
                ArrayList<String> sites = SiteStorage.getSites(this);
                sites.remove(site);
                SiteStorage.saveSites(this, sites);
                renderSites();
            });

            row.addView(name);
            row.addView(delete);
            siteList.addView(row);
        }
    }

    private void updateStatistics() {
        statistics.setText("Всего блокировок: " + SiteStorage.getTotal(this)
                + "\nЗа сегодня: " + SiteStorage.getToday(this)
                + "\nПоследний сайт: " + SiteStorage.getLastSite(this));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (statistics != null) updateStatistics();
    }
}
