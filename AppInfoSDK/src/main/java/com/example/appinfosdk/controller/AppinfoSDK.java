package com.example.appinfosdk.controller;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.example.appinfosdk.controller.model.AppInfo;
import com.example.appinfosdk.controller.services.CheckAppInstallService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static android.content.Intent.CATEGORY_LAUNCHER;

public class AppinfoSDK {
    public static final String CHANNEL_ID = AppinfoSDK.class.getName();
    public static final String CHANNEL_NAME = AppinfoSDK.class.getName();

    private static AppinfoSDK appinfoSDK;
    private Context context;

    public Set<String> appListSet = null;
    public final String WHITE_LIST = "white_list";
    public final String APP_LIST_KEY = "app_list_key";
    public final String PREDICT_CONFIDENCE_KEY = "predict_confidence_key";
    private final String TAG = "AppinfoSDK";
    public float predictCondifence = 0.70f;
    public boolean isDebug = false;
    public final String IS_DEBUG_KEY = "is_debug_key";

    private AppinfoSDK() {
    }

    public static AppinfoSDK getAppinfoSDK() {
        if (appinfoSDK == null) {
            appinfoSDK = new AppinfoSDK();
        }
        return appinfoSDK;
    }

    public void initializeSdk(Context context) {
        this.context = context;
        createNotificationChannel(context);
        this.appListSet = getWhiteAppListSet();
        printAppListSet();
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    AppinfoSDK.CHANNEL_ID,
                    "InstallAppEventChannel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    public void registerForAppInstallUninstallEvents(Context context) {
        Intent serviceIntent = new Intent(context, CheckAppInstallService.class);
        serviceIntent.putExtra("inputExtra", "passing any text");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(context, serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
    }

    public Boolean isInWhiteList(String appBundleID) {
        if (appListSet == null){
            return false;
        }
        if (appListSet.contains(appBundleID)) {
            return true;
        }
        return false;
    }

    public Set<String> getWhiteAppListSet() {
        SharedPreferences preferences = context.getSharedPreferences(WHITE_LIST, Context.MODE_PRIVATE);
        Set<String> tmpSet = preferences.getStringSet(APP_LIST_KEY, null);
        appListSet = tmpSet;
        return tmpSet;
    }

    public float getPredictCondifence(){
        SharedPreferences preferences = context.getSharedPreferences(WHITE_LIST, Context.MODE_PRIVATE);
        float tpc = preferences.getFloat(PREDICT_CONFIDENCE_KEY, 0.70f) * 100;
        predictCondifence = tpc;
        return predictCondifence;
    }

    public void setPredictCondifence(float pc){
        predictCondifence = pc / 100;
        SharedPreferences preferences = context.getSharedPreferences(WHITE_LIST, Context.MODE_PRIVATE);
        SharedPreferences.Editor keyEditor = preferences.edit();
        keyEditor.putFloat(PREDICT_CONFIDENCE_KEY,predictCondifence);
        keyEditor.apply();
    }

    public boolean getIsDebugEnable(){
        SharedPreferences preferences = context.getSharedPreferences(WHITE_LIST, Context.MODE_PRIVATE);
        boolean ts = preferences.getBoolean(IS_DEBUG_KEY, false);
        isDebug = ts;
        return ts;
    }

    public void setIsDebugEnable(boolean is){
        isDebug = is;
        SharedPreferences preferences = context.getSharedPreferences(WHITE_LIST, Context.MODE_PRIVATE);
        SharedPreferences.Editor keyEditor = preferences.edit();
        keyEditor.putBoolean(IS_DEBUG_KEY,isDebug);
        keyEditor.apply();
    }

    public void printAppListSet() {
        Log.i(TAG, "printAppListSet: ");
        if (appListSet == null) {
            Log.i(TAG, "set is null ");
        } else {
            for (String s : appListSet) {
                Log.i(TAG, "\t" + s);
            }
        }
    }

    @SuppressLint("MutatingSharedPrefs")
    public void addAppToWhiteList(String appBundleID) {
        SharedPreferences preferences = context.getSharedPreferences(WHITE_LIST, Context.MODE_PRIVATE);
        Set<String> tmpSet = preferences.getStringSet(APP_LIST_KEY, null);
        if (tmpSet == null) {
            tmpSet = new HashSet<>();
        } else {
            tmpSet = new HashSet<>(tmpSet);
        }
        tmpSet.add(appBundleID);
        appListSet = tmpSet;
        SharedPreferences.Editor keyEditor = preferences.edit();
        keyEditor.putStringSet(APP_LIST_KEY, tmpSet);
        keyEditor.apply();
        Log.i(TAG, "addAppToWhiteList: " + appBundleID);
    }

    @SuppressLint("MutatingSharedPrefs")
    public void removeAppFromWhiteList(String appBundleID) {
        SharedPreferences preferences = context.getSharedPreferences(WHITE_LIST, Context.MODE_PRIVATE);
        Set<String> tmpSet = preferences.getStringSet(APP_LIST_KEY, null);
        if (tmpSet == null) {
            tmpSet = new HashSet<>();
        } else {
            tmpSet = new HashSet<>(tmpSet);
        }
        if (tmpSet.contains(appBundleID)) {
            tmpSet.remove(appBundleID);
        }
        appListSet = tmpSet;
        SharedPreferences.Editor keyEditor = preferences.edit();
        keyEditor.putStringSet(APP_LIST_KEY, tmpSet);
        keyEditor.apply();
        Log.i(TAG, "removeAppFromWhiteList: " + appBundleID);
    }


    public ArrayList<AppInfo> getInstalledApps(boolean getSysPackages, boolean onlyLaunchableApps) {
        ArrayList<AppInfo> res = new ArrayList<>();
        List<PackageInfo> packs = context.getPackageManager().getInstalledPackages(0);
        for (int i = 0; i < packs.size(); i++) {
            try {
                PackageInfo p = packs.get(i);
                Intent intent = context.getPackageManager().getLaunchIntentForPackage(p.packageName);

                if (onlyLaunchableApps &&
                        (intent == null ||
                                (intent.getCategories() == null || !intent.getCategories().contains(CATEGORY_LAUNCHER))))
                    continue;

                if (isSystemPackage(p) && !getSysPackages) continue;

                AppInfo newInfo = new AppInfo();
                newInfo.appname = p.applicationInfo.loadLabel(context.getPackageManager()).toString();
                Log.v("Labels: ", newInfo.appname);
                newInfo.pname = p.packageName;
                newInfo.versionName = p.versionName;
                newInfo.versionCode = p.versionCode;
                newInfo.isSystemPackage = isSystemPackage(p);
                newInfo.icon = p.applicationInfo.loadIcon(context.getPackageManager());
                newInfo.isInWhiteList = isInWhiteList(p.packageName);
                newInfo.print();
                String className = "NA";
                if (intent != null && intent.getComponent() != null) {
                    className = intent.getComponent().getShortClassName();
                    className = className.substring(className.lastIndexOf(".") + 1);
                }
                newInfo.launcherClassName = className;//p.applicationInfo.className;
                res.add(newInfo);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return res;
    }

    /**
     * Return whether the given PackageInfo represents a system package or not.
     * User-installed packages (Market or otherwise) should not be denoted as
     * system packages.
     *
     * @param pkgInfo
     * @return
     */
    private boolean isSystemPackage(PackageInfo pkgInfo) {
        return ((pkgInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
    }

    /**
     * Open another app.
     *
     * @param context     current Context, like Activity, App, or Service
     * @param packageName the full package name of the app to open
     * @return true if likely successful, false if unsuccessful
     */
    public boolean openApp(Context context, String packageName) {
        PackageManager manager = context.getPackageManager();
        try {
            Intent i = manager.getLaunchIntentForPackage(packageName);
            if (i == null) {
                return false;
            }
            i.addCategory(Intent.CATEGORY_LAUNCHER);
            context.startActivity(i);
            return true;
        } catch (ActivityNotFoundException e) {
            return false;
        }
    }
}
