package com.dailycheckin.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "checkin_prefs")

class CheckInDataStore(private val context: Context) {
    
    companion object {
        private val CHECK_IN_PREFIX = stringSetPreferencesKey("check_in_dates")
        private val REMINDER_ENABLED = booleanPreferencesKey("reminder_enabled")
        private val REMINDER_HOUR = intPreferencesKey("reminder_hour")
        private val REMINDER_MINUTE = intPreferencesKey("reminder_minute")
        private val THEME_MODE = stringPreferencesKey("theme_mode")
        private val FIRST_LAUNCH = booleanPreferencesKey("first_launch")
        
        const val DEFAULT_HOUR = 20
        const val DEFAULT_MINUTE = 0
        const val THEME_LIGHT = "light"
        const val THEME_DARK = "dark"
        const val THEME_SYSTEM = "system"
    }
    
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    
    // 获取所有打卡日期
    val checkInDates: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[CHECK_IN_PREFIX] ?: emptySet()
    }
    
    // 同步获取打卡日期（用于非协程环境）
    fun getCheckInDatesSync(): Set<String> = runBlocking {
        checkInDates.first()
    }
    
    // 检查某天是否已打卡
    fun isCheckedIn(date: LocalDate): Boolean {
        val dates = getCheckInDatesSync()
        return dates.contains(date.format(dateFormatter))
    }
    
    // 检查今天是否已打卡
    fun isCheckedInToday(): Boolean {
        return isCheckedIn(LocalDate.now())
    }
    
    // 添加打卡记录
    suspend fun addCheckIn(date: LocalDate = LocalDate.now()) {
        context.dataStore.edit { prefs ->
            val currentDates = prefs[CHECK_IN_PREFIX] ?: emptySet()
            prefs[CHECK_IN_PREFIX] = currentDates + date.format(dateFormatter)
        }
    }
    
    // 取消打卡（用于测试）
    suspend fun removeCheckIn(date: LocalDate) {
        context.dataStore.edit { prefs ->
            val currentDates = prefs[CHECK_IN_PREFIX] ?: emptySet()
            prefs[CHECK_IN_PREFIX] = currentDates - date.format(dateFormatter)
        }
    }
    
    // 计算连续打卡天数
    fun getConsecutiveDays(): Int {
        val dates = getCheckInDatesSync().map { 
            LocalDate.parse(it, dateFormatter) 
        }.sortedDescending()
        
        if (dates.isEmpty()) return 0
        
        var consecutive = 0
        var currentDate = LocalDate.now()
        
        // 如果今天没打卡，从昨天开始算
        if (!dates.contains(currentDate)) {
            currentDate = currentDate.minusDays(1)
        }
        
        for (date in dates) {
            if (date == currentDate.minusDays(consecutive.toLong())) {
                consecutive++
            } else {
                break
            }
        }
        
        return consecutive
    }
    
    // 获取某月的打卡记录
    fun getCheckInDatesForMonth(year: Int, month: Int): Set<LocalDate> {
        val dates = getCheckInDatesSync()
        return dates.mapNotNull { 
            try {
                LocalDate.parse(it, dateFormatter)
            } catch (e: Exception) {
                null
            }
        }.filter { 
            it.year == year && it.monthValue == month 
        }.toSet()
    }
    
    // 提醒设置
    val reminderEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[REMINDER_ENABLED] ?: true
    }
    
    fun isReminderEnabledSync(): Boolean = runBlocking {
        reminderEnabled.first()
    }
    
    suspend fun setReminderEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[REMINDER_ENABLED] = enabled
        }
    }
    
    // 提醒时间
    val reminderHour: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[REMINDER_HOUR] ?: DEFAULT_HOUR
    }
    
    val reminderMinute: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[REMINDER_MINUTE] ?: DEFAULT_MINUTE
    }
    
    fun getReminderTimeSync(): Pair<Int, Int> = runBlocking {
        val hour = reminderHour.first()
        val minute = reminderMinute.first()
        hour to minute
    }
    
    suspend fun setReminderTime(hour: Int, minute: Int) {
        context.dataStore.edit { prefs ->
            prefs[REMINDER_HOUR] = hour
            prefs[REMINDER_MINUTE] = minute
        }
    }
    
    // 主题设置
    val themeMode: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[THEME_MODE] ?: THEME_SYSTEM
    }
    
    fun getThemeModeSync(): String = runBlocking {
        themeMode.first()
    }
    
    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { prefs ->
            prefs[THEME_MODE] = mode
        }
    }
    
    // 首次启动
    val isFirstLaunch: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[FIRST_LAUNCH] ?: true
    }
    
    suspend fun setFirstLaunchComplete() {
        context.dataStore.edit { prefs ->
            prefs[FIRST_LAUNCH] = false
        }
    }
    
    // 清除所有数据
    suspend fun clearAllData() {
        context.dataStore.edit { prefs ->
            prefs.remove(CHECK_IN_PREFIX)
            // 保留设置，只清除打卡记录
        }
    }
}
