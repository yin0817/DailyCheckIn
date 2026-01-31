# 每日打卡 - APK 构建指南

## 快速构建（推荐）

### 方法 1: 使用 GitHub Actions（最简单）

1. 将项目推送到 GitHub 仓库
2. 进入仓库的 Actions 页面
3. 运行 "Build APK" 工作流
4. 下载构建好的 APK 文件

### 方法 2: 本地构建

#### 环境要求

- **JDK 17** 或更高版本
- **Android SDK** (API 34)
- **Gradle** (项目已包含 Wrapper)

#### 步骤

**Linux/Mac:**
```bash
# 1. 进入项目目录
cd DailyCheckIn

# 2. 运行构建脚本
chmod +x build-apk.sh
./build-apk.sh
```

**Windows:**
```cmd
:: 1. 进入项目目录
cd DailyCheckIn

:: 2. 运行构建脚本
build-apk.bat
```

**手动构建:**
```bash
# 1. 进入项目目录
cd DailyCheckIn

# 2. 构建调试版
./gradlew assembleDebug

# 3. APK 输出位置
# app/build/outputs/apk/debug/app-debug.apk
```

## 详细构建步骤

### 1. 安装 JDK 17

**Ubuntu/Debian:**
```bash
sudo apt update
sudo apt install openjdk-17-jdk
```

**macOS:**
```bash
brew install openjdk@17
```

**Windows:**
1. 下载 JDK 17: https://adoptium.net/
2. 安装并设置环境变量

### 2. 安装 Android SDK

**方式 1: Android Studio（推荐）**
1. 下载安装 Android Studio: https://developer.android.com/studio
2. 打开 Android Studio，自动配置 SDK

**方式 2: 命令行工具**
```bash
# 下载命令行工具
mkdir -p ~/android-sdk/cmdline-tools
cd ~/android-sdk/cmdline-tools
curl -O https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip
unzip commandlinetools-linux-11076708_latest.zip
mv cmdline-tools latest

# 设置环境变量
export ANDROID_HOME=~/android-sdk
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools

# 安装所需组件
sdkmanager "platforms;android-34" "build-tools;34.0.0" "platform-tools"
```

### 3. 构建 APK

```bash
# 克隆项目
git clone <your-repo-url>
cd DailyCheckIn

# 构建调试版
./gradlew assembleDebug

# 构建发布版（需要签名）
./gradlew assembleRelease
```

## 构建输出

| 类型 | 位置 | 说明 |
|------|------|------|
| 调试版 | `app/build/outputs/apk/debug/app-debug.apk` | 开发测试用 |
| 发布版 | `app/build/outputs/apk/release/app-release-unsigned.apk` | 需签名后分发 |

## 签名发布版

### 创建密钥库
```bash
keytool -genkey -v -keystore dailycheckin.keystore -alias dailycheckin -keyalg RSA -keysize 2048 -validity 10000
```

### 配置签名
编辑 `app/build.gradle`:
```gradle
android {
    signingConfigs {
        release {
            storeFile file("dailycheckin.keystore")
            storePassword "your_password"
            keyAlias "dailycheckin"
            keyPassword "your_password"
        }
    }
    
    buildTypes {
        release {
            signingConfig signingConfigs.release
            minifyEnabled true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
}
```

### 构建签名 APK
```bash
./gradlew assembleRelease
```

输出位置: `app/build/outputs/apk/release/app-release.apk`

## 安装到设备

### 通过 ADB
```bash
# 安装调试版
adb install app/build/outputs/apk/debug/app-debug.apk

# 覆盖安装
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### 手动安装
1. 将 APK 复制到手机
2. 在手机上点击安装
3. 允许"未知来源"安装

## 常见问题

### 1. Gradle 下载失败
**问题:** 构建时卡在下载 Gradle
**解决:** 
```bash
# 手动下载 Gradle
./gradlew wrapper --gradle-version 8.4 --distribution-type bin
```

### 2. 内存不足
**问题:** 构建时报内存错误
**解决:**
```bash
# 增加 Gradle 内存
export GRADLE_OPTS="-Xmx4g"
```

### 3. SDK 组件缺失
**问题:** 找不到 SDK 组件
**解决:**
```bash
# 安装缺失组件
sdkmanager "platforms;android-34" "build-tools;34.0.0"
```

### 4. 构建失败
**问题:** 构建失败但不知道原因
**解决:**
```bash
# 清理并重新构建
./gradlew clean
./gradlew assembleDebug --stacktrace
```

## CI/CD 集成

### GitHub Actions
项目已包含 `.github/workflows/build.yml`，推送代码后自动构建。

### GitLab CI
创建 `.gitlab-ci.yml`:
```yaml
stages:
  - build

build_apk:
  stage: build
  image: openjdk:17-jdk
  before_script:
    - apt-get update && apt-get install -y android-sdk
  script:
    - ./gradlew assembleDebug
  artifacts:
    paths:
      - app/build/outputs/apk/debug/
```

## 文件大小优化

当前 APK 大小预估: **~3-4 MB**

优化措施:
- ✅ 启用代码压缩 (ProGuard/R8)
- ✅ 启用资源压缩
- ✅ 使用矢量图标
- ✅ 最小化依赖

## 下一步

构建完成后:
1. 在设备上测试 APK
2. 发布到应用商店
3. 分享给你的用户

---

**需要帮助?** 查看 [BUILD_GUIDE.md](BUILD_GUIDE.md) 获取更详细的说明。
