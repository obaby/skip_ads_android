package com.example.appinfosdk.controller.broadcastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.appinfosdk.R;

import java.util.Objects;

import static com.example.appinfosdk.controller.AppinfoSDK.CHANNEL_ID;

public class AppInstallUninstallReceiver extends BroadcastReceiver {
    public final static String SCREEN_ON = Intent.ACTION_SCREEN_ON;
    public final static String SCREEN_OFF = Intent.ACTION_SCREEN_OFF;
    public final static String PCK_ADDED = Intent.ACTION_PACKAGE_ADDED;
    public final static String PCK_CHANGED = Intent.ACTION_PACKAGE_CHANGED;
    public final static String PCK_FIRST_LAUNCH = Intent.ACTION_PACKAGE_FIRST_LAUNCH;
    public final static String PCK_VERIFIED = Intent.ACTION_PACKAGE_VERIFIED;
    public final static String INSTALL_PCK = Intent.ACTION_INSTALL_PACKAGE;
    public final static String PCK_REMOVED = Intent.ACTION_PACKAGE_REMOVED;
    public final static String PCK_FULYY_REMOVED = Intent.ACTION_PACKAGE_FULLY_REMOVED;

    @Override
    public void onReceive(Context context, Intent intent) {
        final PackageManager pm = context.getApplicationContext().getPackageManager();
        ApplicationInfo ai;
        try {
            ai = pm.getApplicationInfo(intent.getData().getSchemeSpecificPart(), 0);
        } catch (Exception e) {
            ai = null;
        }
        final String applicationName = (String) (ai != null ? pm.getApplicationLabel(ai) : intent.getData().getSchemeSpecificPart());

        String notificationMsg = "";
        switch (Objects.requireNonNull(intent.getAction())) {
            case Intent.ACTION_PACKAGE_ADDED:
                notificationMsg = applicationName + " Installed ";
                Toast.makeText(context, notificationMsg, Toast.LENGTH_LONG).show();
                break;
            case Intent.ACTION_PACKAGE_FULLY_REMOVED:
            case Intent.ACTION_PACKAGE_REMOVED:
                notificationMsg = applicationName + " Uninstalled ";
                Toast.makeText(context, notificationMsg, Toast.LENGTH_LONG).show();
                break;
        }

        if(!notificationMsg.isEmpty()) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher_background)
                    .setContentTitle(context.getResources().getString(R.string.app_name))
                    .setContentText(notificationMsg)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true);
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(100, builder.build());
        }
    }
}
