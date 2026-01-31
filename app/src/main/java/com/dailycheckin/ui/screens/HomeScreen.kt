package com.dailycheckin.ui.screens

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
import com.dailycheckin.service.ReminderService
import com.dailycheckin.ui.theme.AppColors
import com.dailycheckin.util.NotificationHelper
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
    val dateFormatter = DateTimeFormatter.ofPattern("M月d日 EEEE", Locale.CHINA)
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 顶部操作栏
            TopBar(
                onCalendarClick = onNavigateToCalendar,
                onSettingsClick = onNavigateToSettings
            )
            
            // 日期
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = today.format(dateFormatter),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // 连续天数
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = consecutiveDays.toString(),
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = if (consecutiveDays > 0) "连续打卡" else "开始打卡",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
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
                            // 立即更新通知栏
                            ReminderService.refresh(context)
                        }
                    }
                }
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // 底部统计
            BottomStats(
                totalCheckIns = totalCheckIns,
                onCalendarClick = onNavigateToCalendar
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun TopBar(
    onCalendarClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            onClick = onCalendarClick,
            modifier = Modifier.size(44.dp)
        ) {
            Icon(
                Icons.Outlined.CalendarMonth,
                contentDescription = "日历",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
        
        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier.size(44.dp)
        ) {
            Icon(
                Icons.Outlined.Settings,
                contentDescription = "设置",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun CheckInButton(
    isCheckedIn: Boolean,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (!isCheckedIn) 1.02f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    val buttonGradient = if (isCheckedIn) {
        Brush.linearGradient(
            colors = listOf(
                AppColors.Success,
                AppColors.Success
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                AppColors.Primary,
                AppColors.PrimaryLight
            )
        )
    }
    
    Box(
        modifier = Modifier
            .size(160.dp)
            .scale(scale)
            .shadow(
                elevation = if (isCheckedIn) 8.dp else 16.dp,
                shape = CircleShape,
                spotColor = if (isCheckedIn) AppColors.Success else AppColors.Primary
            )
            .clip(CircleShape)
            .background(buttonGradient)
            .clickable(
                enabled = !isCheckedIn,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isCheckedIn) {
            Icon(
                Icons.Rounded.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(64.dp)
            )
        } else {
            Text(
                text = "打卡",
                fontSize = 28.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun BottomStats(
    totalCheckIns: Int,
    onCalendarClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .padding(horizontal = 32.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onCalendarClick)
            .padding(horizontal = 32.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "累计打卡",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$totalCheckIns",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = AppColors.Primary
        )
        Text(
            text = " 天",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
