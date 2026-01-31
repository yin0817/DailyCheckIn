package com.dailycheckin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    val totalCheckIns = remember { dataStore.getCheckInDatesSync().size }
    val consecutiveDays = remember { dataStore.getConsecutiveDays() }
    
    val today = LocalDate.now()
    val isCurrentMonth = currentMonth == YearMonth.now()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // 顶部栏
        TopBar(onNavigateBack = onNavigateBack)
        
        // 统计数据
        Spacer(modifier = Modifier.height(24.dp))
        StatsSection(
            totalCheckIns = totalCheckIns,
            consecutiveDays = consecutiveDays,
            thisMonthCheckIns = checkedInDates.size
        )
        
        // 月份选择
        Spacer(modifier = Modifier.height(32.dp))
        MonthSelector(
            currentMonth = currentMonth,
            isCurrentMonth = isCurrentMonth,
            onPreviousMonth = { currentMonth = currentMonth.minusMonths(1) },
            onNextMonth = { currentMonth = currentMonth.plusMonths(1) },
            onTodayClick = { currentMonth = YearMonth.now() }
        )
        
        // 日历
        Spacer(modifier = Modifier.height(24.dp))
        CalendarGrid(
            yearMonth = currentMonth,
            checkedInDates = checkedInDates,
            today = today
        )
        
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun TopBar(onNavigateBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onNavigateBack) {
            Icon(
                Icons.Rounded.ArrowBackIosNew,
                contentDescription = "返回",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(20.dp)
            )
        }
        
        Text(
            text = "打卡记录",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun StatsSection(
    totalCheckIns: Int,
    consecutiveDays: Int,
    thisMonthCheckIns: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatItem(value = totalCheckIns.toString(), label = "总天数")
        StatItem(value = consecutiveDays.toString(), label = "连续")
        StatItem(value = thisMonthCheckIns.toString(), label = "本月")
    }
}

@Composable
private fun StatItem(value: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
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
    val formatter = DateTimeFormatter.ofPattern("yyyy年M月", Locale.CHINA)
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(
                Icons.Rounded.ChevronLeft,
                contentDescription = "上月",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = if (!isCurrentMonth) {
                Modifier.clickable(onClick = onTodayClick)
            } else Modifier
        ) {
            Text(
                text = currentMonth.format(formatter),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            if (!isCurrentMonth) {
                Text(
                    text = "返回今天",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.Primary
                )
            }
        }
        
        IconButton(onClick = onNextMonth) {
            Icon(
                Icons.Rounded.ChevronRight,
                contentDescription = "下月",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
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
    val weekDays = listOf("日", "一", "二", "三", "四", "五", "六")
    val firstDayOfMonth = yearMonth.atDay(1)
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7
    val totalCells = firstDayOfWeek + daysInMonth
    val rows = (totalCells + 6) / 7
    
    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        // 星期标题
        Row(modifier = Modifier.fillMaxWidth()) {
            weekDays.forEach { day ->
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 日期
        for (row in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0..6) {
                    val cellIndex = row * 7 + col
                    val dayOffset = cellIndex - firstDayOfWeek
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (dayOffset in 0 until daysInMonth) {
                            val date = yearMonth.atDay(dayOffset + 1)
                            val isCheckedIn = checkedInDates.contains(date)
                            val isToday = date == today
                            val isFuture = date.isAfter(today)
                            
                            DayCell(
                                day = dayOffset + 1,
                                isCheckedIn = isCheckedIn,
                                isToday = isToday,
                                isFuture = isFuture
                            )
                        }
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
    isFuture: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(CircleShape)
            .background(
                when {
                    isCheckedIn -> AppColors.Primary
                    isToday -> MaterialTheme.colorScheme.surfaceVariant
                    else -> Color.Transparent
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isToday || isCheckedIn) FontWeight.SemiBold else FontWeight.Normal,
            color = when {
                isCheckedIn -> Color.White
                isFuture -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                isToday -> AppColors.Primary
                else -> MaterialTheme.colorScheme.onSurface
            }
        )
    }
}
