package com.dailycheckin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dailycheckin.R
import com.dailycheckin.data.CheckInDataStore
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    dataStore: CheckInDataStore,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    val checkedInDates = remember(currentMonth) {
        dataStore.getCheckInDatesForMonth(currentMonth.year, currentMonth.monthValue)
    }
    
    val monthFormatter = DateTimeFormatter.ofPattern("yyyy年MM月", Locale.CHINA)
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.calendar_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // 月份导航
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { currentMonth = currentMonth.minusMonths(1) }
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowLeft,
                        contentDescription = "Previous Month",
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                Text(
                    text = currentMonth.format(monthFormatter),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                IconButton(
                    onClick = { currentMonth = currentMonth.plusMonths(1) }
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "Next Month",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 星期标题
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val weekDays = listOf(
                    R.string.sun, R.string.mon, R.string.tue, R.string.wed,
                    R.string.thu, R.string.fri, R.string.sat
                )
                weekDays.forEach { dayRes ->
                    Text(
                        text = context.getString(dayRes),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Divider()
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 日历网格
            CalendarGrid(
                yearMonth = currentMonth,
                checkedInDates = checkedInDates,
                today = LocalDate.now()
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 统计信息
            val daysInMonth = currentMonth.lengthOfMonth()
            val checkedInCount = checkedInDates.count { 
                it.year == currentMonth.year && it.monthValue == currentMonth.monthValue 
            }
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = checkedInCount.toString(),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "本月打卡",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    
                    Divider(
                        modifier = Modifier
                            .height(48.dp)
                            .width(1.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f)
                    )
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$checkedInCount/$daysInMonth",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "打卡率",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
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
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7 // 0 = Sunday, 1 = Monday, ...
    
    Column {
        // 空白填充（月初之前的日期）
        var currentDay = 1
        
        // 计算需要多少行
        val totalCells = firstDayOfWeek + daysInMonth
        val rows = (totalCells + 6) / 7
        
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
                        val isCheckedIn = checkedInDates.contains(date)
                        val isToday = date == today
                        
                        DayCell(
                            day = dayOffset + 1,
                            isCheckedIn = isCheckedIn,
                            isToday = isToday,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Box(modifier = Modifier.weight(1f))
                    }
                }
            }
            
            if (row < rows - 1) {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun DayCell(
    day: Int,
    isCheckedIn: Boolean,
    isToday: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(4.dp)
            .clip(CircleShape)
            .background(
                when {
                    isCheckedIn -> MaterialTheme.colorScheme.primary
                    isToday -> MaterialTheme.colorScheme.primaryContainer
                    else -> Color.Transparent
                }
            )
            .border(
                width = if (isToday && !isCheckedIn) 2.dp else 0.dp,
                color = if (isToday && !isCheckedIn) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.toString(),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
            color = when {
                isCheckedIn -> MaterialTheme.colorScheme.onPrimary
                isToday -> MaterialTheme.colorScheme.onPrimaryContainer
                else -> MaterialTheme.colorScheme.onSurface
            }
        )
    }
}

@Composable
private fun stringResource(id: Int): String {
    return LocalContext.current.getString(id)
}
