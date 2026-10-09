package com.dailycheckin.ui.screens

import android.app.TimePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.BatteryAlert
import androidx.compose.material.icons.outlined.BatteryFull
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
                .padding(horizontal = 20.dp)
        ) {
            SettingsSection(title = stringResource(R.string.settings_section_reminder)) {
                SettingsCard {
                    SettingsSwitchItem(
                        title = stringResource(R.string.daily_reminder),
                        subtitle = if (reminderEnabled) {
                            stringResource(R.string.reminder_on)
                        } else {
                            stringResource(R.string.reminder_off)
                        },
                        icon = Icons.Outlined.Notifications,
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
                            SettingsDivider()
                            SettingsItem(
                                title = stringResource(R.string.reminder_time),
                                subtitle = String.format("%02d:%02d", reminderHour, reminderMinute),
                                icon = Icons.Outlined.Schedule,
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
                            SettingsDivider()
                            SettingsSwitchItem(
                                title = stringResource(R.string.enhanced_reminder),
                                subtitle = if (enhancedReminder) {
                                    stringResource(R.string.enhanced_reminder_subtitle_on)
                                } else {
                                    stringResource(R.string.enhanced_reminder_subtitle_off)
                                },
                                icon = Icons.Outlined.Shield,
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
                }
            }

            SettingsSection(title = stringResource(R.string.settings_section_appearance)) {
                SettingsCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.theme),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))
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
                    }
                }
            }

            SettingsSection(title = stringResource(R.string.settings_section_background)) {
                SettingsCard {
                    SettingsItem(
                        title = stringResource(R.string.battery_optimization_title),
                        subtitle = if (batteryIgnored) {
                            stringResource(R.string.battery_status_ok)
                        } else {
                            stringResource(R.string.battery_status_needed)
                        },
                        icon = if (batteryIgnored) {
                            Icons.Outlined.BatteryFull
                        } else {
                            Icons.Outlined.BatteryAlert
                        },
                        onClick = { showBatteryDialog = true }
                    )
                }
            }

            SettingsSection(title = stringResource(R.string.settings_section_data)) {
                SettingsCard {
                    SettingsItem(
                        title = stringResource(R.string.clear_data),
                        subtitle = stringResource(R.string.clear_data_subtitle),
                        icon = Icons.Outlined.Delete,
                        isDestructive = true,
                        showChevron = false,
                        onClick = { showClearDataDialog = true }
                    )
                }
            }

            SettingsSection(title = stringResource(R.string.about)) {
                SettingsCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.version, "1.0.0"),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.privacy_note),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
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
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onNavigateBack) {
            Icon(
                Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = stringResource(R.string.back),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.semantics { heading() }
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .padding(start = 4.dp, top = 20.dp, bottom = 8.dp)
                .semantics { heading() }
        )
        content()
    }
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(content = content)
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 56.dp),
        color = MaterialTheme.colorScheme.outlineVariant
    )
}

@Composable
private fun SettingsItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isDestructive: Boolean = false,
    showChevron: Boolean = true,
    onClick: () -> Unit
) {
    val contentColor = if (isDestructive) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val iconTint = if (isDestructive) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable(onClick = onClick, role = Role.Button)
            .semantics(mergeDescendants = true) {}
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = contentColor
            )
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (showChevron) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SettingsSwitchItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .toggleable(
                value = checked,
                role = Role.Switch,
                onValueChange = onCheckedChange
            )
            .semantics(mergeDescendants = true) {}
            .padding(horizontal = 16.dp, vertical = 8.dp),
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
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
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
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { (value, label) ->
            val selected = themeMode == value
            val shape = RoundedCornerShape(12.dp)
            val borderColor = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outline
            }
            val background = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                Color.Transparent
            }
            val textColor = if (selected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurface
            }
            BoxSelectable(
                label = label,
                selected = selected,
                borderColor = borderColor,
                background = background,
                textColor = textColor,
                shape = shape,
                onClick = { if (!selected) onSelect(value) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun BoxSelectable(
    label: String,
    selected: Boolean,
    borderColor: Color,
    background: Color,
    textColor: Color,
    shape: RoundedCornerShape,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .heightIn(min = 48.dp)
            .clip(shape)
            .border(BorderStroke(1.dp, borderColor), shape)
            .background(background, shape)
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onClick
            )
            .padding(horizontal = 4.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = textColor,
            textAlign = TextAlign.Center
        )
    }
}
