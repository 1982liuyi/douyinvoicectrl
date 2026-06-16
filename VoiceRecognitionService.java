package com.voicecontrol.douyin;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class VoiceRecognitionService extends Service {

    private static final String TAG = "VoiceRecog";
    private static final String CHANNEL_ID = "voice_control_channel";
    private SpeechRecognizer recognizer;
    private boolean isListening = false;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        Notification notification = new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("语音控制抖音")
                .setContentText("正在监听语音指令...")
                .setSmallIcon(android.R.drawable.ic_btn_speak_now)
                .build();
        startForeground(1, notification);
        startRecognition();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "语音控制服务", NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("保持语音监听的前台服务通知");
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
    }

    private void startRecognition() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            Log.e(TAG, "Speech recognition not available");
            return;
        }
        if (recognizer != null) {
            try { recognizer.destroy(); } catch (Exception ignored) {}
        }
        recognizer = SpeechRecognizer.createSpeechRecognizer(this);
        recognizer.setRecognitionListener(new Listener());

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh-CN");
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false);

        try {
            recognizer.startListening(intent);
            isListening = true;
            Log.d(TAG, "Started listening");
        } catch (Exception e) {
            Log.e(TAG, "startListening failed", e);
        }
    }

    private void restartListening() {
        if (recognizer != null) {
            try { recognizer.cancel(); } catch (Exception ignored) {}
        }
        // Small delay to avoid rapid restart issues
        new android.os.Handler(getMainLooper()).postDelayed(this::startRecognition, 300);
    }

    private class Listener implements RecognitionListener {
        @Override public void onReadyForSpeech(Bundle params) {}
        @Override public void onBeginningOfSpeech() {}
        @Override public void onRmsChanged(float rmsdB) {}
        @Override public void onBufferReceived(byte[] buffer) {}
        @Override public void onEndOfSpeech() {}

        @Override
        public void onError(int error) {
            Log.w(TAG, "Recognition error: " + error);
            // Restart on recoverable errors
            if (error == SpeechRecognizer.ERROR_NO_MATCH
                    || error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT
                    || error == SpeechRecognizer.ERROR_AUDIO) {
                restartListening();
            }
        }

        @Override
        public void onResults(Bundle results) {
            ArrayList<String> matches = results.getStringArrayList(
                    SpeechRecognizer.RESULTS_RECOGNITION);
            if (matches != null) {
                for (String text : matches) {
                    Log.d(TAG, "Heard: " + text);
                    KeywordMatcher.Action action = KeywordMatcher.match(text);
                    if (action != null) {
                        executeAction(action);
                        break;
                    }
                }
            }
            restartListening();
        }

        @Override public void onPartialResults(Bundle partialResults) {}
        @Override public void onEvent(int eventType, Bundle params) {}
    }

    private void executeAction(KeywordMatcher.Action action) {
        DouyinAccessibilityService svc = DouyinAccessibilityService.getInstance();
        if (svc == null) {
            Log.w(TAG, "Accessibility service not running");
            return;
        }
        if (action == KeywordMatcher.Action.NEXT) {
            svc.swipeUp();
            Log.i(TAG, ">> NEXT (swipe up)");
            FloatingWindowService.updateAction("下一个 ▲");
        } else if (action == KeywordMatcher.Action.PREV) {
            svc.swipeDown();
            Log.i(TAG, ">> PREV (swipe down)");
            FloatingWindowService.updateAction("上一个 ▼");
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        if (recognizer != null) {
            try { recognizer.destroy(); } catch (Exception ignored) {}
        }
        isListening = false;
        super.onDestroy();
    }
}
