package com.dailycheckin.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dailycheckin.data.CheckInDataStore
import com.dailycheckin.util.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        // 检查今天是否已经打卡
        val dataStore = CheckInDataStore(context)
        
        if (!dataStore.isCheckedInToday()) {
            // 今天还没打卡，显示通知
            NotificationHelper.showNotification(context)
        }
        
        // 重新设置明天的提醒
        NotificationHelper.scheduleDailyReminder(context)
    }
}
