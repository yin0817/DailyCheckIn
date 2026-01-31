# 每日打卡 - 快速开始

## 5 分钟上手

### 1. 环境检查
确保已安装：
- ✅ Android Studio (2023.1.1+)
- ✅ JDK 17
- ✅ Android SDK 34

### 2. 打开项目
```bash
# 或者直接在 Android Studio 中选择 File → Open
```

### 3. 等待同步
首次打开会自动下载依赖，等待 Gradle 同步完成（约 2-5 分钟）

### 4. 运行项目
- 连接 Android 设备或启动模拟器
- 点击工具栏的绿色 "Run" 按钮
- 选择目标设备

### 5. 完成！
应用安装完成，开始体验极简打卡

## 核心代码说明

### 打卡逻辑
```kotlin
// 检查今天是否已打卡
fun isCheckedInToday(): Boolean

// 添加打卡记录
suspend fun addCheckIn(date: LocalDate = LocalDate.now())

// 获取连续打卡天数
fun getConsecutiveDays(): Int
```

### 提醒设置
```kotlin
// 设置每日提醒
NotificationHelper.scheduleDailyReminder(context)

// 取消提醒
NotificationHelper.cancelReminder(context)

// 显示通知
NotificationHelper.showNotification(context)
```

### 数据存储
```kotlin
// 使用 DataStore 存储
val checkInDates: Flow<Set<String>> // 打卡日期集合
val reminderEnabled: Flow<Boolean>   // 提醒开关
val reminderHour: Flow<Int>          // 提醒小时
val reminderMinute: Flow<Int>        // 提醒分钟
val themeMode: Flow<String>          // 主题模式
```

## 自定义修改

### 修改默认提醒时间
编辑 `CheckInDataStore.kt`：
```kotlin
const val DEFAULT_HOUR = 20    // 改为 21 表示晚上 9 点
const val DEFAULT_MINUTE = 0
```

### 修改主题颜色
编辑 `Theme.kt` 中的颜色值：
```kotlin
private val LightColors = lightColorScheme(
    primary = Color(0xFF4CAF50),  // 修改为主色调
    // ...
)
```

### 修改通知内容
编辑 `strings.xml`：
```xml
<string name="notification_title">你的自定义标题</string>
<string name="notification_content">你的自定义内容</string>
```

## 调试技巧

### 查看日志
```bash
# 过滤应用日志
adb logcat -s DailyCheckIn:D
```

### 快速测试提醒
修改 `AlarmReceiver.kt` 中的触发时间，或使用 ADB 模拟：
```bash
# 模拟闹钟触发
adb shell am broadcast -a android.intent.action.TIME_SET
```

### 清除数据测试
```bash
# 清除应用数据
adb shell pm clear com.dailycheckin
```

## 常见问题

**Q: 提醒不工作怎么办？**
A: 检查：
1. 是否关闭了电池优化
2. 是否允许自启动
3. 通知权限是否开启
4. 提醒开关是否打开

**Q: 如何测试开机恢复？**
A: 使用 ADB 模拟：
```bash
adb shell am broadcast -a android.intent.action.BOOT_COMPLETED
```

**Q: 如何修改最小支持版本？**
A: 编辑 `app/build.gradle`：
```gradle
defaultConfig {
    minSdk 21  // 改为需要的版本
}
```

## 下一步

- 📖 详细文档：[README.md](README.md)
- 🔧 构建指南：[BUILD_GUIDE.md](BUILD_GUIDE.md)
- 🎨 自定义主题和颜色
- 🚀 发布到应用商店
