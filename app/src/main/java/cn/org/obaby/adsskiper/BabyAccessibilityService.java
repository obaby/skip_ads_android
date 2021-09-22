package cn.org.obaby.adsskiper;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import com.yorhp.recordlibrary.OnScreenShotListener;
import com.yorhp.recordlibrary.ScreenRecordUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class BabyAccessibilityService extends AccessibilityService {
    final private String TAG = "BabyAccessibilityService";
    @SuppressLint("LongLogTag")
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
//        Log.i(TAG, "onAccessibilityEvent: " + event.toString());
        String packageName = event.getPackageName().toString();
//        if (!packageName.equals("com.tencent.mm")) {
//            return;
//        }

        int eventType = event.getEventType();
        switch (eventType) {
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED: //WINDOWS_CHANGE_ACTIVE
                Log.i(TAG, packageName + " onAccessibilityEvent: TYPE_WINDOW_STATE_CHANGED");
                Bitmap bmScreenShot;
                try {
                    bmScreenShot = ScreenShotter.getInstance().getScreenShotSync();
                } catch (Exception e) {
                    e.printStackTrace();
                    bmScreenShot = null;
                }
                if (bmScreenShot!=null) {
                    SaveBitmapToLocal(bmScreenShot);
                }

                break;
            case AccessibilityEvent.TYPE_WINDOWS_CHANGED:
                Log.i(TAG,  packageName + " onAccessibilityEvent: TYPE_WINDOWS_CHANGED");
                break;
            case AccessibilityEvent.WINDOWS_CHANGE_FOCUSED:
                Log.i(TAG,  packageName + " onAccessibilityEvent: WINDOWS_CHANGE_FOCUSED");
                break;
        }
    }

    @Override
    public void onInterrupt() {

    }

    /**
     * 保存图片到本地缓存目录（调试时使用）
     *
     * @param bmp
     */
    @SuppressLint("LongLogTag")
    private void SaveBitmapToLocal(Bitmap bmp) {
        String strSavePath = getCacheDir().getAbsolutePath() + File.separator + java.util.UUID.randomUUID().toString() + ".jpg";
        try {
            File f = new File(strSavePath);
            if (f.createNewFile()) {
                FileOutputStream outStream = new FileOutputStream(f);
                bmp.compress(Bitmap.CompressFormat.JPEG, 90, outStream);
                outStream.flush();
                outStream.close();
                Log.i(TAG, "SaveBitmapToLocal: "+ strSavePath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String takeScreenShot(){
        String savePicPath = "";

        return savePicPath;
    }
}
