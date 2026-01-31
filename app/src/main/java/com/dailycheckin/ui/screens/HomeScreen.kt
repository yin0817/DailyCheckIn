package com.dailycheckin.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import com.dailycheckin.ui.theme.AppColors
import com.dailycheckin.util.NotificationHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

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
    val dateFormatter = DateTimeFormatter.ofPattern("MM/dd", Locale.CHINA)
    val weekFormatter = DateTimeFormatter.ofPattern("EEEE", Locale.CHINA)
    
    // 渐变背景动画
    val infiniteTransition = rememberInfiniteTransition(label = "bg")
    val animatedOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradient"
    )
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // 渐变背景
        GradientBackground(animatedOffset)
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
        ) {
            // 顶部栏
            TopSection(
                date = today.format(dateFormatter),
                weekDay = today.format(weekFormatter),
                onCalendarClick = onNavigateToCalendar,
                onSettingsClick = onNavigateToSettings
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // 主要内容区
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 连续打卡展示
                StreakDisplay(
                    consecutiveDays = consecutiveDays,
                    isCheckedIn = isCheckedIn
                )
                
                Spacer(modifier = Modifier.height(40.dp))
                
                // 打卡按钮
                CheckInButton(
                    isCheckedIn = isCheckedIn,
                    showSuccessAnimation = showSuccessAnimation,
                    onClick = {
                        if (!isCheckedIn) {
                            scope.launch {
                                showSuccessAnimation = true
                                dataStore.addCheckIn(LocalDate.now())
                                delay(300)
                                isCheckedIn = true
                                consecutiveDays = dataStore.getConsecutiveDays()
                                totalCheckIns = dataStore.getCheckInDatesSync().size
                                NotificationHelper.scheduleDailyReminder(context)
                                delay(1200)
                                showSuccessAnimation = false
                            }
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(48.dp))
                
                // 统计卡片
                StatsCards(
                    consecutiveDays = consecutiveDays,
                    totalCheckIns = totalCheckIns,
                    onCalendarClick = onNavigateToCalendar
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 激励卡片
                MotivationBanner(
                    consecutiveDays = consecutiveDays,
                    isCheckedIn = isCheckedIn
                )
                
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun GradientBackground(animatedOffset: Float) {
    val isDark = isSystemInDarkTheme()
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        
        // 主背景渐变
        if (isDark) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F0F1A),
                        Color(0xFF1A1A2E),
                        Color(0xFF16213E)
                    )
                )
            )
        } else {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFAFAFC),
                        Color(0xFFF5F3FF),
                        Color(0xFFEEF2FF)
                    )
                )
            )
        }
        
        // 装饰性渐变圆
        val circleX = width * (0.8f + animatedOffset * 0.1f)
        val circleY = height * 0.15f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    AppColors.Purple500.copy(alpha = if (isDark) 0.15f else 0.1f),
                    Color.Transparent
                ),
                center = Offset(circleX, circleY),
                radius = width * 0.5f
            ),
            center = Offset(circleX, circleY),
            radius = width * 0.5f
        )
        
        val circle2X = width * 0.2f
        val circle2Y = height * (0.7f + animatedOffset * 0.1f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    AppColors.Cyan400.copy(alpha = if (isDark) 0.1f else 0.08f),
                    Color.Transparent
                ),
                center = Offset(circle2X, circle2Y),
                radius = width * 0.4f
            ),
            center = Offset(circle2X, circle2Y),
            radius = width * 0.4f
        )
    }
}

@Composable
private fun TopSection(
    date: String,
    weekDay: String,
    onCalendarClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "每日打卡",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = date,
                    style = MaterialTheme.typography.titleMedium,
                    color = AppColors.Purple500,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = " · $weekDay",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        }
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(
                onClick = onCalendarClick,
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(14.dp)
                    )
            ) {
                Icon(
                    Icons.Outlined.CalendarMonth,
                    contentDescription = "日历",
                    tint = AppColors.Purple500
                )
            }
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(14.dp)
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

@Composable
private fun StreakDisplay(
    consecutiveDays: Int,
    isCheckedIn: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "streak")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 火焰或数字展示
        Box(
            contentAlignment = Alignment.Center
        ) {
            // 光晕效果
            if (consecutiveDays > 0) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .alpha(glowAlpha * 0.3f)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    AppColors.Amber500,
                                    Color.Transparent
                                )
                            ),
                            CircleShape
                        )
                )
            }
            
            Text(
                text = if (consecutiveDays > 0) "🔥" else "✨",
                fontSize = 64.sp
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 连续天数
        Row(
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = consecutiveDays.toString(),
                fontSize = 56.sp,
                fontWeight = FontWeight.Bold,
                color = if (consecutiveDays > 0) AppColors.Amber500 else MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "天",
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 10.dp)
            )
        }
        
        Text(
            text = if (consecutiveDays > 0) "连续打卡" else "开始你的第一天",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
        
        // 今日状态标签
        if (isCheckedIn) {
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = AppColors.Emerald500.copy(alpha = 0.15f)
            ) {
                Text(
                    text = "✓ 今日已完成",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = AppColors.Emerald500,
                    fontWeight = FontWeight.SemiBold
                )
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
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    // 旋转光环
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    // 成功动画
    val successScale by animateFloatAsState(
        targetValue = if (showSuccessAnimation) 1.2f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "success"
    )
    
    val buttonSize = 160.dp
    
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(200.dp)
    ) {
        // 外层旋转光环（仅未打卡时显示）
        if (!isCheckedIn) {
            Canvas(
                modifier = Modifier
                    .size(buttonSize + 40.dp)
                    .rotate(rotation)
            ) {
                val sweepAngle = 90f
                for (i in 0..3) {
                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                AppColors.Purple500.copy(alpha = 0f),
                                AppColors.Purple500.copy(alpha = 0.5f),
                                AppColors.Cyan400.copy(alpha = 0.5f),
                                AppColors.Cyan400.copy(alpha = 0f)
                            )
                        ),
                        startAngle = i * 90f,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4.dp.toPx())
                    )
                }
            }
        }
        
        // 主按钮
        Box(
            modifier = Modifier
                .size(buttonSize)
                .scale(if (!isCheckedIn) scale * successScale else successScale)
                .shadow(
                    elevation = if (isCheckedIn) 8.dp else 20.dp,
                    shape = CircleShape,
                    spotColor = if (isCheckedIn) Color.Gray else AppColors.Purple500
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
                                AppColors.Purple500,
                                AppColors.Purple600,
                                Color(0xFF7C3AED)
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
                AnimatedContent(
                    targetState = isCheckedIn,
                    transitionSpec = {
                        scaleIn(animationSpec = tween(400)) + fadeIn() togetherWith
                                scaleOut(animationSpec = tween(400)) + fadeOut()
                    },
                    label = "icon"
                ) { checked ->
                    Text(
                        text = if (checked) "✓" else "👆",
                        fontSize = if (checked) 48.sp else 40.sp,
                        color = if (checked) MaterialTheme.colorScheme.onSurfaceVariant else Color.White
                    )
                }
                
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
        
        // 成功粒子效果
        if (showSuccessAnimation) {
            ParticleEffect()
        }
    }
}

@Composable
private fun ParticleEffect() {
    val particles = remember {
        List(8) { index ->
            val angle = index * 45.0
            Triple(
                cos(Math.toRadians(angle)).toFloat(),
                sin(Math.toRadians(angle)).toFloat(),
                listOf(AppColors.Purple500, AppColors.Cyan400, AppColors.Pink500, AppColors.Amber500)[index % 4]
            )
        }
    }
    
    val progress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(600, easing = EaseOut),
        label = "particles"
    )
    
    particles.forEach { (dx, dy, color) ->
        val distance = 80.dp * progress
        val alpha = 1f - progress
        
        Box(
            modifier = Modifier
                .offset(x = distance * dx, y = distance * dy)
                .size(10.dp)
                .alpha(alpha)
                .background(color, CircleShape)
        )
    }
}

@Composable
private fun StatsCards(
    consecutiveDays: Int,
    totalCheckIns: Int,
    onCalendarClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            icon = "📊",
            value = totalCheckIns.toString(),
            label = "累计打卡",
            gradientColors = listOf(AppColors.Purple500, AppColors.Purple400),
            onClick = onCalendarClick
        )
        
        StatCard(
            modifier = Modifier.weight(1f),
            icon = "🎯",
            value = "${minOf(consecutiveDays, 7)}/7",
            label = "本周目标",
            gradientColors = listOf(AppColors.Cyan400, Color(0xFF06B6D4)),
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
    gradientColors: List<Color>,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 图标容器
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        Brush.linearGradient(gradientColors),
                        RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = icon, fontSize = 20.sp)
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MotivationBanner(
    consecutiveDays: Int,
    isCheckedIn: Boolean
) {
    val (emoji, title, subtitle) = remember(consecutiveDays, isCheckedIn) {
        when {
            !isCheckedIn -> Triple("💡", "坚持每一天", "点击上方按钮完成今日打卡")
            consecutiveDays >= 100 -> Triple("👑", "百日达成！", "你已经是坚持的王者")
            consecutiveDays >= 30 -> Triple("🏆", "月度冠军！", "一个月的坚持，为你骄傲")
            consecutiveDays >= 7 -> Triple("⭐", "一周达成！", "好习惯正在养成中")
            consecutiveDays >= 3 -> Triple("🌱", "继续加油！", "三天的坚持，很棒的开始")
            else -> Triple("🎉", "打卡成功！", "明天继续保持哦")
        }
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            AppColors.Purple500.copy(alpha = 0.1f),
                            AppColors.Cyan400.copy(alpha = 0.1f)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = emoji,
                    fontSize = 36.sp
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
private fun stringResource(id: Int): String {
    return LocalContext.current.getString(id)
}
