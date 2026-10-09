package com.dailycheckin.ui.screens

import android.app.TimePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.dailycheckin.R
import com.dailycheckin.data.CheckInDataStore
import com.dailycheckin.service.ReminderService
import com.dailycheckin.util.BatteryOptimizationHelper
import com.dailycheckin.util.NotificationHelper
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    dataStore: CheckInDataStore,
    onNavigateBack: () -> Unit,
    onThemeChange: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    var reminderEnabled by remember { mutableStateOf(dataStore.isReminderEnabledSync()) }
    var reminderHour by remember { mutableStateOf(dataStore.getReminderTimeSync().first) }
    var reminderMinute by remember { mutableStateOf(dataStore.getReminderTimeSync().second) }
    var themeMode by remember { mutableStateOf(dataStore.getThemeModeSync()) }
    var enhancedReminder by remember { mutableStateOf(dataStore.isEnhancedReminderEnabledSync()) }
    var batteryIgnored by remember {
        mutableStateOf(BatteryOptimizationHelper.isBatteryOptimizationIgnored(context))
    }

    var showClearDataDialog by remember { mutableStateOf(false) }
    var showBatteryDialog by remember { mutableStateOf(false) }
    var showEnhancedReminderDialog by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                batteryIgnored = BatteryOptimizationHelper.isBatteryOptimizationIgnored(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        TopBar(onNavigateBack = onNavigateBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 28.dp)
        ) {
            SectionLabel(stringResource(R.string.settings_section_reminder))
            SettingsSwitchItem(
                icon = R.drawable.ic_lucide_bell,
                title = stringResource(R.string.daily_reminder),
                subtitle = if (reminderEnabled) {
                    stringResource(R.string.reminder_on)
                } else {
                    stringResource(R.string.reminder_off)
                },
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
            AnimatedVisibility(visible = reminderEnabled) {
                Column {
                    Hairline()
                    SettingsItem(
                        icon = R.drawable.ic_lucide_clock,
                        title = stringResource(R.string.reminder_time),
                        subtitle = String.format("%02d:%02d", reminderHour, reminderMinute),
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
                    Hairline()
                    SettingsSwitchItem(
                        icon = R.drawable.ic_lucide_shield,
                        title = stringResource(R.string.enhanced_reminder),
                        subtitle = if (enhancedReminder) {
                            stringResource(R.string.enhanced_reminder_subtitle_on)
                        } else {
                            stringResource(R.string.enhanced_reminder_subtitle_off)
                        },
                        checked = enhancedReminder,
                        onCheckedChange = { enabled ->
                            if (enabled && !enhancedReminder) {
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

            SectionLabel(stringResource(R.string.settings_section_appearance))
            Text(
                text = stringResource(R.string.theme),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 8.dp)
            )
            ThemeModePicker(
                themeMode = themeMode,
                onSelect = { mode ->
                    themeMode = mode
                    onThemeChange(mode)
                    scope.launch {
                        dataStore.setThemeMode(mode)
                    }
                }
            )

            SectionLabel(stringResource(R.string.settings_section_background))
            SettingsItem(
                icon = R.drawable.ic_lucide_battery,
                title = stringResource(R.string.battery_optimization_title),
                subtitle = if (batteryIgnored) {
                    stringResource(R.string.battery_status_ok)
                } else {
                    stringResource(R.string.battery_status_needed)
                },
                onClick = { showBatteryDialog = true }
            )

            SectionLabel(stringResource(R.string.settings_section_data))
            SettingsItem(
                icon = R.drawable.ic_lucide_trash,
                title = stringResource(R.string.clear_data),
                subtitle = stringResource(R.string.clear_data_subtitle),
                isDestructive = true,
                onClick = { showClearDataDialog = true }
            )

            SectionLabel(stringResource(R.string.about))
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = stringResource(R.string.version, "1.0.0"),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = stringResource(R.string.privacy_note),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
            )
        }
    }

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
                    Text(
                        text = stringResource(R.string.confirm),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

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
private fun TopBar(onNavigateBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 20.dp, top = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onNavigateBack) {
            Icon(
                painter = painterResource(R.drawable.ic_lucide_arrow_left),
                contentDescription = stringResource(R.string.back),
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.semantics { heading() }
        )
    }
}

@Composable
private fun SectionLabel(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .padding(top = 28.dp, bottom = 4.dp)
            .semantics { heading() }
    )
}

@Composable
private fun Hairline() {
    HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
}

@Composable
private fun SettingsItem(
    title: String,
    subtitle: String,
    icon: Int,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick, role = Role.Button)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = if (isDestructive) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.size(20.dp)
        )
        Column(modifier = Modifier.padding(start = 16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isDestructive) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onBackground
                }
            )
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun SettingsSwitchItem(
    title: String,
    subtitle: String,
    icon: Int,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(
                value = checked,
                role = Role.Switch,
                onValueChange = onCheckedChange
            )
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Switch(
            checked = checked,
            onCheckedChange = null,
            modifier = Modifier.clearAndSetSemantics {}
        )
    }
}

@Composable
private fun ThemeModePicker(
    themeMode: String,
    onSelect: (String) -> Unit
) {
    val options = listOf(
        CheckInDataStore.THEME_LIGHT to stringResource(R.string.theme_light),
        CheckInDataStore.THEME_DARK to stringResource(R.string.theme_dark),
        CheckInDataStore.THEME_SYSTEM to stringResource(R.string.theme_system)
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp)
    ) {
        options.forEach { (value, label) ->
            val selected = themeMode == value
            Column(
                modifier = Modifier
                    .padding(end = 24.dp)
                    .selectable(
                        selected = selected,
                        role = Role.RadioButton,
                        onClick = { if (!selected) onSelect(value) }
                    )
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
                    color = if (selected) {
                        MaterialTheme.colorScheme.onBackground
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                Spacer(modifier = Modifier.height(6.dp))
                Spacer(
                    modifier = Modifier
                        .height(2.dp)
                        .width(28.dp)
                        .background(
                            if (selected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                androidx.compose.ui.graphics.Color.Transparent
                            }
                        )
                )
            }
        }
    }
}
