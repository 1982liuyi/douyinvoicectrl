#!/bin/bash
# 一键推送项目到 GitHub
# 使用方法：把项目文件下载到本地后，在项目根目录运行此脚本

set -e

echo "=== 初始化 Git 仓库 ==="
cd "$(dirname "$0")"
git init
git add .
git commit -m "feat: 语音控制抖音切换 - 初始版本

- SpeechRecognizer 持续监听语音
- AccessibilityService 模拟上滑/下滑手势
- 关键词匹配：上一个/下一个
- 悬浮窗控制界面
- 前台服务保持监听"

echo "=== 推送到 GitHub ==="
git remote add origin git@github.com:1982liuyi/douyinvoicectrl.git
git branch -M main
git push -u origin main

echo "=== 完成！==="
echo "仓库地址：https://github.com/1982liuyi/douyinvoicectrl"
echo ""
echo "接下来配置 GitHub Actions 自动编译 APK："
echo "1. 在仓库 Settings → Actions → General 中启用 Actions"
echo "2. 将 .github/workflows/build.yml 文件推送到仓库"
echo "3. 等待 Actions 自动编译，APK 在 Actions → Artifacts 中下载"
