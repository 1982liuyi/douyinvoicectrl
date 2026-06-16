package com.voicecontrol.douyin;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Context;
import android.graphics.Path;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

public class DouyinAccessibilityService extends AccessibilityService {

    private static final String TAG = "DouyinA11y";
    private static DouyinAccessibilityService instance;

    public static DouyinAccessibilityService getInstance() {
        return instance;
    }

    public static boolean isEnabled(Context context) {
        // Simple check: see if instance exists
        return instance != null;
    }

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
        Log.i(TAG, "Accessibility service connected");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Not used, but required
    }

    @Override
    public void onInterrupt() {
        Log.w(TAG, "Accessibility service interrupted");
    }

    /**
     * Swipe up → next video (finger moves from bottom to top)
     */
    public void swipeUp() {
        performSwipe(0.8f, 0.2f);
    }

    /**
     * Swipe down → previous video (finger moves from top to bottom)
     */
    public void swipeDown() {
        performSwipe(0.2f, 0.8f);
    }

    private void performSwipe(float startYRatio, float endYRatio) {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        float centerX = dm.widthPixels / 2f;
        float startY = dm.heightPixels * startYRatio;
        float endY = dm.heightPixels * endYRatio;

        Path path = new Path();
        path.moveTo(centerX, startY);
        path.lineTo(centerX, endY);

        GestureDescription.Builder builder = new GestureDescription.Builder();
        builder.addStroke(new GestureDescription.StrokeDescription(path, 0, 300));

        boolean dispatched = dispatchGesture(builder.build(), new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                Log.d(TAG, "Gesture completed");
            }
            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                Log.w(TAG, "Gesture cancelled");
            }
        }, null);

        if (!dispatched) {
            Log.e(TAG, "Gesture dispatch failed");
        }
    }

    @Override
    public void onDestroy() {
        instance = null;
        super.onDestroy();
    }
}
