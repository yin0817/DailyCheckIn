package com.dailycheckin.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
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
import kotlin.math.sin

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
    var totalCheckIns by remember { mutableStateOf(dataStore.getCheckInDatesSync().size) }
    var showSuccessAnimation by remember { mutableStateOf(false) }
    
    val today = LocalDate.now()
    val dateFormatter = DateTimeFormatter.ofPattern("MM月dd日", Locale.CHINA)
    val weekFormatter = DateTimeFormatter.ofPattern("EEEE", Locale.CHINA)
    
    // 背景动画
    val infiniteTransition = rememberInfiniteTransition(label = "bg")
    val bgOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bg_offset"
    )
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // 动态渐变背景
        AnimatedGradientBackground(offset = bgOffset)
        
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                // 自定义顶部栏
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                        .statusBarsPadding()
                ) {
                    Column {
                        Text(
                            text = "每日打卡",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "${today.format(dateFormatter)} ${today.format(weekFormatter)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                    }
                    
                    Row(
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        IconButton(
                            onClick = onNavigateToCalendar,
                            modifier = Modifier
                                .size(44.dp)
                                .background(
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                    CircleShape
                                )
                        ) {
                            Icon(
                                Icons.Outlined.CalendarMonth,
                                contentDescription = "日历",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = onNavigateToSettings,
                            modifier = Modifier
                                .size(44.dp)
                                .background(
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                    CircleShape
                                )
                        ) {
                            Icon(
                                Icons.Outlined.Settings,
                                contentDescription = "设置",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                
                // 连续签到卡片
                StreakCard(
                    consecutiveDays = consecutiveDays,
                    isCheckedIn = isCheckedIn
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 打卡按钮
                CheckInButton(
                    isCheckedIn = isCheckedIn,
                    showSuccessAnimation = showSuccessAnimation,
                    onClick = {
                        if (!isCheckedIn) {
                            scope.launch {
                                showSuccessAnimation = true
                                dataStore.addCheckIn(LocalDate.now())
                                delay(200)
                                isCheckedIn = true
                                consecutiveDays = dataStore.getConsecutiveDays()
                                totalCheckIns = dataStore.getCheckInDatesSync().size
                                NotificationHelper.scheduleDailyReminder(context)
                                delay(1500)
                                showSuccessAnimation = false
                            }
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // 统计卡片
                StatsRow(
                    consecutiveDays = consecutiveDays,
                    totalCheckIns = totalCheckIns,
                    onCalendarClick = onNavigateToCalendar
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 激励文字
                MotivationCard(consecutiveDays = consecutiveDays, isCheckedIn = isCheckedIn)
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun AnimatedGradientBackground(offset: Float) {
    val colorScheme = MaterialTheme.colorScheme
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        
        // 主渐变背景
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    colorScheme.primary.copy(alpha = 0.08f),
                    colorScheme.background,
                    colorScheme.tertiary.copy(alpha = 0.05f)
                )
            )
        )
        
        // 动态圆形装饰
        val radius1 = width * 0.4f
        val x1 = width * 0.8f + sin(Math.toRadians(offset.toDouble())).toFloat() * 50
        val y1 = height * 0.15f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    colorScheme.primary.copy(alpha = 0.1f),
                    Color.Transparent
                ),
                center = Offset(x1, y1),
                radius = radius1
            ),
            center = Offset(x1, y1),
            radius = radius1
        )
        
        val radius2 = width * 0.3f
        val x2 = width * 0.2f
        val y2 = height * 0.85f + sin(Math.toRadians(offset.toDouble() + 180)).toFloat() * 30
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    colorScheme.tertiary.copy(alpha = 0.08f),
                    Color.Transparent
                ),
                center = Offset(x2, y2),
                radius = radius2
            ),
            center = Offset(x2, y2),
            radius = radius2
        )
    }
}

@Composable
private fun StreakCard(
    consecutiveDays: Int,
    isCheckedIn: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "streak")
    val fireScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fire_scale"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(28.dp),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
            ),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = if (consecutiveDays > 0) listOf(
                            Color(0xFFFF9800).copy(alpha = 0.1f),
                            Color(0xFFFF5722).copy(alpha = 0.05f),
                            Color.Transparent
                        ) else listOf(
                            Color.Transparent,
                            Color.Transparent
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 火焰图标区域
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .scale(if (consecutiveDays > 0) fireScale else 1f)
                        .background(
                            brush = if (consecutiveDays > 0) Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFFF9800),
                                    Color(0xFFFF5722)
                                )
                            ) else Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    MaterialTheme.colorScheme.surfaceVariant
                                )
                            ),
                            shape = RoundedCornerShape(20.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (consecutiveDays > 0) "🔥" else "💤",
                        fontSize = 32.sp
                    )
                }
                
                Spacer(modifier = Modifier.width(20.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (consecutiveDays > 0) "连续打卡" else "开始打卡吧",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Row(
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = consecutiveDays.toString(),
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (consecutiveDays > 0) 
                                Color(0xFFFF5722) 
                            else 
                                MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "天",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    
                    if (consecutiveDays > 0 && isCheckedIn) {
                        Text(
                            text = "✓ 今日已打卡",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                // 徽章
                if (consecutiveDays >= 7) {
                    Box(
                        modifier = Modifier
                            .background(
                                Color(0xFFFFD700).copy(alpha = 0.2f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = when {
                                consecutiveDays >= 365 -> "👑 年度王者"
                                consecutiveDays >= 100 -> "🏆 百日达人"
                                consecutiveDays >= 30 -> "🌟 月度之星"
                                else -> "💪 周冠军"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFFFF8F00)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CheckInButton(
    isCheckedIn: Boolean,
    showSuccessAnimation: Boolean,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "button")
    
    // 呼吸动画
    val breatheScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathe"
    )
    
    // 光环动画
    val ringScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseOut),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring_scale"
    )
    
    val ringAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseOut),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring_alpha"
    )
    
    // 成功动画
    val successScale by animateFloatAsState(
        targetValue = if (showSuccessAnimation) 1.15f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "success"
    )
    
    val buttonSize = 180.dp
    
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(240.dp)
    ) {
        // 外层光环（未打卡时显示）
        if (!isCheckedIn) {
            Box(
                modifier = Modifier
                    .size(buttonSize)
                    .scale(ringScale)
                    .alpha(ringAlpha)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        CircleShape
                    )
            )
            
            Box(
                modifier = Modifier
                    .size(buttonSize)
                    .scale(breatheScale)
                    .alpha(0.2f)
                    .background(
                        MaterialTheme.colorScheme.primary,
                        CircleShape
                    )
            )
        }
        
        // 主按钮
        Box(
            modifier = Modifier
                .size(buttonSize)
                .scale(if (!isCheckedIn) breatheScale * successScale else successScale)
                .shadow(
                    elevation = if (isCheckedIn) 8.dp else 24.dp,
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
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
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
                // 图标
                AnimatedContent(
                    targetState = isCheckedIn,
                    transitionSpec = {
                        scaleIn(animationSpec = tween(300)) + fadeIn() togetherWith
                                scaleOut(animationSpec = tween(300)) + fadeOut()
                    },
                    label = "icon"
                ) { checked ->
                    if (checked) {
                        Text(
                            text = "✓",
                            fontSize = 56.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = "👆",
                            fontSize = 48.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = if (isCheckedIn) "已打卡" else "打卡",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isCheckedIn)
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else
                        Color.White
                )
            }
        }
        
        // 成功粒子效果
        if (showSuccessAnimation) {
            SuccessParticles()
        }
    }
}

@Composable
private fun SuccessParticles() {
    val particles = remember {
        List(12) { index ->
            val angle = (index * 30).toDouble()
            Pair(
                kotlin.math.cos(Math.toRadians(angle)).toFloat(),
                kotlin.math.sin(Math.toRadians(angle)).toFloat()
            )
        }
    }
    
    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(800, easing = EaseOut),
        label = "particles"
    )
    
    particles.forEachIndexed { index, (dx, dy) ->
        val distance = 100.dp * animatedProgress
        val alpha = 1f - animatedProgress
        
        Box(
            modifier = Modifier
                .offset(
                    x = distance * dx,
                    y = distance * dy
                )
                .size(12.dp)
                .alpha(alpha)
                .background(
                    when (index % 3) {
                        0 -> Color(0xFFFFD700)
                        1 -> Color(0xFFFF6B6B)
                        else -> Color(0xFF4ECDC4)
                    },
                    CircleShape
                )
        )
    }
}

@Composable
private fun StatsRow(
    consecutiveDays: Int,
    totalCheckIns: Int,
    onCalendarClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 累计打卡
        StatCard(
            modifier = Modifier.weight(1f),
            icon = "📅",
            value = totalCheckIns.toString(),
            label = "累计打卡",
            onClick = onCalendarClick
        )
        
        // 本周目标
        val weekProgress = minOf(consecutiveDays % 7 + if (consecutiveDays > 0) 0 else 0, 7)
        StatCard(
            modifier = Modifier.weight(1f),
            icon = "🎯",
            value = "$weekProgress/7",
            label = "本周进度",
            onClick = onCalendarClick
        )
        
        // 最长连续
        StatCard(
            modifier = Modifier.weight(1f),
            icon = "🏅",
            value = consecutiveDays.toString(),
            label = "最长连续",
            onClick = onCalendarClick
        )
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    icon: String,
    value: String,
    label: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = icon, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MotivationCard(
    consecutiveDays: Int,
    isCheckedIn: Boolean
) {
    val (emoji, text) = remember(consecutiveDays, isCheckedIn) {
        when {
            !isCheckedIn -> Pair("💡", "每天进步一点点，坚持就是胜利！")
            consecutiveDays >= 100 -> Pair("🎊", "100天！你已经是坚持的大师了！")
            consecutiveDays >= 30 -> Pair("🌟", "一个月的坚持，你太棒了！继续加油！")
            consecutiveDays >= 7 -> Pair("🎉", "一周达成！好习惯正在养成中～")
            consecutiveDays >= 3 -> Pair("✨", "连续三天，你已经在路上了！")
            else -> Pair("🎯", "今日打卡完成！明天继续保持哦～")
        }
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = emoji,
                fontSize = 32.sp
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
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
