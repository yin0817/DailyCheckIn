#!/bin/bash
# 每日打卡 APK 构建脚本

set -e

echo "======================================"
echo "  每日打卡 - APK 构建脚本"
echo "======================================"
echo ""

# 检查 Java
if ! command -v java &> /dev/null; then
    echo "错误: 未找到 Java，请先安装 JDK 17"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | head -1 | cut -d'"' -f2)
echo "✓ Java 版本: $JAVA_VERSION"

# 检查 Android SDK
if [ -z "$ANDROID_HOME" ] && [ -z "$ANDROID_SDK_ROOT" ]; then
    echo "警告: 未设置 ANDROID_HOME 环境变量"
    echo "请设置 Android SDK 路径，例如:"
    echo "  export ANDROID_HOME=/path/to/android-sdk"
    echo ""
fi

# 清理旧构建
echo ""
echo "[1/4] 清理旧构建..."
./gradlew clean --no-daemon 2>/dev/null || true

# 构建调试版
echo ""
echo "[2/4] 构建调试版 APK..."
./gradlew assembleDebug --no-daemon

# 检查构建结果
DEBUG_APK="app/build/outputs/apk/debug/app-debug.apk"
if [ -f "$DEBUG_APK" ]; then
    echo ""
    echo "✓ 调试版 APK 构建成功!"
    echo "  位置: $DEBUG_APK"
    ls -lh "$DEBUG_APK" | awk '{print "  大小:", $5}'
else
    echo "错误: 调试版 APK 构建失败"
    exit 1
fi

# 构建发布版
echo ""
echo "[3/4] 构建发布版 APK..."
echo "注意: 发布版需要签名密钥"

# 检查签名配置
if grep -q "storeFile.*keystore" app/build.gradle 2>/dev/null; then
    ./gradlew assembleRelease --no-daemon
    RELEASE_APK="app/build/outputs/apk/release/app-release.apk"
    if [ -f "$RELEASE_APK" ]; then
        echo ""
        echo "✓ 发布版 APK 构建成功!"
        echo "  位置: $RELEASE_APK"
        ls -lh "$RELEASE_APK" | awk '{print "  大小:", $5}'
    fi
else
    echo "提示: 未配置签名密钥，跳过发布版构建"
    echo "      如需构建发布版，请先配置签名"
fi

echo ""
echo "[4/4] 构建完成!"
echo ""
echo "======================================"
echo "  安装 APK 到设备:"
echo "  adb install $DEBUG_APK"
echo "======================================"
