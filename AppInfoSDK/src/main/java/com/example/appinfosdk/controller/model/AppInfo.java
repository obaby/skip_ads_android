package com.example.appinfosdk.controller.model;

import android.graphics.drawable.Drawable;
import android.util.Log;

import java.util.Comparator;

public class AppInfo implements Comparable<AppInfo> {
    private final static String TAG = "APPINFO";
    public String appname = "";
    public String pname = "";
    public String versionName = "";
    public int versionCode = 0;
    public String launcherClassName = "";
    public Drawable icon;
    public Boolean isSystemPackage = false;
    public boolean isInWhiteList = false;

    public void print() {
        Log.v(TAG, appname + "\t"
                + pname + "\t"
                + versionName + "\t"
                + versionCode + "\t"
                + launcherClassName + "\t"
                + isSystemPackage);
    }

    @Override
    public int compareTo(AppInfo o) {
        return this.appname.compareTo(o.appname);
    }

    public Comparator<AppInfo> sortByAppName() {
        return new Comparator<AppInfo>() {
            @Override
            public int compare(AppInfo o1, AppInfo o2) {
                return o1.appname.compareTo(o2.appname);
            }
        };
    }
}
