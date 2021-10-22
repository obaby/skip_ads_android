package cn.org.obaby.adsskiper;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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

    @SuppressLint("LongLogTag")
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.i(TAG, "onAccessibilityEvent: " + event.toString());
        String packageName = "";
        int eventType = event.getEventType();
        switch (eventType) {
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED: //WINDOWS_CHANGE_ACTIVE
                packageName = event.getPackageName().toString();

                if (!AppinfoSDK.getAppinfoSDK().isInWhiteList(packageName)){
                    Log.i(TAG, "onAccessibilityEvent: in White list," + packageName);
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
                    SaveBitmapToLocal(bmScreenShot);
                    Handler handler = new Handler();

                    Bitmap finalBmScreenShot = bmScreenShot;
                    new Thread(() -> {
                        Bitmap cropBitmap = Utils.processBitmap(finalBmScreenShot, TF_OD_API_INPUT_SIZE);
                        final List<Classifier.Recognition> results = detector.recognizeImage(cropBitmap);
                        Bitmap paintBitmap = finalBmScreenShot.copy(Bitmap.Config.ARGB_8888, true);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                handleResult(paintBitmap, results);
                            }
                        });
                    }).start();
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
        }
    }

    @Override
    public void onInterrupt() {

    }

    private void handleResult(Bitmap bitmap, List<Classifier.Recognition> results) {
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
//                cropToFrameTransform.mapRect(location);
//
//                result.setLocation(location);
//                mappedRecognitions.add(result);
                Log.i("CONFIDENCE", "handleResult: "+ result.getConfidence());
                paint.setStyle(Paint.Style.FILL_AND_STROKE);
                canvas.drawText("skip:" + result.getConfidence(),
                        location.left,
                        location.top,
                        paint);
                paint.setStyle(Paint.Style.STROKE);
            }
        }

    }

    /**
     * 保存图片到本地缓存目录（调试时使用）
     *
     * @param bmp
     */
    @SuppressLint("LongLogTag")
    private void SaveBitmapToLocal(Bitmap bmp) {
        String strSavePath = getExternalCacheDir().getAbsolutePath() + File.separator + java.util.UUID.randomUUID().toString() + ".jpg";
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
