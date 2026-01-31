package com.dailycheckin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(24.dp)
    ) {
        // 顶部导航
        TopBar(onNavigateBack = onNavigateBack)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 统计卡片
        StatsRow(
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
        
        Spacer(modifier = Modifier.weight(1f))
        
        // 本月打卡率
        MonthProgress(
            yearMonth = currentMonth,
            checkedInCount = checkedInDates.size,
            isCurrentMonth = isCurrentMonth,
            today = today
        )
    }
}

@Composable
private fun TopBar(onNavigateBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .size(40.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(12.dp)
                )
        ) {
            Icon(
                Icons.Rounded.ArrowBack,
                contentDescription = "返回",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(22.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Text(
            text = "打卡日历",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun StatsRow(
    totalCheckIns: Int,
    consecutiveDays: Int,
    thisMonthCheckIns: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(AppColors.Blue500, AppColors.Blue600)
                )
            )
            .padding(20.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatItem(value = totalCheckIns.toString(), label = "总打卡")
        
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(40.dp)
                .background(Color.White.copy(alpha = 0.3f))
        )
        
        StatItem(value = consecutiveDays.toString(), label = "连续天数")
        
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(40.dp)
                .background(Color.White.copy(alpha = 0.3f))
        )
        
        StatItem(value = thisMonthCheckIns.toString(), label = "本月打卡")
    }
}

@Composable
private fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
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
                .size(36.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    CircleShape
                )
        ) {
            Icon(
                Icons.Rounded.ChevronLeft,
                contentDescription = "上个月",
                tint = AppColors.Blue600
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
                fontWeight = FontWeight.SemiBold
            )
            if (!isCurrentMonth) {
                Text(
                    text = "返回本月",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.Blue600
                )
            }
        }
        
        IconButton(
            onClick = onNextMonth,
            modifier = Modifier
                .size(36.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    CircleShape
                )
        ) {
            Icon(
                Icons.Rounded.ChevronRight,
                contentDescription = "下个月",
                tint = AppColors.Blue600
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 星期标题
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("日", "一", "二", "三", "四", "五", "六").forEach { day ->
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
        verticalArrangement = Arrangement.spacedBy(4.dp)
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
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isCheckedIn -> AppColors.Blue500
                        isToday -> AppColors.Blue500.copy(alpha = 0.1f)
                        else -> Color.Transparent
                    }
                )
                .then(
                    if (isToday && !isCheckedIn) {
                        Modifier.border(1.5.dp, AppColors.Blue500, CircleShape)
                    } else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCheckedIn) {
                Icon(
                    Icons.Rounded.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            } else {
                Text(
                    text = day.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                    color = when {
                        isFuture -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        isToday -> AppColors.Blue600
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
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "本月打卡率",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "$checkedInCount / $passedDays 天",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 进度条
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progressFloat)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(AppColors.Blue500)
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = "$percentage%",
            style = MaterialTheme.typography.labelMedium,
            color = AppColors.Blue600,
            fontWeight = FontWeight.Medium
        )
    }
}
