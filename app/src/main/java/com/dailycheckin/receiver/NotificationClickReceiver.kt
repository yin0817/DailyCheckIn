package com.dailycheckin.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dailycheckin.data.CheckInDataStore
import com.dailycheckin.util.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationClickReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            NotificationHelper.ACTION_CHECK_IN -> {
                // 从通知快速打卡
                CoroutineScope(Dispatchers.IO).launch {
                    val dataStore = CheckInDataStore(context)
                    if (!dataStore.isCheckedInToday()) {
                        dataStore.addCheckIn()
                    }
                    // 取消通知
                    NotificationHelper.cancelNotification(context)
                    // 重新设置明天的提醒
                    NotificationHelper.scheduleDailyReminder(context)
                }
            }
        }
    }
}
