package cn.org.obaby.adsskiper.whitelist.view;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appinfosdk.controller.AppinfoSDK;
import com.example.appinfosdk.controller.model.AppInfo;

import cn.org.obaby.adsskiper.R;
import cn.org.obaby.adsskiper.whitelist.viewmodel.MainViewModel;

import java.util.ArrayList;
import java.util.Collections;

public class WhiteListMainActivity extends AppCompatActivity {
    final static String TAG = "MainActivityTAG";

    private RecyclerView recyclerView;
    private RecyclerViewAdapter recyclerViewAdapter;
    private MainViewModel mainViewModel;
    private SearchView searchView;
    private AppinfoSDK appinfoSDK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (appinfoSDK == null) {
            appinfoSDK = AppinfoSDK.getAppinfoSDK();
            appinfoSDK.initializeSdk(getApplicationContext());
        }

        setContentView(R.layout.activity_main_whitelist);
        recyclerView = findViewById(R.id.rv_main);
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        // toolbar fancy stuff
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        mainViewModel.getAppInfoMutableLiveData().observe(this, appListUpdateObserver);

        AppinfoSDK.getAppinfoSDK().registerForAppInstallUninstallEvents(this);
    }

    Observer<ArrayList<AppInfo>> appListUpdateObserver = new Observer<ArrayList<AppInfo>>() {
        @Override
        public void onChanged(ArrayList<AppInfo> appInfoList) {
            if (appInfoList != null && appInfoList.size() > 0) {
                Collections.sort(appInfoList);
//                Collections.sort(appInfoList, appInfoList.get(0).sortByAppName());
            }
            recyclerViewAdapter = new RecyclerViewAdapter(getApplicationContext(), appInfoList);
            recyclerView.setLayoutManager(new LinearLayoutManager(WhiteListMainActivity.this));
            recyclerView.setAdapter(recyclerViewAdapter);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search)
                .getActionView();
        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);

        // listening to search query text change
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // filter recycler view when query submitted
                recyclerViewAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                // filter recycler view when text is changed
                recyclerViewAdapter.getFilter().filter(query);
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // close search view on back button pressed
        if (!searchView.isIconified()) {
            searchView.setIconified(true);
            return;
        }
        super.onBackPressed();
    }
}