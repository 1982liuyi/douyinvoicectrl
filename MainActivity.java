package com.voicecontrol.douyin;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private static final int RC_AUDIO = 100;
    private static final int RC_OVERLAY = 101;
    private boolean listening = false;
    private Button btnToggle;
    private TextView tvStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnToggle = findViewById(R.id.btn_toggle);
        tvStatus = findViewById(R.id.tv_status);
        Button btnAccessibility = findViewById(R.id.btn_accessibility);

        btnToggle.setOnClickListener(v -> {
            if (!listening) {
                startListening();
            } else {
                stopListening();
            }
        });

        btnAccessibility.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatus();
    }

    private void startListening() {
        // Check audio permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO}, RC_AUDIO);
            return;
        }
        // Check overlay permission
        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, RC_OVERLAY);
            return;
        }
        // Check notification permission (Android 13+)
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(this, "android.permission.POST_NOTIFICATIONS")
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{"android.permission.POST_NOTIFICATIONS"}, 102);
                return;
            }
        }
        // Start voice service
        Intent svc = new Intent(this, VoiceRecognitionService.class);
        startForegroundService(svc);
        // Start floating window
        Intent floatSvc = new Intent(this, FloatingWindowService.class);
        startService(floatSvc);
        listening = true;
        updateStatus();
    }

    private void stopListening() {
        stopService(new Intent(this, VoiceRecognitionService.class));
        stopService(new Intent(this, FloatingWindowService.class));
        listening = false;
        updateStatus();
    }

    private void updateStatus() {
        boolean a11yEnabled = DouyinAccessibilityService.isEnabled(this);
        tvStatus.setText(a11yEnabled
                ? "✅ 无障碍服务已开启"
                : "⚠️ 请先开启无障碍服务");
        tvStatus.setTextColor(a11yEnabled ? 0xFF4CAF50 : 0xFFFF5722);
        btnToggle.setText(listening ? "停止语音控制" : "开始语音控制");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] perms,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, perms, grantResults);
        if (requestCode == RC_AUDIO && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startListening(); // retry
        } else if (requestCode == RC_AUDIO) {
            Toast.makeText(this, "需要麦克风权限", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listening) {
            stopListening();
        }
    }
}
