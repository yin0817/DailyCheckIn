package com.dailycheckin

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dailycheckin.data.CheckInDataStore
import com.dailycheckin.service.ReminderService
import com.dailycheckin.ui.screens.*
import com.dailycheckin.ui.theme.DailyCheckInTheme
import com.dailycheckin.util.BatteryOptimizationHelper
import com.dailycheckin.util.NotificationHelper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    
    private lateinit var dataStore: CheckInDataStore
    private var themeMode by mutableStateOf(CheckInDataStore.THEME_SYSTEM)
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // 通知权限请求结果
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        dataStore = CheckInDataStore(this)
        
        // 加载主题设置
        lifecycleScope.launch {
            themeMode = dataStore.themeMode.first()
        }
        
        // 请求通知权限（Android 13+）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == 
                    PackageManager.PERMISSION_GRANTED -> {
                    // 已有权限
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    // 显示解释为什么需要通知权限
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
        
        // 检查是否是首次启动，并设置提醒
        lifecycleScope.launch {
            val isFirst = dataStore.isFirstLaunch.first()
            if (isFirst) {
                dataStore.setFirstLaunchComplete()
                // 首次启动默认开启增强提醒
                dataStore.setEnhancedReminder(true)
            }
            
            // 每次启动都重新设置提醒（因为闹钟可能被系统清除）
            if (dataStore.isReminderEnabledSync()) {
                NotificationHelper.scheduleDailyReminder(this@MainActivity)
            }
            
            // 启动前台服务确保提醒可靠（默认开启）
            if (dataStore.isEnhancedReminderEnabledSync()) {
                ReminderService.start(this@MainActivity)
            }
        }
        
        setContent {
            DailyCheckInTheme(themeMode = themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(
                        dataStore = dataStore,
                        onThemeChange = { newTheme ->
                            themeMode = newTheme
                        }
                    )
                }
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        // 刷新主题设置
        lifecycleScope.launch {
            themeMode = dataStore.themeMode.first()
        }
        
        // 每次恢复时检查并重新设置提醒
        lifecycleScope.launch {
            if (dataStore.isReminderEnabledSync()) {
                NotificationHelper.scheduleDailyReminder(this@MainActivity)
            }
            
            // 确保前台服务运行
            if (dataStore.isEnhancedReminderEnabledSync()) {
                if (!ReminderService.isRunning(this@MainActivity)) {
                    ReminderService.start(this@MainActivity)
                }
            }
        }
    }
    
    /**
     * 检查是否有精确闹钟权限（Android 12+）
     */
    private fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }
    
    /**
     * 打开精确闹钟权限设置页面（Android 12+）
     */
    private fun requestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            startActivity(intent)
        }
    }
}

@Composable
fun AppNavigation(
    dataStore: CheckInDataStore,
    onThemeChange: (String) -> Unit
) {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                dataStore = dataStore,
                onNavigateToCalendar = {
                    navController.navigate("calendar")
                },
                onNavigateToSettings = {
                    navController.navigate("settings")
                }
            )
        }
        
        composable("calendar") {
            CalendarScreen(
                dataStore = dataStore,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable("settings") {
            SettingsScreen(
                dataStore = dataStore,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onThemeChange = onThemeChange
            )
        }
    }
}
