package com.dailycheckin.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dailycheckin.R
import com.dailycheckin.data.CheckInDataStore
import com.dailycheckin.util.NotificationHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
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
    var showCheckInAnimation by remember { mutableStateOf(false) }
    
    // 呼吸动画
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    
    // 光晕动画
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )
    
    // 打卡成功动画
    val checkInScale by animateFloatAsState(
        targetValue = if (showCheckInAnimation) 1.2f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "checkin_scale"
    )
    
    val today = LocalDate.now()
    val dateFormatter = DateTimeFormatter.ofPattern("MM月dd日 EEEE", Locale.CHINA)
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { 
                        Column {
                            Text(
                                stringResource(R.string.app_name),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                today.format(dateFormatter),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = onNavigateToCalendar) {
                            Icon(
                                imageVector = Icons.Outlined.CalendarMonth,
                                contentDescription = stringResource(R.string.calendar)
                            )
                        }
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = stringResource(R.string.settings)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // 连续打卡天数显示
                AnimatedVisibility(
                    visible = consecutiveDays > 0,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    Card(
                        modifier = Modifier
                            .padding(bottom = 40.dp)
                            .shadow(
                                elevation = 8.dp,
                                shape = RoundedCornerShape(24.dp),
                                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 32.dp, vertical = 20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 火焰图标
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(
                                                Color(0xFFFF9800),
                                                Color(0xFFFF5722)
                                            )
                                        ),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Bolt,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(20.dp))
                            
                            Column {
                                Text(
                                    text = consecutiveDays.toString(),
                                    style = MaterialTheme.typography.displayMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "连续打卡天数",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                
                // 首次打卡提示
                AnimatedVisibility(
                    visible = consecutiveDays == 0 && !isCheckedIn,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(bottom = 32.dp)
                    ) {
                        Text(
                            text = "👋",
                            fontSize = 48.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.first_check_in),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 打卡按钮
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    // 外层光晕效果（未打卡时）
                    if (!isCheckedIn) {
                        Box(
                            modifier = Modifier
                                .size(240.dp)
                                .scale(pulseScale)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary.copy(alpha = glowAlpha),
                                            Color.Transparent
                                        )
                                    ),
                                    CircleShape
                                )
                        )
                    }
                    
                    // 打卡按钮主体
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .scale(if (!isCheckedIn) pulseScale * 0.95f else checkInScale)
                            .shadow(
                                elevation = if (isCheckedIn) 4.dp else 16.dp,
                                shape = CircleShape,
                                spotColor = if (isCheckedIn) 
                                    Color.Gray 
                                else 
                                    MaterialTheme.colorScheme.primary
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
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.tertiary
                                        )
                                    )
                                }
                            )
                            .clickable(
                                enabled = !isCheckedIn,
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {
                                    scope.launch {
                                        showCheckInAnimation = true
                                        dataStore.addCheckIn(LocalDate.now())
                                        delay(100)
                                        isCheckedIn = true
                                        consecutiveDays = dataStore.getConsecutiveDays()
                                        NotificationHelper.scheduleDailyReminder(context)
                                        delay(300)
                                        showCheckInAnimation = false
                                    }
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // 打卡图标
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(
                                        Color.White.copy(alpha = 0.2f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(36.dp),
                                    tint = if (isCheckedIn) {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    } else {
                                        Color.White
                                    }
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Text(
                                text = if (isCheckedIn) {
                                    stringResource(R.string.checked_in_today)
                                } else {
                                    stringResource(R.string.check_in_today)
                                },
                                style = MaterialTheme.typography.titleMedium,
                                color = if (isCheckedIn) {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                } else {
                                    Color.White
                                },
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(48.dp))
                
                // 已打卡后的鼓励文字
                AnimatedVisibility(
                    visible = isCheckedIn,
                    enter = fadeIn() + slideInVertically { it },
                    exit = fadeOut()
                ) {
                    Text(
                        text = getEncouragementText(consecutiveDays),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                // 底部日历入口
                FilledTonalButton(
                    onClick = onNavigateToCalendar,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "查看打卡日历",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

/**
 * 根据连续打卡天数返回鼓励文字
 */
private fun getEncouragementText(days: Int): String {
    return when {
        days >= 365 -> "🏆 一年了！你是坚持的王者！"
        days >= 100 -> "🌟 百日成就达成！太厉害了！"
        days >= 30 -> "🎉 坚持一个月了，继续加油！"
        days >= 7 -> "💪 一周打卡完成，养成好习惯！"
        days >= 3 -> "✨ 连续三天，好的开始！"
        days == 1 -> "🎯 今日打卡成功！明天继续！"
        else -> "👍 做得好！继续保持！"
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
