package com.dailycheckin

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.dailycheckin.util.NotificationHelper

class DailyCheckInApp : Application() {
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NotificationHelper.CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH  // 高优先级，会弹出横幅通知
            ).apply {
                description = getString(R.string.notification_channel_description)
                
                // 不强制设置声音和震动，跟随系统设置
                // 用户可以在系统设置 > 应用 > 通知中自定义
                
                // 锁屏可见性 - 完全显示
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                
                // 显示角标
                setShowBadge(true)
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
