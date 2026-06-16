package com.voicecontrol.douyin;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

public class FloatingWindowService extends Service {

    private WindowManager windowManager;
    private View floatingView;
    private static TextView tvAction;

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        LayoutInflater inflater = LayoutInflater.from(this);
        floatingView = inflater.inflate(R.layout.floating_window, null);

        int layoutType = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layoutType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 0;
        params.y = 200;

        tvAction = floatingView.findViewById(R.id.tv_action);
        tvAction.setText("等待指令...");

        // Make draggable
        floatingView.setOnTouchListener(new DragTouchListener(params));

        // Tap to stop
        floatingView.findViewById(R.id.btn_stop).setOnClickListener(v -> {
            stopService(new Intent(this, VoiceRecognitionService.class));
            stopSelf();
        });

        windowManager.addView(floatingView, params);
    }

    /** Called from VoiceRecognitionService to update action display */
    public static void updateAction(String action) {
        if (tvAction != null) {
            tvAction.post(() -> tvAction.setText(action));
        }
    }

    private class DragTouchListener implements View.OnTouchListener {
        private final WindowManager.LayoutParams params;
        private int initialX, initialY;
        private float initialTouchX, initialTouchY;

        DragTouchListener(WindowManager.LayoutParams params) {
            this.params = params;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    initialX = params.x;
                    initialY = params.y;
                    initialTouchX = event.getRawX();
                    initialTouchY = event.getRawY();
                    return true;
                case MotionEvent.ACTION_MOVE:
                    params.x = initialX + (int) (event.getRawX() - initialTouchX);
                    params.y = initialY + (int) (event.getRawY() - initialTouchY);
                    windowManager.updateViewLayout(floatingView, params);
                    return true;
            }
            return false;
        }
    }

    @Override
    public void onDestroy() {
        if (floatingView != null && windowManager != null) {
            windowManager.removeView(floatingView);
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
