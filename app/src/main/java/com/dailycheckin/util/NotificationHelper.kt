package com.dailycheckin.util

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.dailycheckin.MainActivity
import com.dailycheckin.R
import com.dailycheckin.data.CheckInDataStore
import com.dailycheckin.receiver.AlarmReceiver
import com.dailycheckin.receiver.NotificationClickReceiver
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Calendar

object NotificationHelper {
    const val CHANNEL_ID = "daily_checkin_reminder"
    const val NOTIFICATION_ID = 1001
    const val ACTION_CHECK_IN = "com.dailycheckin.ACTION_CHECK_IN"
    
    fun scheduleDailyReminder(context: Context) {
        val dataStore = CheckInDataStore(context)
        
        if (!dataStore.isReminderEnabledSync()) {
            cancelReminder(context)
            return
        }
        
        val (hour, minute) = dataStore.getReminderTimeSync()
        
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // 设置明天的提醒时间
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            
            // 如果今天的时间已过，设置为明天
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        
        // 使用精确的闹钟
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                // 如果不能设置精确闹钟，使用不精确的
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }
    
    fun cancelReminder(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
    
    fun showNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val dataStore = CheckInDataStore(context)
        
        // 获取连续签到天数
        val consecutiveDays = dataStore.getConsecutiveDays()
        
        // 创建打开应用的Intent
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("from_notification", true)
        }
        
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            0,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // 全屏Intent（用于锁屏时显示）
        val fullScreenIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("from_notification", true)
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            2,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // 创建快速打卡的Intent
        val checkInIntent = Intent(context, NotificationClickReceiver::class.java).apply {
            action = ACTION_CHECK_IN
        }
        
        val checkInPendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            checkInIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // 根据连续签到天数生成不同的提示文本
        val (title, content) = getNotificationText(context, consecutiveDays)
        
        val notification = androidx.core.app.NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_my_calendar)
            .setContentTitle(title)
            .setContentText(content)
            // 高优先级 - 会显示横幅通知（Heads-up）
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_MAX)
            .setCategory(androidx.core.app.NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            // 设置默认震动和声音
            .setDefaults(androidx.core.app.NotificationCompat.DEFAULT_ALL)
            // 震动模式
            .setVibrate(longArrayOf(0, 500, 200, 500, 200, 500))
            // 锁屏可见性
            .setVisibility(androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC)
            // 全屏Intent - 在锁屏时可以弹出
            .setFullScreenIntent(fullScreenPendingIntent, true)
            // 添加快速打卡按钮
            .addAction(
                android.R.drawable.ic_menu_save,
                context.getString(R.string.notification_check_in),
                checkInPendingIntent
            )
            // 添加大文本样式，显示连续签到信息
            .setStyle(
                androidx.core.app.NotificationCompat.BigTextStyle()
                    .bigText(getExpandedText(context, consecutiveDays))
                    .setBigContentTitle(title)
            )
            // 设置子文本显示连续天数
            .setSubText(if (consecutiveDays > 0) "🔥 已连续 $consecutiveDays 天" else null)
            .build()
        
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    
    /**
     * 根据连续签到天数生成通知标题和内容
     */
    private fun getNotificationText(context: Context, consecutiveDays: Int): Pair<String, String> {
        return when {
            consecutiveDays >= 30 -> Pair(
                "🏆 坚持就是胜利！",
                "已连续打卡 ${consecutiveDays} 天，今天继续保持！"
            )
            consecutiveDays >= 7 -> Pair(
                "💪 一周达成！别断了！",
                "已连续打卡 ${consecutiveDays} 天，快来延续记录！"
            )
            consecutiveDays >= 3 -> Pair(
                "🔥 连续 ${consecutiveDays} 天，继续加油！",
                "好习惯正在养成，点击完成今日打卡"
            )
            consecutiveDays > 0 -> Pair(
                "⏰ 打卡提醒",
                "已连续 ${consecutiveDays} 天，别让记录归零！"
            )
            else -> Pair(
                context.getString(R.string.notification_title),
                context.getString(R.string.notification_content)
            )
        }
    }
    
    /**
     * 获取展开后的详细文本
     */
    private fun getExpandedText(context: Context, consecutiveDays: Int): String {
        return when {
            consecutiveDays >= 100 -> "🎊 太厉害了！你已经连续打卡 ${consecutiveDays} 天！\n继续保持，你就是坚持的榜样！"
            consecutiveDays >= 30 -> "🌟 一个月的坚持！已连续 ${consecutiveDays} 天！\n今天也要完成打卡哦～"
            consecutiveDays >= 7 -> "✨ 一周习惯养成中！已连续 ${consecutiveDays} 天\n点击通知或下方按钮快速打卡"
            consecutiveDays > 0 -> "📅 已连续打卡 ${consecutiveDays} 天\n坚持每天打卡，养成好习惯！"
            else -> context.getString(R.string.notification_content_expanded)
        }
    }
    
    fun cancelNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
    }
}
