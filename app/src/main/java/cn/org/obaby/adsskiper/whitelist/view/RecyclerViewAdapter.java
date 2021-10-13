package cn.org.obaby.adsskiper.whitelist.view;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appinfosdk.controller.model.AppInfo;
import cn.org.obaby.adsskiper.R;
import cn.org.obaby.adsskiper.whitelist.controller.MainActivityListener;
import cn.org.obaby.adsskiper.databinding.ItemBinding;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {

    private final static String TAG = "ADAPTERTAG";
    private Context context;
    private ArrayList<AppInfo> appInfoArrayList;
    private ArrayList<AppInfo> appInfoFilteredArrayList;

    public RecyclerViewAdapter(Context context, ArrayList<AppInfo> appInfoArrayList) {
        this.context = context;
        this.appInfoArrayList = appInfoArrayList;
        this.appInfoFilteredArrayList = appInfoArrayList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
//        View itemView = inflater.inflate(R.layout.item, parent, false);
        ItemBinding itemBinding = DataBindingUtil.inflate(inflater, R.layout.item, parent, false);
        return new RecyclerViewViewHolder(itemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        AppInfo appInfo = appInfoFilteredArrayList.get(position);
        ((RecyclerViewViewHolder) holder).bind(appInfo);
        ((RecyclerViewViewHolder) holder).itemBinding.setListener(new MainActivityListener() {
            @Override
            public void appInfoListItemClicked(View view, AppInfo appInfo) {
                // todo: change to switch
                Toast.makeText(context, appInfo.appname, Toast.LENGTH_LONG).show();
                Log.v(TAG, appInfo.appname);
                Switch sw = view.findViewById(R.id.switchWhiteList);
                sw.setChecked(!sw.isChecked());
//                AppinfoSDK.getAppinfoSDK().openApp(context, appInfo.pname);
            }
        });
    }

    @Override
    public int getItemCount() {
        return appInfoFilteredArrayList.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    appInfoFilteredArrayList = appInfoArrayList;
                } else {
                    ArrayList<AppInfo> filteredList = new ArrayList<>();
                    for (AppInfo appInfo : appInfoArrayList) {
                        if (appInfo.appname.toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(appInfo);
                        }
                    }

                    appInfoFilteredArrayList = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = appInfoFilteredArrayList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                appInfoFilteredArrayList = (ArrayList<AppInfo>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    class RecyclerViewViewHolder extends RecyclerView.ViewHolder {
        ItemBinding itemBinding;

        public RecyclerViewViewHolder(ItemBinding itemBinding){
            super(itemBinding.getRoot());
            this.itemBinding = itemBinding;
        }

        public void bind(AppInfo obj){
            itemBinding.setAppInfo(obj);
            itemBinding.executePendingBindings();
        }
    }
}