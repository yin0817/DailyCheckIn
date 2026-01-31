package com.dailycheckin.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.dailycheckin.MainActivity
import com.dailycheckin.R
import com.dailycheckin.data.CheckInDataStore
import com.dailycheckin.util.NotificationHelper
import java.util.Calendar

/**
 * 前台服务 - 确保提醒能准时触发
 * 即使应用被关闭也能保持运行
 */
class ReminderService : Service() {
    
    companion object {
        const val SERVICE_CHANNEL_ID = "reminder_service_channel"
        const val SERVICE_NOTIFICATION_ID = 1002
        const val ACTION_REFRESH = "com.dailycheckin.ACTION_REFRESH_NOTIFICATION"
        private const val TAG = "ReminderService"
        private const val PREFS_NAME = "reminder_service_prefs"
        private const val KEY_NOTIFIED_TODAY = "notified_today"
        private const val KEY_LAST_NOTIFIED_DATE = "last_notified_date"
        
        fun start(context: Context) {
            try {
                val intent = Intent(context, ReminderService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
                Log.d(TAG, "Service start requested")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start service", e)
            }
        }
        
        fun stop(context: Context) {
            try {
                val intent = Intent(context, ReminderService::class.java)
                context.stopService(intent)
                Log.d(TAG, "Service stop requested")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to stop service", e)
            }
        }
        
        /**
         * 立即刷新通知（打卡或设置后调用）
         */
        fun refresh(context: Context) {
            try {
                val intent = Intent(context, ReminderService::class.java).apply {
                    action = ACTION_REFRESH
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
                Log.d(TAG, "Service refresh requested")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to refresh service", e)
            }
        }
        
        fun isRunning(context: Context): Boolean {
            val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
            for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
                if (ReminderService::class.java.name == service.service.className) {
                    return true
                }
            }
            return false
        }
    }
    
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var dataStore: CheckInDataStore
    private lateinit var prefs: SharedPreferences
    private var wakeLock: PowerManager.WakeLock? = null
    
    // 精确定时提醒的 Runnable
    private var reminderRunnable: Runnable? = null
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service onCreate")
        dataStore = CheckInDataStore(this)
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        createServiceNotificationChannel()
        acquireWakeLock()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service onStartCommand, action=${intent?.action}")
        
        // 启动前台服务
        startForeground(SERVICE_NOTIFICATION_ID, createServiceNotification())
        
        // 处理刷新请求
        if (intent?.action == ACTION_REFRESH) {
            updateServiceNotification()
            // 重新安排提醒（时间可能已改变）
            scheduleExactReminder()
            return START_STICKY
        }
        
        // 安排精确提醒
        scheduleExactReminder()
        
        return START_STICKY // 服务被杀死后自动重启
    }
    
    /**
     * 安排精确时间点的提醒
     */
    private fun scheduleExactReminder() {
        // 移除之前的定时任务
        reminderRunnable?.let { handler.removeCallbacks(it) }
        
        if (!dataStore.isReminderEnabledSync()) {
            Log.d(TAG, "Reminder disabled")
            return
        }
        
        // 如果今天已打卡，安排明天的提醒
        if (dataStore.isCheckedInToday()) {
            Log.d(TAG, "Already checked in today, scheduling for tomorrow")
            scheduleTomorrowReminder()
            return
        }
        
        val (targetHour, targetMinute) = dataStore.getReminderTimeSync()
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, targetHour)
            set(Calendar.MINUTE, targetMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        val delayMs = target.timeInMillis - now.timeInMillis
        
        when {
            delayMs > 0 -> {
                // 还没到提醒时间，安排精确定时
                Log.d(TAG, "Scheduling reminder in ${delayMs}ms (${delayMs/1000}s)")
                reminderRunnable = Runnable {
                    triggerReminder()
                }
                handler.postDelayed(reminderRunnable!!, delayMs)
            }
            else -> {
                // 已过提醒时间，检查是否需要立即提醒
                checkAndNotifyIfNeeded()
                // 安排明天的提醒
                scheduleTomorrowReminder()
            }
        }
    }
    
    /**
     * 安排明天的提醒
     */
    private fun scheduleTomorrowReminder() {
        val (targetHour, targetMinute) = dataStore.getReminderTimeSync()
        val now = Calendar.getInstance()
        val tomorrow = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, targetHour)
            set(Calendar.MINUTE, targetMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        val delayMs = tomorrow.timeInMillis - now.timeInMillis
        Log.d(TAG, "Scheduling tomorrow's reminder in ${delayMs/1000/60} minutes")
        
        reminderRunnable = Runnable {
            triggerReminder()
        }
        handler.postDelayed(reminderRunnable!!, delayMs)
    }
    
    /**
     * 触发提醒（精确时间点到达时调用）
     */
    private fun triggerReminder() {
        Log.d(TAG, "Trigger reminder NOW!")
        
        if (!dataStore.isReminderEnabledSync()) return
        
        if (!dataStore.isCheckedInToday()) {
            // 发送通知
            NotificationHelper.showNotification(this)
            
            // 记录已通知
            val today = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
            prefs.edit()
                .putBoolean(KEY_NOTIFIED_TODAY, true)
                .putInt(KEY_LAST_NOTIFIED_DATE, today)
                .apply()
        }
        
        // 安排明天的提醒
        scheduleTomorrowReminder()
    }
    
    /**
     * 检查是否需要补发通知（服务重启时使用）
     */
    private fun checkAndNotifyIfNeeded() {
        val today = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        val lastNotifiedDate = prefs.getInt(KEY_LAST_NOTIFIED_DATE, -1)
        val notifiedToday = prefs.getBoolean(KEY_NOTIFIED_TODAY, false) && lastNotifiedDate == today
        
        if (!notifiedToday && !dataStore.isCheckedInToday()) {
            Log.d(TAG, "Missed reminder, sending now")
            NotificationHelper.showNotification(this)
            prefs.edit()
                .putBoolean(KEY_NOTIFIED_TODAY, true)
                .putInt(KEY_LAST_NOTIFIED_DATE, today)
                .apply()
        }
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        Log.d(TAG, "Service onDestroy")
        super.onDestroy()
        reminderRunnable?.let { handler.removeCallbacks(it) }
        releaseWakeLock()
    }
    
    override fun onTaskRemoved(rootIntent: Intent?) {
        Log.d(TAG, "Service onTaskRemoved - restarting")
        // 当用户从最近任务中滑掉应用时，重新启动服务
        val restartIntent = Intent(applicationContext, ReminderService::class.java)
        val pendingIntent = PendingIntent.getService(
            applicationContext,
            1,
            restartIntent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
        alarmManager.set(
            android.app.AlarmManager.ELAPSED_REALTIME_WAKEUP,
            android.os.SystemClock.elapsedRealtime() + 1000,
            pendingIntent
        )
        super.onTaskRemoved(rootIntent)
    }
    
    private fun acquireWakeLock() {
        try {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "DailyCheckIn::ReminderWakeLock"
            ).apply {
                acquire(10 * 60 * 1000L) // 10分钟
            }
            Log.d(TAG, "WakeLock acquired")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to acquire WakeLock", e)
        }
    }
    
    private fun releaseWakeLock() {
        try {
            wakeLock?.let {
                if (it.isHeld) {
                    it.release()
                    Log.d(TAG, "WakeLock released")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to release WakeLock", e)
        }
    }
    
    
    /**
     * 创建服务通知渠道
     */
    private fun createServiceNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                SERVICE_CHANNEL_ID,
                getString(R.string.service_channel_name),
                NotificationManager.IMPORTANCE_LOW // 低优先级，显示完整内容但不发声
            ).apply {
                description = getString(R.string.service_channel_description)
                setShowBadge(false)
                enableLights(false)
                enableVibration(false)
                setSound(null, null)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * 创建前台服务通知
     */
    private fun createServiceNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // 获取连续签到天数和提醒时间
        val consecutiveDays = dataStore.getConsecutiveDays()
        val (hour, minute) = dataStore.getReminderTimeSync()
        val timeStr = String.format("%02d:%02d", hour, minute)
        
        // 简洁显示：连续天数 · 提醒时间
        val text = if (consecutiveDays > 0) {
            "连续 $consecutiveDays 天 · $timeStr 提醒"
        } else {
            "$timeStr 提醒"
        }
        
        return NotificationCompat.Builder(this, SERVICE_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_my_calendar)
            .setContentTitle(text)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setSilent(true)
            .setShowWhen(false)
            .setContentIntent(pendingIntent)
            .build()
    }
    
    /**
     * 更新前台服务通知
     */
    private fun updateServiceNotification() {
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(SERVICE_NOTIFICATION_ID, createServiceNotification())
    }
}
