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
        private const val CHECK_INTERVAL = 30 * 1000L // 每30秒检查一次
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
    
    private val checkRunnable = object : Runnable {
        override fun run() {
            checkAndNotify()
            handler.postDelayed(this, CHECK_INTERVAL)
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service onCreate")
        dataStore = CheckInDataStore(this)
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        createServiceNotificationChannel()
        acquireWakeLock()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service onStartCommand")
        // 启动前台服务
        startForeground(SERVICE_NOTIFICATION_ID, createServiceNotification())
        
        // 开始定时检查
        handler.removeCallbacks(checkRunnable)
        handler.post(checkRunnable)
        
        return START_STICKY // 服务被杀死后自动重启
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        Log.d(TAG, "Service onDestroy")
        super.onDestroy()
        handler.removeCallbacks(checkRunnable)
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
     * 检查是否需要发送提醒通知
     */
    private fun checkAndNotify() {
        try {
            if (!dataStore.isReminderEnabledSync()) {
                Log.d(TAG, "Reminder disabled")
                return
            }
            
            // 检查今天是否已经打卡
            if (dataStore.isCheckedInToday()) {
                Log.d(TAG, "Already checked in today")
                resetNotifiedToday()
                return
            }
            
            val (targetHour, targetMinute) = dataStore.getReminderTimeSync()
            val calendar = Calendar.getInstance()
            val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
            val currentMinute = calendar.get(Calendar.MINUTE)
            val today = calendar.get(Calendar.DAY_OF_YEAR)
            
            // 检查今天是否已经发送过通知
            val lastNotifiedDate = prefs.getInt(KEY_LAST_NOTIFIED_DATE, -1)
            val notifiedToday = prefs.getBoolean(KEY_NOTIFIED_TODAY, false) && lastNotifiedDate == today
            
            Log.d(TAG, "Check: current=$currentHour:$currentMinute, target=$targetHour:$targetMinute, notifiedToday=$notifiedToday")
            
            // 判断是否已过提醒时间
            val isPastReminderTime = currentHour > targetHour || 
                (currentHour == targetHour && currentMinute >= targetMinute)
            
            // 如果已过提醒时间且今天还没发送过通知
            if (isPastReminderTime && !notifiedToday) {
                Log.d(TAG, "Sending notification!")
                NotificationHelper.showNotification(this)
                
                // 记录已发送通知
                prefs.edit()
                    .putBoolean(KEY_NOTIFIED_TODAY, true)
                    .putInt(KEY_LAST_NOTIFIED_DATE, today)
                    .apply()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in checkAndNotify", e)
        }
    }
    
    private fun resetNotifiedToday() {
        prefs.edit().putBoolean(KEY_NOTIFIED_TODAY, false).apply()
    }
    
    /**
     * 创建服务通知渠道
     */
    private fun createServiceNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                SERVICE_CHANNEL_ID,
                getString(R.string.service_channel_name),
                NotificationManager.IMPORTANCE_MIN // 最低优先级，不发出声音，最小化显示
            ).apply {
                description = getString(R.string.service_channel_description)
                setShowBadge(false)
                enableLights(false)
                enableVibration(false)
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
        
        val contentText = if (consecutiveDays > 0) {
            "🔥 已连续 $consecutiveDays 天 · 提醒时间 $timeStr"
        } else {
            "提醒时间 $timeStr"
        }
        
        return NotificationCompat.Builder(this, SERVICE_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_my_calendar)
            .setContentTitle(getString(R.string.service_notification_title))
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setOngoing(true)
            .setSilent(true)
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
