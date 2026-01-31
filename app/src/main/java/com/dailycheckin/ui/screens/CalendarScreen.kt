package com.dailycheckin.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dailycheckin.R
import com.dailycheckin.data.CheckInDataStore
import com.dailycheckin.ui.theme.AppColors
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
fun CalendarScreen(
    dataStore: CheckInDataStore,
    onNavigateBack: () -> Unit
) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    val checkedInDates = remember(currentMonth) {
        dataStore.getCheckInDatesForMonth(currentMonth.year, currentMonth.monthValue)
    }
    val allCheckedInDates = remember { dataStore.getCheckInDatesSync() }
    val consecutiveDays = remember { dataStore.getConsecutiveDays() }
    
    val today = LocalDate.now()
    val isCurrentMonth = currentMonth == YearMonth.now()
    
    val isDark = isSystemInDarkTheme()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 背景装饰
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        AppColors.Purple500.copy(alpha = if (isDark) 0.1f else 0.06f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.9f, size.height * 0.1f),
                    radius = size.width * 0.5f
                ),
                center = Offset(size.width * 0.9f, size.height * 0.1f),
                radius = size.width * 0.5f
            )
        }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // 顶部导航
            TopBar(onNavigateBack = onNavigateBack)
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                
                // 统计卡片
                StatsOverview(
                    totalCheckIns = allCheckedInDates.size,
                    consecutiveDays = consecutiveDays,
                    thisMonthCheckIns = checkedInDates.size
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 月份选择器
                MonthSelector(
                    currentMonth = currentMonth,
                    isCurrentMonth = isCurrentMonth,
                    onPreviousMonth = { currentMonth = currentMonth.minusMonths(1) },
                    onNextMonth = { currentMonth = currentMonth.plusMonths(1) },
                    onTodayClick = { currentMonth = YearMonth.now() }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 日历
                CalendarView(
                    yearMonth = currentMonth,
                    checkedInDates = checkedInDates,
                    today = today
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 本月进度
                MonthProgress(
                    yearMonth = currentMonth,
                    checkedInCount = checkedInDates.size,
                    isCurrentMonth = isCurrentMonth,
                    today = today
                )
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun TopBar(onNavigateBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .size(44.dp)
                .background(
                    MaterialTheme.colorScheme.surface,
                    RoundedCornerShape(14.dp)
                )
        ) {
            Icon(
                Icons.Rounded.ArrowBack,
                contentDescription = "返回",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        Text(
            text = "打卡日历",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        Box(modifier = Modifier.size(44.dp))
    }
}

@Composable
private fun StatsOverview(
    totalCheckIns: Int,
    consecutiveDays: Int,
    thisMonthCheckIns: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = AppColors.Purple500.copy(alpha = 0.2f)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            AppColors.Purple500,
                            AppColors.Purple600,
                            Color(0xFF7C3AED)
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OverviewItem(value = totalCheckIns.toString(), label = "总打卡", emoji = "📊")
                
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(50.dp)
                        .background(Color.White.copy(alpha = 0.2f))
                )
                
                OverviewItem(value = consecutiveDays.toString(), label = "连续天数", emoji = "🔥")
                
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(50.dp)
                        .background(Color.White.copy(alpha = 0.2f))
                )
                
                OverviewItem(value = thisMonthCheckIns.toString(), label = "本月打卡", emoji = "📅")
            }
        }
    }
}

@Composable
private fun OverviewItem(value: String, label: String, emoji: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = emoji, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun MonthSelector(
    currentMonth: YearMonth,
    isCurrentMonth: Boolean,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onTodayClick: () -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("yyyy年MM月", Locale.CHINA)
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onPreviousMonth,
            modifier = Modifier
                .size(40.dp)
                .background(
                    MaterialTheme.colorScheme.surface,
                    RoundedCornerShape(12.dp)
                )
        ) {
            Icon(
                Icons.Rounded.ChevronLeft,
                contentDescription = "上个月",
                tint = AppColors.Purple500
            )
        }
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable(
                enabled = !isCurrentMonth,
                onClick = onTodayClick
            )
        ) {
            Text(
                text = currentMonth.format(formatter),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            if (!isCurrentMonth) {
                Text(
                    text = "点击回到本月",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.Purple500
                )
            }
        }
        
        IconButton(
            onClick = onNextMonth,
            modifier = Modifier
                .size(40.dp)
                .background(
                    MaterialTheme.colorScheme.surface,
                    RoundedCornerShape(12.dp)
                )
        ) {
            Icon(
                Icons.Rounded.ChevronRight,
                contentDescription = "下个月",
                tint = AppColors.Purple500
            )
        }
    }
}

@Composable
private fun CalendarView(
    yearMonth: YearMonth,
    checkedInDates: Set<LocalDate>,
    today: LocalDate
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 星期标题
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("日", "一", "二", "三", "四", "五", "六").forEachIndexed { index, day ->
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (index == 0 || index == 6)
                                AppColors.Purple500
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 日期网格
            CalendarGrid(
                yearMonth = yearMonth,
                checkedInDates = checkedInDates,
                today = today
            )
        }
    }
}

@Composable
private fun CalendarGrid(
    yearMonth: YearMonth,
    checkedInDates: Set<LocalDate>,
    today: LocalDate
) {
    val firstDayOfMonth = yearMonth.atDay(1)
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7
    val totalCells = firstDayOfWeek + daysInMonth
    val rows = (totalCells + 6) / 7
    
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        for (row in 0 until rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (col in 0..6) {
                    val cellIndex = row * 7 + col
                    val dayOffset = cellIndex - firstDayOfWeek
                    
                    if (dayOffset in 0 until daysInMonth) {
                        val date = yearMonth.atDay(dayOffset + 1)
                        DayCell(
                            day = dayOffset + 1,
                            isCheckedIn = checkedInDates.contains(date),
                            isToday = date == today,
                            isFuture = date.isAfter(today),
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Box(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    day: Int,
    isCheckedIn: Boolean,
    isToday: Boolean,
    isFuture: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize(0.9f)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    when {
                        isCheckedIn -> Brush.linearGradient(
                            colors = listOf(AppColors.Purple500, AppColors.Purple400)
                        )
                        isToday -> Brush.linearGradient(
                            colors = listOf(
                                AppColors.Purple500.copy(alpha = 0.1f),
                                AppColors.Purple500.copy(alpha = 0.1f)
                            )
                        )
                        else -> Brush.linearGradient(
                            colors = listOf(Color.Transparent, Color.Transparent)
                        )
                    }
                )
                .then(
                    if (isToday && !isCheckedIn) {
                        Modifier.border(
                            width = 2.dp,
                            color = AppColors.Purple500,
                            shape = RoundedCornerShape(10.dp)
                        )
                    } else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCheckedIn) {
                Text(
                    text = "✓",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            } else {
                Text(
                    text = day.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                    color = when {
                        isFuture -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        isToday -> AppColors.Purple500
                        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    }
                )
            }
        }
    }
}

@Composable
private fun MonthProgress(
    yearMonth: YearMonth,
    checkedInCount: Int,
    isCurrentMonth: Boolean,
    today: LocalDate
) {
    val daysInMonth = yearMonth.lengthOfMonth()
    val passedDays = if (isCurrentMonth) today.dayOfMonth else daysInMonth
    val percentage = if (passedDays > 0) (checkedInCount * 100 / passedDays) else 0
    val progressFloat = percentage / 100f
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "本月打卡率",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$percentage%",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        percentage >= 80 -> AppColors.Emerald500
                        percentage >= 50 -> AppColors.Amber500
                        else -> Color(0xFFEF4444)
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 进度条
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progressFloat)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = when {
                                    percentage >= 80 -> listOf(AppColors.Emerald500, Color(0xFF34D399))
                                    percentage >= 50 -> listOf(AppColors.Amber500, Color(0xFFFBBF24))
                                    else -> listOf(Color(0xFFEF4444), Color(0xFFF87171))
                                }
                            )
                        )
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ProgressStat(
                    label = "已打卡",
                    value = "$checkedInCount",
                    color = AppColors.Purple500
                )
                ProgressStat(
                    label = if (isCurrentMonth) "已过去" else "总天数",
                    value = "$passedDays",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                ProgressStat(
                    label = "未打卡",
                    value = "${passedDays - checkedInCount}",
                    color = Color(0xFFEF4444).copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun ProgressStat(
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
