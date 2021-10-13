package cn.org.obaby.adsskiper.whitelist.controller;

import android.app.Application;

import com.example.appinfosdk.controller.AppinfoSDK;

public class MyApp extends Application {
    private AppinfoSDK appinfoSDK;
    @Override
    public void onCreate() {
        super.onCreate();
        appinfoSDK = AppinfoSDK.getAppinfoSDK();
        appinfoSDK.initializeSdk(getApplicationContext());
    }
}
