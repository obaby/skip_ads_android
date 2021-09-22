package cn.org.obaby.adsskiper;

import android.content.Context;

import com.tencent.tinker.loader.app.TinkerApplication;
import com.tencent.tinker.loader.shareutil.ShareConstants;

import java.lang.ref.WeakReference;

/**
 * Created by Ln_Jan on 2019/1/27.
 * 腾讯Bugly热更新框架TinkerApplication实现类
 */

public class LuckyMoneyTinkerApplication extends TinkerApplication {
    private static WeakReference<Context> mContext;

    public LuckyMoneyTinkerApplication() {
        super(ShareConstants.TINKER_ENABLE_ALL, "cn.org.obaby.adsskiper.TinkerApplicationLike",
                "com.tencent.tinker.loader.TinkerLoader", false);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = new WeakReference<>(getApplicationContext());
    }

    public static Context getContext() {
        return mContext.get();
    }
}
