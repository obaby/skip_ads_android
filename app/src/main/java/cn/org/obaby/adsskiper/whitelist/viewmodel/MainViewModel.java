package cn.org.obaby.adsskiper.whitelist.viewmodel;

import android.app.Application;
import android.content.Context;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.appinfosdk.controller.AppinfoSDK;
import com.example.appinfosdk.controller.model.AppInfo;

import java.util.ArrayList;

public class MainViewModel extends AndroidViewModel {

    private MutableLiveData<ArrayList<AppInfo>> appInfoLiveData;
    private ArrayList<AppInfo> appInfoArrayList;
    private Context context;

    public MainViewModel(Application application) {
        super(application);
        context = getApplication().getApplicationContext();
        appInfoLiveData = new MutableLiveData<>();
        // call your Rest API in init method
        init();
    }

    public MutableLiveData<ArrayList<AppInfo>> getAppInfoMutableLiveData() {
        return appInfoLiveData;
    }

    public void init() {
        populateList();
        appInfoLiveData.setValue(appInfoArrayList);
    }

    public void populateList() {
        appInfoArrayList = AppinfoSDK.getAppinfoSDK().getInstalledApps(true, true);
    }

}
