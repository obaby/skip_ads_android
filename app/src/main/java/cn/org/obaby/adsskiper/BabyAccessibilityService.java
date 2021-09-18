package cn.org.obaby.adsskiper;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

public class BabyAccessibilityService extends AccessibilityService {
    final private String TAG = "BabyAccessibilityService";
    @SuppressLint("LongLogTag")
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.i(TAG, "onAccessibilityEvent: " + event.toString());

    }

    @Override
    public void onInterrupt() {

    }
}
