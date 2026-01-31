# 每日打卡 - 构建指南

## 环境准备

### 1. 安装 Android Studio
- 下载地址：https://developer.android.com/studio
- 推荐版本：Android Studio Hedgehog (2023.1.1) 或更高

### 2. 配置 JDK
- 推荐 JDK 17
- 在 Android Studio 中设置：File → Settings → Build → Build Tools → Gradle → Gradle JDK

### 3. 安装 Android SDK
- 通过 Android Studio 的 SDK Manager 安装
- 必需组件：
  - Android SDK Platform 34
  - Android SDK Build-Tools 34
  - Android SDK Command-line Tools

## 打开项目

1. 启动 Android Studio
2. 选择 "Open" 或 "File" → "Open"
3. 选择 `DailyCheckIn` 项目文件夹
4. 等待 Gradle 同步完成（首次可能需要几分钟）

## 构建 APK

### 方法 1：使用 Android Studio GUI
1. 点击菜单栏 "Build"
2. 选择 "Build Bundle(s) / APK(s)"
3. 选择 "Build APK(s)"
4. 构建完成后，右下角会显示通知，点击 "locate" 查看 APK 文件

### 方法 2：使用命令行

```bash
# 进入项目目录
cd DailyCheckIn

# 构建调试版本
./gradlew assembleDebug

# 构建发布版本
./gradlew assembleRelease
```

### 输出位置
- 调试版：`app/build/outputs/apk/debug/app-debug.apk`
- 发布版：`app/build/outputs/apk/release/app-release.apk`

## 安装到设备

### 方法 1：使用 Android Studio
1. 连接设备或启动模拟器
2. 点击工具栏的 "Run" 按钮（绿色三角形）
3. 选择目标设备

### 方法 2：使用 ADB 命令
```bash
# 安装调试版
adb install app/build/outputs/apk/debug/app-debug.apk

# 安装发布版
adb install app/build/outputs/apk/release/app-release.apk
```

## 生成签名发布版

### 1. 创建密钥库
```bash
keytool -genkey -v -keystore dailycheckin.keystore -alias dailycheckin -keyalg RSA -keysize 2048 -validity 10000
```

### 2. 配置签名
在 `app/build.gradle` 中添加：
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

### 3. 构建签名 APK
```bash
./gradlew assembleRelease
```

## 常见问题

### Gradle 同步失败
1. 检查网络连接（需要访问 Maven Central 和 Google 仓库）
2. 尝试 File → Invalidate Caches → Invalidate and Restart
3. 检查 Gradle 版本兼容性

### 编译错误
1. 确保 Kotlin 插件版本与项目配置一致
2. 检查 Compose 编译器版本与 Kotlin 版本兼容
3. 清理并重建：Build → Clean Project → Rebuild Project

### 运行时崩溃
1. 检查 AndroidManifest.xml 中的权限声明
2. 确保所有 Activity 和 Receiver 已正确注册
3. 查看 Logcat 获取详细错误信息

## 项目依赖

主要依赖库版本：
- Kotlin: 1.9.22
- Compose BOM: 2024.02.00
- compileSdk: 34
- minSdk: 24
- targetSdk: 34

## 优化建议

### 减小 APK 体积
- 已启用 R8 代码压缩和资源压缩
- 使用 ProGuard 规则优化
- 矢量图标替代位图

### 提高启动速度
- 使用 Lazy 加载非关键组件
- 避免在 Application.onCreate 中执行耗时操作
- 使用 SplashScreen API（已实现）

## 测试检查清单

### 功能测试
- [ ] 首次启动正常
- [ ] 点击打卡成功
- [ ] 连续天数计算正确
- [ ] 日历显示正确
- [ ] 月份切换正常
- [ ] 提醒开关有效
- [ ] 时间设置生效
- [ ] 主题切换正常
- [ ] 清除数据功能正常

### 提醒测试
- [ ] 定时提醒触发
- [ ] 通知点击打开应用
- [ ] 通知快速打卡有效
- [ ] 设备重启后提醒恢复

### 兼容性测试
- [ ] Android 7.0 (API 24)
- [ ] Android 10 (API 29)
- [ ] Android 13 (API 33)
- [ ] Android 14 (API 34)
- [ ] 深色模式
- [ ] 不同屏幕尺寸

## 发布准备

1. 更新版本号（`app/build.gradle` 中的 versionCode 和 versionName）
2. 生成签名密钥库
3. 构建发布版 APK
4. 测试发布版功能
5. 准备应用商店素材（截图、描述等）

## 联系方式

如有问题，请通过以下方式联系：
- 邮箱：your.email@example.com
- GitHub Issues：项目仓库
