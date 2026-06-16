# 语音控制抖音 - Douyin Voice Control

用语音说"下一个"或"上一个"切换抖音视频。

## 功能
- 🎤 语音识别：说"下一个"/"下一条" → 上滑切下一个视频
- 🎤 语音识别：说"上一个"/"上一条" → 下滑切上一个视频
- 🔵 悬浮窗：显示当前状态，可拖动，可停止
- ♿ 无障碍服务：通过 `dispatchGesture` 模拟滑动手势
- 🔄 连续监听：识别完自动重启监听

## 编译方法

### 方法一：Android Studio（推荐）
1. 打开 Android Studio
2. File → Open → 选择 `douyin-voice-control/` 目录
3. 等待 Gradle Sync 完成
4. Build → Build Bundle(s) / APK(s) → Build APK(s)
5. APK 位于 `app/build/outputs/apk/debug/app-debug.apk`

### 方法二：命令行编译
```bash
cd douyin-voice-control/

# 如果没有 gradle wrapper，先生成：
# gradle wrapper --gradle-version 8.2

# 编译 debug APK
./gradlew assembleDebug

# APK 路径
ls app/build/outputs/apk/debug/app-debug.apk
```

### 前置要求
- JDK 17+
- Android SDK (compileSdk 34)
- 环境变量 `ANDROID_HOME` 指向 Android SDK 路径
- 或者使用 Android Studio 自带的 SDK

## 使用方法
1. 安装 APK 到手机
2. 打开 App
3. 点击"1. 开启无障碍服务" → 找到"语音控制抖音" → 开启
4. 返回 App，点击"开始语音控制"
5. 切换到抖音
6. 说"下一个"或"上一个"即可切换视频

## 注意事项
- 需要 Android 7.0+ (API 24)
- 首次使用需要授权麦克风和悬浮窗权限
- 无障碍服务需要手动开启（系统安全要求）
- 某些手机需要在电池优化中设为"不限制"以保持后台运行
- 语音识别使用系统内置引擎（Google 或手机厂商自带）

## 项目结构
```
app/src/main/java/com/voicecontrol/douyin/
├── MainActivity.java              # 主界面，权限检查
├── VoiceRecognitionService.java   # 前台服务，持续语音监听
├── DouyinAccessibilityService.java # 无障碍服务，模拟滑动手势
├── FloatingWindowService.java     # 悬浮窗，状态显示
└── KeywordMatcher.java            # 关键词匹配引擎
```
