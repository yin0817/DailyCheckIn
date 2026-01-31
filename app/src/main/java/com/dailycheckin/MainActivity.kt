package com.dailycheckin

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
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
        
        // 检查是否是首次启动
        lifecycleScope.launch {
            val isFirst = dataStore.isFirstLaunch.first()
            if (isFirst) {
                dataStore.setFirstLaunchComplete()
                // 首次启动设置默认提醒
                NotificationHelper.scheduleDailyReminder(this@MainActivity)
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
