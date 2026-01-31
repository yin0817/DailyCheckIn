package com.dailycheckin.ui.screens

import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dailycheckin.R
import com.dailycheckin.data.CheckInDataStore
import com.dailycheckin.service.ReminderService
import com.dailycheckin.util.BatteryOptimizationHelper
import com.dailycheckin.util.NotificationHelper
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    dataStore: CheckInDataStore,
    onNavigateBack: () -> Unit,
    onThemeChange: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var reminderEnabled by remember { mutableStateOf(dataStore.isReminderEnabledSync()) }
    var reminderHour by remember { mutableStateOf(dataStore.getReminderTimeSync().first) }
    var reminderMinute by remember { mutableStateOf(dataStore.getReminderTimeSync().second) }
    var themeMode by remember { mutableStateOf(dataStore.getThemeModeSync()) }
    var enhancedReminder by remember { mutableStateOf(dataStore.isEnhancedReminderEnabledSync()) }
    
    var showClearDataDialog by remember { mutableStateOf(false) }
    var showBatteryDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showEnhancedReminderDialog by remember { mutableStateOf(false) }
    
    val scrollState = rememberScrollState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
        ) {
            // 提醒设置
            SettingsSection(title = "提醒设置") {
                // 每日提醒开关
                SettingsSwitchItem(
                    title = stringResource(R.string.daily_reminder),
                    subtitle = if (reminderEnabled) "已开启" else "已关闭",
                    icon = Icons.Default.Notifications,
                    checked = reminderEnabled,
                    onCheckedChange = { enabled ->
                        reminderEnabled = enabled
                        scope.launch {
                            dataStore.setReminderEnabled(enabled)
                            if (enabled) {
                                NotificationHelper.scheduleDailyReminder(context)
                                ReminderService.refresh(context)
                            } else {
                                NotificationHelper.cancelReminder(context)
                            }
                        }
                    }
                )
                
                // 提醒时间
                AnimatedVisibility(visible = reminderEnabled) {
                    Column {
                        SettingsItem(
                            title = stringResource(R.string.reminder_time),
                            subtitle = String.format("%02d:%02d", reminderHour, reminderMinute),
                            icon = Icons.Default.Schedule,
                            onClick = {
                                TimePickerDialog(
                                    context,
                                    { _, hour, minute ->
                                        reminderHour = hour
                                        reminderMinute = minute
                                        scope.launch {
                                            dataStore.setReminderTime(hour, minute)
                                            NotificationHelper.scheduleDailyReminder(context)
                                            ReminderService.refresh(context)
                                        }
                                    },
                                    reminderHour,
                                    reminderMinute,
                                    true
                                ).show()
                            }
                        )
                        
                        // 增强提醒模式
                        SettingsSwitchItem(
                            title = stringResource(R.string.enhanced_reminder),
                            subtitle = if (enhancedReminder) 
                                stringResource(R.string.enhanced_reminder_subtitle_on) 
                            else 
                                stringResource(R.string.enhanced_reminder_subtitle_off),
                            icon = Icons.Default.Shield,
                            checked = enhancedReminder,
                            onCheckedChange = { enabled ->
                                if (enabled && !enhancedReminder) {
                                    // 首次开启时显示说明对话框
                                    showEnhancedReminderDialog = true
                                } else {
                                    enhancedReminder = enabled
                                    scope.launch {
                                        dataStore.setEnhancedReminder(enabled)
                                        if (enabled) {
                                            ReminderService.start(context)
                                        } else {
                                            ReminderService.stop(context)
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
            
            Divider()
            
            // 外观设置
            SettingsSection(title = "外观") {
                SettingsItem(
                    title = stringResource(R.string.theme),
                    subtitle = when (themeMode) {
                        CheckInDataStore.THEME_LIGHT -> stringResource(R.string.theme_light)
                        CheckInDataStore.THEME_DARK -> stringResource(R.string.theme_dark)
                        else -> stringResource(R.string.theme_system)
                    },
                    icon = Icons.Default.ColorLens,
                    onClick = { showThemeDialog = true }
                )
            }
            
            Divider()
            
            // 电池优化
            SettingsSection(title = "后台运行") {
                SettingsItem(
                    title = stringResource(R.string.battery_optimization_title),
                    subtitle = "确保提醒能正常工作",
                    icon = Icons.Default.BatteryFull,
                    onClick = { showBatteryDialog = true }
                )
            }
            
            Divider()
            
            // 数据管理
            SettingsSection(title = "数据管理") {
                SettingsItem(
                    title = stringResource(R.string.clear_data),
                    subtitle = "清除所有打卡记录",
                    icon = Icons.Default.Delete,
                    isDestructive = true,
                    onClick = { showClearDataDialog = true }
                )
            }
            
            Divider()
            
            // 关于
            SettingsSection(title = "关于") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.version, "1.0.0"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.privacy_note),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
    
    // 清除数据确认对话框
    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = { Text(stringResource(R.string.clear_data)) },
            text = { Text(stringResource(R.string.clear_data_confirm)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            dataStore.clearAllData()
                            showClearDataDialog = false
                        }
                    }
                ) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
    
    // 电池优化对话框
    if (showBatteryDialog) {
        AlertDialog(
            onDismissRequest = { showBatteryDialog = false },
            title = { Text(stringResource(R.string.battery_optimization_title)) },
            text = {
                Column {
                    Text(stringResource(R.string.battery_optimization_message))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        BatteryOptimizationHelper.getManufacturerTip(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        BatteryOptimizationHelper.openBatteryOptimizationSettings(context)
                        showBatteryDialog = false
                    }
                ) {
                    Text(stringResource(R.string.go_to_settings))
                }
            },
            dismissButton = {
                TextButton(onClick = { showBatteryDialog = false }) {
                    Text(stringResource(R.string.later))
                }
            }
        )
    }
    
    // 主题选择对话框
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text(stringResource(R.string.theme)) },
            text = {
                Column {
                    ThemeOption(
                        title = stringResource(R.string.theme_light),
                        selected = themeMode == CheckInDataStore.THEME_LIGHT,
                        onClick = {
                            themeMode = CheckInDataStore.THEME_LIGHT
                            scope.launch {
                                dataStore.setThemeMode(CheckInDataStore.THEME_LIGHT)
                                onThemeChange(CheckInDataStore.THEME_LIGHT)
                            }
                            showThemeDialog = false
                        }
                    )
                    ThemeOption(
                        title = stringResource(R.string.theme_dark),
                        selected = themeMode == CheckInDataStore.THEME_DARK,
                        onClick = {
                            themeMode = CheckInDataStore.THEME_DARK
                            scope.launch {
                                dataStore.setThemeMode(CheckInDataStore.THEME_DARK)
                                onThemeChange(CheckInDataStore.THEME_DARK)
                            }
                            showThemeDialog = false
                        }
                    )
                    ThemeOption(
                        title = stringResource(R.string.theme_system),
                        selected = themeMode == CheckInDataStore.THEME_SYSTEM,
                        onClick = {
                            themeMode = CheckInDataStore.THEME_SYSTEM
                            scope.launch {
                                dataStore.setThemeMode(CheckInDataStore.THEME_SYSTEM)
                                onThemeChange(CheckInDataStore.THEME_SYSTEM)
                            }
                            showThemeDialog = false
                        }
                    )
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
    
    // 增强提醒模式说明对话框
    if (showEnhancedReminderDialog) {
        AlertDialog(
            onDismissRequest = { showEnhancedReminderDialog = false },
            title = { Text(stringResource(R.string.enhanced_reminder)) },
            text = { Text(stringResource(R.string.enhanced_reminder_description)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        enhancedReminder = true
                        scope.launch {
                            dataStore.setEnhancedReminder(true)
                            ReminderService.start(context)
                        }
                        showEnhancedReminderDialog = false
                    }
                ) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showEnhancedReminderDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )
        content()
    }
}

@Composable
private fun SettingsItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SettingsSwitchItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun ThemeOption(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun stringResource(id: Int): String {
    return LocalContext.current.getString(id)
}

@Composable
private fun stringResource(id: Int, vararg formatArgs: Any): String {
    return LocalContext.current.getString(id, *formatArgs)
}
