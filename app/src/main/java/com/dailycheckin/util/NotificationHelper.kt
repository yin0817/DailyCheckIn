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
        
        val notification = androidx.core.app.NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_my_calendar)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(context.getString(R.string.notification_content))
            // 高优先级 - 会显示横幅通知（Heads-up）
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_MAX)
            .setCategory(androidx.core.app.NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            // 设置默认震动和声音（Android 8.0 以下有效，8.0+ 由通知渠道控制）
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
            // 设置为持续通知，直到用户交互（不会自动消失）
            .setOngoing(false)
            // 添加大文本样式
            .setStyle(
                androidx.core.app.NotificationCompat.BigTextStyle()
                    .bigText(context.getString(R.string.notification_content_expanded))
            )
            .build()
        
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    
    fun cancelNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
    }
}
