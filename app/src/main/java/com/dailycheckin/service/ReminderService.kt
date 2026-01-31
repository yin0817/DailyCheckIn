package com.dailycheckin.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.dailycheckin.MainActivity
import com.dailycheckin.R
import com.dailycheckin.data.CheckInDataStore
import com.dailycheckin.util.NotificationHelper
import java.util.Calendar

/**
 * 前台服务 - 增强提醒模式
 * 通过保持服务存活来确保提醒能准时触发
 */
class ReminderService : Service() {
    
    companion object {
        const val SERVICE_CHANNEL_ID = "reminder_service_channel"
        const val SERVICE_NOTIFICATION_ID = 1002
        private const val CHECK_INTERVAL = 60 * 1000L // 每分钟检查一次
        
        fun start(context: Context) {
            val intent = Intent(context, ReminderService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
        
        fun stop(context: Context) {
            val intent = Intent(context, ReminderService::class.java)
            context.stopService(intent)
        }
    }
    
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var dataStore: CheckInDataStore
    
    private val checkRunnable = object : Runnable {
        override fun run() {
            checkAndNotify()
            handler.postDelayed(this, CHECK_INTERVAL)
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        dataStore = CheckInDataStore(this)
        createServiceNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 启动前台服务
        startForeground(SERVICE_NOTIFICATION_ID, createServiceNotification())
        
        // 开始定时检查
        handler.post(checkRunnable)
        
        return START_STICKY // 服务被杀死后自动重启
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(checkRunnable)
    }
    
    /**
     * 检查是否需要发送提醒通知
     */
    private fun checkAndNotify() {
        if (!dataStore.isReminderEnabledSync()) return
        
        val (targetHour, targetMinute) = dataStore.getReminderTimeSync()
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)
        
        // 检查是否到达提醒时间（允许1分钟误差）
        if (currentHour == targetHour && currentMinute == targetMinute) {
            // 检查今天是否已打卡
            if (!dataStore.isCheckedInToday()) {
                NotificationHelper.showNotification(this)
            }
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
                NotificationManager.IMPORTANCE_LOW // 低优先级，不发出声音
            ).apply {
                description = getString(R.string.service_channel_description)
                setShowBadge(false)
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
        
        return NotificationCompat.Builder(this, SERVICE_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_my_calendar)
            .setContentTitle(getString(R.string.service_notification_title))
            .setContentText(getString(R.string.service_notification_content))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }
}
