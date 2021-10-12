package com.example.appinfosdk.controller.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.appinfosdk.R;
import com.example.appinfosdk.controller.AppinfoSDK;
import com.example.appinfosdk.controller.broadcastReceiver.AppInstallUninstallReceiver;

public class CheckAppInstallService extends Service {

    private AppInstallUninstallReceiver appInstallUninstallReceiver;
    @Override
    public void onCreate() {
        super.onCreate();
        final IntentFilter theFilter = new IntentFilter();
        theFilter.addAction(AppInstallUninstallReceiver.PCK_ADDED);
        theFilter.addAction(AppInstallUninstallReceiver.PCK_REMOVED);

        theFilter.addDataScheme("package");
        theFilter.setPriority(999);

        this.appInstallUninstallReceiver = new AppInstallUninstallReceiver();
        this.registerReceiver(this.appInstallUninstallReceiver, theFilter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification = new NotificationCompat.Builder(this, AppinfoSDK.CHANNEL_ID)
                .setContentTitle("Background Service")
                .setNotificationSilent()
                .setSmallIcon(R.drawable.ic_launcher_background)
                .build();
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel( AppinfoSDK.CHANNEL_ID, AppinfoSDK.CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            mNotificationManager.createNotificationChannel(channel);
            new NotificationCompat.Builder(this, AppinfoSDK.CHANNEL_ID);
        }
        startForeground(1, notification);
        return START_STICKY;
    }
    @Override
    public void onDestroy() {
        Log.v("BroadcastR: ", "BroadcastR: onDestroy");
        super.onDestroy();
        unregisterReceiver(this.appInstallUninstallReceiver);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
