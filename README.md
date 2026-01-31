# 每日打卡 - Daily Check In

一款极简、无干扰、高可靠的每日打卡工具。

## 产品定位

- **极简设计**：无目标、无分类、无复杂逻辑
- **每日一次**：仅需点击一次完成打卡
- **系统提醒**：即使 App 被关闭也能准时推送通知
- **离线可用**：所有数据本地存储，无需网络
- **轻量隐私**：不收集任何信息，无广告，无第三方 SDK

## 核心功能

### 1. 主界面
- 中央大按钮：
  - 未打卡时：显示"点击打卡"（高亮可点）
  - 已打卡时：显示"今日已打卡"（灰色不可点）+ 连续打卡天数
- 底部入口：打卡日历

### 2. 打卡日历
- 月视图展示历史记录
- 已打卡日期以实心圆/绿色标记
- 支持左右滑动切换月份
- 显示本月打卡统计

### 3. 每日提醒
- 默认提醒时间：20:00（可自定义）
- 即使 App 被系统杀死也能准时推送
- 通知内容："别忘了今日打卡 ✅"
- 支持快速打卡（点击通知按钮直接打卡）
- 支持关闭提醒

### 4. 设置
- 开启/关闭每日提醒
- 自定义提醒时间（时间选择器）
- 切换主题（浅色 / 深色 / 跟随系统）
- 清除所有打卡数据（二次确认）
- 电池优化引导

## 技术实现

### 定时提醒机制
- 使用 `AlarmManager` 设置精确每日闹钟
- 配合 `setExactAndAllowWhileIdle` 确保 Doze 模式下也能触发
- 每次打卡或收到闹钟后重新设置次日闹钟

### 设备重启恢复
- 监听 `BOOT_COMPLETED` 广播
- 重启后自动重新注册明日提醒闹钟

### 对抗国产 ROM 限制
- 检测是否被"电池优化"
- 首次启动引导用户关闭电池优化
- 提供跳转系统设置的快捷入口
- 针对小米、华为、OPPO、vivo 等厂商优化

### 数据存储
- 使用 Jetpack DataStore 存储偏好设置
- 按日期存储打卡记录
- 连续天数通过遍历最近日期计算

### 通知与交互
- 本地通知（NotificationManager）
- 点击通知打开主 Activity
- 支持从通知快速打卡

## 权限说明

所需权限极少：
- `WAKE_LOCK` - 保持短暂唤醒
- `RECEIVE_BOOT_COMPLETED` - 开机恢复
- `POST_NOTIFICATIONS` - 发送通知（Android 13+）
- `SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM` - 精确闹钟
- `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` - 引导关闭电池优化

**不申请**：网络、位置、存储、电话等任何敏感权限

## 兼容性

- 支持 Android 7.0（API 24）及以上
- 适配全面屏、深色模式、不同 DPI 屏幕
- 针对主流国产手机优化后台存活策略

## 项目结构

```
DailyCheckIn/
├── app/
│   ├── src/main/java/com/dailycheckin/
│   │   ├── MainActivity.kt              # 主Activity
│   │   ├── DailyCheckInApp.kt           # Application类
│   │   ├── data/
│   │   │   └── CheckInDataStore.kt      # 数据存储
│   │   ├── receiver/
│   │   │   ├── AlarmReceiver.kt         # 闹钟接收器
│   │   │   ├── BootReceiver.kt          # 开机启动接收器
│   │   │   └── NotificationClickReceiver.kt  # 通知点击接收器
│   │   ├── ui/
│   │   │   ├── screens/
│   │   │   │   ├── HomeScreen.kt        # 主界面
│   │   │   │   ├── CalendarScreen.kt    # 日历界面
│   │   │   │   └── SettingsScreen.kt    # 设置界面
│   │   │   └── theme/
│   │   │       ├── Theme.kt             # 主题定义
│   │   │       └── Type.kt              # 字体样式
│   │   └── util/
│   │       ├── NotificationHelper.kt    # 通知帮助类
│   │       └── BatteryOptimizationHelper.kt  # 电池优化帮助类
│   └── src/main/res/                    # 资源文件
├── build.gradle                         # 项目级构建配置
├── settings.gradle                      # 项目设置
└── gradle.properties                    # Gradle配置
```

## 构建说明

### 环境要求
- Android Studio Hedgehog (2023.1.1) 或更高
- JDK 17
- Android SDK 34

### 构建步骤
1. 使用 Android Studio 打开项目
2. 等待 Gradle 同步完成
3. 点击 "Build" → "Build Bundle(s) / APK(s)" → "Build APK(s)"
4. 或使用命令行：
   ```bash
   ./gradlew assembleDebug    # 构建调试版
   ./gradlew assembleRelease  # 构建发布版
   ```

### 输出位置
- 调试版：`app/build/outputs/apk/debug/app-debug.apk`
- 发布版：`app/build/outputs/apk/release/app-release.apk`

## 发布版本特性

- ✅ 启动快：冷启动 < 500ms
- ✅ 体积小：APK < 5MB
- ✅ 零广告：纯净体验
- ✅ 无障碍支持：基础 TalkBack 兼容

## 适用人群

- 想培养"每日存在感"的独居者
- 需要简单仪式感的心理健康关注者
- 厌倦复杂打卡 App 的极简主义者
- 作为"签到锚点"辅助其他习惯养成

## 隐私承诺

- 所有数据仅存储在设备本地
- 永不上传任何数据
- 无需网络权限
- 无第三方 SDK

## 开源协议

MIT License
