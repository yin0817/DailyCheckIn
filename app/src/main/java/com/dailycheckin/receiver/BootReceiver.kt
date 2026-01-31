package com.dailycheckin.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dailycheckin.data.CheckInDataStore
import com.dailycheckin.util.NotificationHelper

class BootReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // 设备重启后重新设置提醒
            val dataStore = CheckInDataStore(context)
            
            if (dataStore.isReminderEnabledSync()) {
                NotificationHelper.scheduleDailyReminder(context)
            }
        }
    }
}
