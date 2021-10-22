package cn.org.obaby.adsskiper;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Handler;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import com.example.appinfosdk.controller.AppinfoSDK;
import com.yorhp.recordlibrary.OnScreenShotListener;
import com.yorhp.recordlibrary.ScreenRecordUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import cn.org.obaby.adsskiper.detection.env.Logger;
import cn.org.obaby.adsskiper.detection.env.Utils;
import cn.org.obaby.adsskiper.detection.tflite.Classifier;
import cn.org.obaby.adsskiper.detection.tflite.YoloV5Classifier;

public class BabyAccessibilityService extends AccessibilityService {
    final private String TAG = "BabyAccessibilityService";

    private Classifier detector;
    public static final int TF_OD_API_INPUT_SIZE = 640;
    private static final boolean TF_OD_API_IS_QUANTIZED = false;
    private static final String TF_OD_API_MODEL_FILE = "yolov5s-fp16.tflite";
    private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/coco.txt";
    private static float MINIMUM_CONFIDENCE_TF_OD_API = 0.05f;
    private boolean isDebugEnable = false;
    private AppinfoSDK appinfoSDK;
    private String lastApp="";


    @SuppressLint("LongLogTag")
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
//        Log.i(TAG, "onAccessibilityEvent: " + event.toString());
        String packageName = "";
        int eventType = event.getEventType();
        switch (eventType) {
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED: //WINDOWS_CHANGE_ACTIVE
                packageName = event.getPackageName().toString();
                lastApp = packageName;
                if (AppinfoSDK.getAppinfoSDK().isInWhiteList(packageName)){
                    Log.i(TAG, "onAccessibilityEvent: in White list," + packageName);
                    return;
                }
                // com.huawei.android.launcher  com.android.systemui
               if (packageName.contains(".launcher")| packageName.contains(".systemui")){
                   Log.i(TAG, "onAccessibilityEvent: maybe system launcher");
                   return;
               }

                Log.i(TAG, packageName + " onAccessibilityEvent: TYPE_WINDOW_STATE_CHANGED");
                Bitmap bmScreenShot;
                try {
                    bmScreenShot = ScreenShotter.getInstance().getScreenShotSync();
                } catch (Exception e) {
                    e.printStackTrace();
                    bmScreenShot = null;
                }
                if (bmScreenShot!=null) {
                    if (isDebugEnable){
                        SaveBitmapToLocal(bmScreenShot, "");
                    }
                    Handler handler = new Handler();

                    Bitmap finalBmScreenShot = bmScreenShot;
                    String finalPackageName = packageName;
                    new Thread(() -> {
                        Bitmap cropBitmap = Utils.processBitmap(finalBmScreenShot, TF_OD_API_INPUT_SIZE);
                        final List<Classifier.Recognition> results = detector.recognizeImage(cropBitmap);
                        Bitmap paintBitmap = finalBmScreenShot.copy(Bitmap.Config.ARGB_8888, true);
                        if (isDebugEnable){
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    handleResult(paintBitmap, results,finalPackageName);
                                }
                            });
                        }else{
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    doGuesture(paintBitmap, results, finalPackageName);
                                }
                            });
                        }
                    }).start();
                }else{
                    Log.i(TAG, "onAccessibilityEvent: screenshot failed.");
                }

                break;
            case AccessibilityEvent.TYPE_WINDOWS_CHANGED:
                packageName = event.getPackageName().toString();
//                Log.i(TAG,  packageName + " onAccessibilityEvent: TYPE_WINDOWS_CHANGED");
                break;
            case AccessibilityEvent.WINDOWS_CHANGE_FOCUSED:
                packageName = event.getPackageName().toString();
                Log.i(TAG,  packageName + " onAccessibilityEvent: WINDOWS_CHANGE_FOCUSED");
                break;
            case AccessibilityEvent.WINDOWS_CHANGE_ADDED:
                packageName = event.getPackageName().toString();
                Log.i(TAG,  packageName + " onAccessibilityEvent: WINDOWS_CHANGE_ADDED");
                break;
        }
    }

    @Override
    public void onInterrupt() {

    }

    @SuppressLint("LongLogTag")
    public Classifier.Recognition getBestResult(List<Classifier.Recognition> results){
        Classifier.Recognition ret=null;
        float conf = 0.f;
        for (final Classifier.Recognition result : results) {
            if (result.getConfidence() > conf) {
                ret = result;
                conf = result.getConfidence();
            }
        }
        Log.i(TAG, "getBestResult: " + ret.toString());
        return ret;
    }

    @SuppressLint("LongLogTag")
    private void doGuesture(Bitmap bitmap, List<Classifier.Recognition> results, String packageName){
        if (!packageName.equals(lastApp)){
            Log.i(TAG, "doGuesture: app has changed, stop now.");
            return;
        }
        //默认返回已经排序，无需再次排序
        if (results.isEmpty()){
            Log.i(TAG, "doGuesture: result is empty");
        }else {
            Classifier.Recognition result = results.get(0);
            Log.i(TAG, "doGuesture: best = " + result.toString());
            if (result != null) {
                final RectF location = result.getLocation();
                if (location != null && result.getConfidence() >= MINIMUM_CONFIDENCE_TF_OD_API) {
                    // todo: 需要判断location位置是否在中间
                    RectF paintLocation = location;
                    paintLocation.left = location.left / 640 * bitmap.getWidth();
                    paintLocation.top = location.top / 640 * bitmap.getHeight();
                    paintLocation.right = location.right / 640 * bitmap.getWidth();
                    paintLocation.bottom = location.bottom / 640 * bitmap.getHeight();
                    int x = (int) paintLocation.centerX();
                    int y = (int) paintLocation.centerY();
                    doClick(x,y);
                }else {
                    Log.i(TAG, "doGuesture: location is null or confidence to low");
                }
            }
        }
    }

    @SuppressLint("LongLogTag")
    private void doClick(int x, int y){
        Path swipePath = new Path();
//        swipePath.moveTo(1000, 1000);
//        swipePath.lineTo(100, 1000);
        swipePath.moveTo(x,y);
        Log.i(TAG, String.format("doClick: try to click at: x = %d, y = %d.", x, y));
        GestureDescription.Builder gestureBuilder = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            gestureBuilder = new GestureDescription.Builder();
            gestureBuilder.addStroke(new GestureDescription.StrokeDescription(swipePath, 0, 500));
            dispatchGesture(gestureBuilder.build(), null, null);
        }
    }

    @SuppressLint("LongLogTag")
    private void handleResult(Bitmap bitmap, List<Classifier.Recognition> results, String packageName) {
        if (!packageName.equals(lastApp)){
            Log.i(TAG, "doGuesture: app has changed, stop now.");
            return;
        }
        final Canvas canvas = new Canvas(bitmap);
        final Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.2f);
        paint.setTextSize(40.0f);

        final List<Classifier.Recognition> mappedRecognitions =
                new LinkedList<Classifier.Recognition>();

        for (final Classifier.Recognition result : results) {
            final RectF location = result.getLocation();
            if (location != null && result.getConfidence() >= MINIMUM_CONFIDENCE_TF_OD_API) {
                RectF paintLocation = location;
                paintLocation.left = location.left/640 * bitmap.getWidth();
                paintLocation.top = location.top/640 * bitmap.getHeight();
                paintLocation.right = location.right/640 *bitmap.getWidth();
                paintLocation.bottom = location.bottom/640 *bitmap.getHeight();
                canvas.drawRect(location, paint);
                Log.i("CONFIDENCE", "handleResult: "+ result.getConfidence());
                paint.setStyle(Paint.Style.FILL_AND_STROKE);
                canvas.drawText("skip:" + result.getConfidence(),
                        location.left,
                        location.top,
                        paint);
                paint.setStyle(Paint.Style.STROKE);
            }
        }
        if (isDebugEnable){
            SaveBitmapToLocal(bitmap, "_Predicted");
        }
    }

    /**
     * 保存图片到本地缓存目录（调试时使用）
     *
     * @param bmp
     */
    @SuppressLint("LongLogTag")
    private void SaveBitmapToLocal(Bitmap bmp, String predictedTail) {

        String strSavePath = getExternalCacheDir().getAbsolutePath() + File.separator + java.util.UUID.randomUUID().toString() + predictedTail + ".jpg";
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

    /**
     * 当启动服务的时候就会被调用,系统成功绑定该服务时被触发，也就是当你在设置中开启相应的服务，
     * 系统成功的绑定了该服务时会触发，通常我们可以在这里做一些初始化操作
     */
    @SuppressLint("LongLogTag")
    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();

        appinfoSDK = AppinfoSDK.getAppinfoSDK();
        appinfoSDK.initializeSdk(getApplicationContext());

        MINIMUM_CONFIDENCE_TF_OD_API = AppinfoSDK.getAppinfoSDK().getPredictCondifence() / 100;
        isDebugEnable = AppinfoSDK.getAppinfoSDK().getIsDebugEnable();
        Log.i(TAG, "-------------------------------------------------------------------------------------");
        Log.i(TAG, "onServiceConnected: called");
        Log.i(TAG, String.format("onServiceConnected: set MINIMUM_CONFIDENCE_TF_OD_API = %.4f",MINIMUM_CONFIDENCE_TF_OD_API));
        Log.i(TAG, String.format("onServiceConnected: set isDebugEnable = %b",isDebugEnable));
        try {
            detector =
                    YoloV5Classifier.create(
                            getAssets(),
                            TF_OD_API_MODEL_FILE,
                            TF_OD_API_LABELS_FILE,
                            TF_OD_API_IS_QUANTIZED,
                            TF_OD_API_INPUT_SIZE);
            Log.i(TAG,"yolov5 init success");
        } catch (final IOException e) {
            Log.e(TAG, "Exception initializing classifier!");
            e.printStackTrace();
            Toast toast =
                    Toast.makeText(
                            getApplicationContext(), "Classifier could not be initialized", Toast.LENGTH_SHORT);
            toast.show();
        }
        Log.i(TAG, "-------------------------------------------------------------------------------------");
    }
}
