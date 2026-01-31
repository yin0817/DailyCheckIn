package com.dailycheckin.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.util.Log
import com.dailycheckin.data.CheckInDataStore
import com.dailycheckin.service.ReminderService
import com.dailycheckin.util.NotificationHelper

class AlarmReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "AlarmReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "AlarmReceiver triggered")
        
        // 获取 WakeLock 确保设备保持唤醒状态完成操作
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "DailyCheckIn::AlarmWakeLock"
        )
        wakeLock.acquire(60 * 1000L) // 1分钟
        
        try {
            val dataStore = CheckInDataStore(context)
            
            // 检查今天是否已经打卡
            if (!dataStore.isCheckedInToday()) {
                Log.d(TAG, "Not checked in today, showing notification")
                // 今天还没打卡，显示通知
                NotificationHelper.showNotification(context)
            } else {
                Log.d(TAG, "Already checked in today")
            }
            
            // 重新设置明天的提醒
            NotificationHelper.scheduleDailyReminder(context)
            
            // 确保前台服务运行
            if (dataStore.isEnhancedReminderEnabledSync()) {
                ReminderService.start(context)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in AlarmReceiver", e)
        } finally {
            // 释放 WakeLock
            if (wakeLock.isHeld) {
                wakeLock.release()
            }
        }
    }
}
