package com.dailycheckin

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import com.dailycheckin.util.NotificationHelper

class DailyCheckInApp : Application() {
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 获取默认通知铃声
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            
            // 音频属性 - 用于通知
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()
            
            val channel = NotificationChannel(
                NotificationHelper.CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH  // 高优先级，会弹出横幅通知
            ).apply {
                description = getString(R.string.notification_channel_description)
                
                // 启用震动
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 500)  // 震动模式
                
                // 设置声音
                setSound(soundUri, audioAttributes)
                
                // 启用呼吸灯
                enableLights(true)
                lightColor = android.graphics.Color.GREEN
                
                // 锁屏可见性 - 完全显示
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                
                // 允许绕过勿扰模式（用户可在系统设置中修改）
                setBypassDnd(false)
                
                // 显示角标
                setShowBadge(true)
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
