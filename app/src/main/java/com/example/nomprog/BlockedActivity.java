package com.example.nomprog;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.*;

public class BlockedActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String site = getIntent().getStringExtra("site");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(30, 30, 30, 30);
        layout.setBackgroundColor(Color.WHITE);

        TextView title = new TextView(this);
        title.setText("NomProg");
        title.setTextSize(32);
        title.setTextColor(Color.BLACK);
        title.setGravity(Gravity.CENTER);

        TextView message = new TextView(this);
        message.setText("\nСайт заблокирован!\n\n" +
                (site == null ? "" : site));
        message.setTextSize(20);
        message.setTextColor(Color.RED);
        message.setGravity(Gravity.CENTER);

        Button button = new Button(this);
        button.setText("Понятно");
        button.setOnClickListener(v -> finish());

        layout.addView(title);
        layout.addView(message);
        layout.addView(button);
        setContentView(layout);
    }
}
