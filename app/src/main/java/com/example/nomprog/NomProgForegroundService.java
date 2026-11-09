package com.example.nomprog;

import android.app.Service;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

public class NomProgForegroundService extends Service {

    private static final String CHANNEL_ID = "nomprog_protection";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForeground(1001, createNotification());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Защита NomProg",
                    NotificationManager.IMPORTANCE_LOW
            );

            channel.setDescription("Состояние защиты сайтов");
            channel.setShowBadge(false);

            NotificationManager manager =
                    getSystemService(NotificationManager.class);

            manager.createNotificationChannel(channel);
        }
    }

    private Notification createNotification() {
        Intent openApp = new Intent(this, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                openApp,
                PendingIntent.FLAG_UPDATE_CURRENT |
                        (Build.VERSION.SDK_INT >= 23
                                ? PendingIntent.FLAG_IMMUTABLE : 0)
        );

        Notification.Builder builder;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(this, CHANNEL_ID);
        } else {
            builder = new Notification.Builder(this);
        }

        return builder
                .setSmallIcon(android.R.drawable.ic_lock_lock)
                .setContentTitle("NomProg — защита активна")
                .setContentText("Контроль сайтов в Google Chrome")
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setShowWhen(false)
                .build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
