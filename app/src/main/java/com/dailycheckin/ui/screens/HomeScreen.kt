package com.dailycheckin.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dailycheckin.data.CheckInDataStore
import com.dailycheckin.ui.theme.AppColors
import com.dailycheckin.util.NotificationHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HomeScreen(
    dataStore: CheckInDataStore,
    onNavigateToCalendar: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var isCheckedIn by remember { mutableStateOf(dataStore.isCheckedInToday()) }
    var consecutiveDays by remember { mutableStateOf(dataStore.getConsecutiveDays()) }
    var totalCheckIns by remember { mutableStateOf(dataStore.getCheckInDatesSync().size) }
    
    val today = LocalDate.now()
    val dateFormatter = DateTimeFormatter.ofPattern("MM月dd日", Locale.CHINA)
    val weekFormatter = DateTimeFormatter.ofPattern("EEEE", Locale.CHINA)
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 顶部栏
            TopBar(
                date = today.format(dateFormatter),
                weekDay = today.format(weekFormatter),
                onCalendarClick = onNavigateToCalendar,
                onSettingsClick = onNavigateToSettings
            )
            
            Spacer(modifier = Modifier.weight(0.8f))
            
            // 连续天数显示
            StreakSection(
                consecutiveDays = consecutiveDays,
                isCheckedIn = isCheckedIn
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // 打卡按钮
            CheckInButton(
                isCheckedIn = isCheckedIn,
                onClick = {
                    if (!isCheckedIn) {
                        scope.launch {
                            dataStore.addCheckIn(LocalDate.now())
                            isCheckedIn = true
                            consecutiveDays = dataStore.getConsecutiveDays()
                            totalCheckIns = dataStore.getCheckInDatesSync().size
                            NotificationHelper.scheduleDailyReminder(context)
                        }
                    }
                }
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // 底部统计
            StatsRow(
                totalCheckIns = totalCheckIns,
                consecutiveDays = consecutiveDays,
                onCalendarClick = onNavigateToCalendar
            )
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TopBar(
    date: String,
    weekDay: String,
    onCalendarClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "每日打卡",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "$date $weekDay",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(
                onClick = onCalendarClick,
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(12.dp)
                    )
            ) {
                Icon(
                    Icons.Outlined.CalendarMonth,
                    contentDescription = "日历",
                    tint = AppColors.Blue600,
                    modifier = Modifier.size(22.dp)
                )
            }
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(12.dp)
                    )
            ) {
                Icon(
                    Icons.Outlined.Settings,
                    contentDescription = "设置",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun StreakSection(
    consecutiveDays: Int,
    isCheckedIn: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 连续天数图标
        if (consecutiveDays > 0) {
            Icon(
                Icons.Rounded.LocalFireDepartment,
                contentDescription = null,
                tint = AppColors.Blue500,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        // 连续天数
        Row(
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = consecutiveDays.toString(),
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.Blue600
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "天",
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }
        
        Text(
            text = if (consecutiveDays > 0) "连续打卡" else "开始你的第一天",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
        
        // 今日完成标签
        if (isCheckedIn) {
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = AppColors.Green500.copy(alpha = 0.1f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Rounded.Check,
                        contentDescription = null,
                        tint = AppColors.Green500,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "今日已完成",
                        style = MaterialTheme.typography.labelMedium,
                        color = AppColors.Green500,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun CheckInButton(
    isCheckedIn: Boolean,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "button")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (!isCheckedIn) 1.03f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    val buttonSize = 140.dp
    
    Box(
        modifier = Modifier
            .size(buttonSize)
            .scale(scale)
            .shadow(
                elevation = if (isCheckedIn) 4.dp else 12.dp,
                shape = CircleShape,
                spotColor = if (isCheckedIn) Color.Gray else AppColors.Blue500
            )
            .clip(CircleShape)
            .background(
                if (isCheckedIn) {
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surfaceVariant,
                            MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                } else {
                    Brush.linearGradient(
                        colors = listOf(
                            AppColors.Blue500,
                            AppColors.Blue600
                        )
                    )
                }
            )
            .clickable(
                enabled = !isCheckedIn,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                if (isCheckedIn) Icons.Rounded.Check else Icons.Rounded.TouchApp,
                contentDescription = null,
                tint = if (isCheckedIn) MaterialTheme.colorScheme.onSurfaceVariant else Color.White,
                modifier = Modifier.size(40.dp)
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = if (isCheckedIn) "已打卡" else "打卡",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isCheckedIn)
                    MaterialTheme.colorScheme.onSurfaceVariant
                else
                    Color.White
            )
        }
    }
}

@Composable
private fun StatsRow(
    totalCheckIns: Int,
    consecutiveDays: Int,
    onCalendarClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onCalendarClick)
            .padding(20.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatItem(
            value = totalCheckIns.toString(),
            label = "累计打卡"
        )
        
        Divider(
            modifier = Modifier
                .height(40.dp)
                .width(1.dp),
            color = MaterialTheme.colorScheme.outline
        )
        
        StatItem(
            value = consecutiveDays.toString(),
            label = "连续天数"
        )
        
        Divider(
            modifier = Modifier
                .height(40.dp)
                .width(1.dp),
            color = MaterialTheme.colorScheme.outline
        )
        
        StatItem(
            value = "${minOf(consecutiveDays, 7)}/7",
            label = "本周进度"
        )
    }
}

@Composable
private fun StatItem(
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = AppColors.Blue600
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
