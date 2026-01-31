package com.dailycheckin.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.dailycheckin.data.CheckInDataStore
import com.dailycheckin.service.ReminderService
import com.dailycheckin.util.NotificationHelper

class BootReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "BootReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "Boot completed, setting up reminders")
            
            try {
                val dataStore = CheckInDataStore(context)
                
                // 设备重启后重新设置提醒
                if (dataStore.isReminderEnabledSync()) {
                    Log.d(TAG, "Scheduling daily reminder")
                    NotificationHelper.scheduleDailyReminder(context)
                }
                
                // 启动前台服务（默认开启）
                if (dataStore.isEnhancedReminderEnabledSync()) {
                    Log.d(TAG, "Starting ReminderService")
                    ReminderService.start(context)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in BootReceiver", e)
            }
        }
    }
}
