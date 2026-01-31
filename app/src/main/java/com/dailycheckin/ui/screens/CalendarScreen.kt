package com.dailycheckin.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
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
    val today = LocalDate.now()
    val isCurrentMonth = currentMonth == YearMonth.now()
    
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
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
                .padding(16.dp)
        ) {
            // 月份导航卡片
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(20.dp),
                        spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { currentMonth = currentMonth.minusMonths(1) }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ChevronLeft,
                            contentDescription = "Previous Month",
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = currentMonth.format(monthFormatter),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (isCurrentMonth) {
                            Text(
                                text = "本月",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    IconButton(
                        onClick = { currentMonth = currentMonth.plusMonths(1) }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ChevronRight,
                            contentDescription = "Next Month",
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // 日历卡片
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(20.dp),
                        spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // 星期标题
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        val weekDays = listOf(
                            R.string.sun, R.string.mon, R.string.tue, R.string.wed,
                            R.string.thu, R.string.fri, R.string.sat
                        )
                        weekDays.forEachIndexed { index, dayRes ->
                            val isWeekend = index == 0 || index == 6
                            Text(
                                text = context.getString(dayRes),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isWeekend) 
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.7f) 
                                else 
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // 日历网格
                    CalendarGrid(
                        yearMonth = currentMonth,
                        checkedInDates = checkedInDates,
                        today = today
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // 统计信息卡片
            val daysInMonth = currentMonth.lengthOfMonth()
            val checkedInCount = checkedInDates.count { 
                it.year == currentMonth.year && it.monthValue == currentMonth.monthValue 
            }
            val percentage = if (daysInMonth > 0) (checkedInCount * 100 / daysInMonth) else 0
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(20.dp),
                        spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // 打卡天数
                    StatItem(
                        value = checkedInCount.toString(),
                        label = "打卡天数",
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    // 分隔线
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(60.dp)
                            .background(
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                    )
                    
                    // 打卡率
                    StatItem(
                        value = "$percentage%",
                        label = "打卡率",
                        color = when {
                            percentage >= 80 -> Color(0xFF4CAF50)
                            percentage >= 50 -> Color(0xFFFF9800)
                            else -> MaterialTheme.colorScheme.error
                        }
                    )
                    
                    // 分隔线
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(60.dp)
                            .background(
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                    )
                    
                    // 剩余天数
                    val remainingDays = if (isCurrentMonth) {
                        daysInMonth - today.dayOfMonth
                    } else if (currentMonth.isBefore(YearMonth.now())) {
                        0
                    } else {
                        daysInMonth
                    }
                    StatItem(
                        value = remainingDays.toString(),
                        label = "剩余天数",
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 图例说明
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendItem(
                    color = MaterialTheme.colorScheme.primary,
                    text = "已打卡"
                )
                Spacer(modifier = Modifier.width(24.dp))
                LegendItem(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    text = "今天",
                    hasBorder = true
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    value: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LegendItem(
    color: Color,
    text: String,
    hasBorder: Boolean = false
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(color)
                .then(
                    if (hasBorder) Modifier.border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    ) else Modifier
                )
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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
    
    Column {
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
                        val isFuture = date.isAfter(today)
                        val isWeekend = col == 0 || col == 6
                        
                        DayCell(
                            day = dayOffset + 1,
                            isCheckedIn = isCheckedIn,
                            isToday = isToday,
                            isFuture = isFuture,
                            isWeekend = isWeekend,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Box(modifier = Modifier.weight(1f))
                    }
                }
            }
            
            if (row < rows - 1) {
                Spacer(modifier = Modifier.height(6.dp))
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
    isWeekend: Boolean,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isCheckedIn) 1f else 1f,
        animationSpec = spring(),
        label = "scale"
    )
    
    val backgroundColor by animateColorAsState(
        targetValue = when {
            isCheckedIn -> MaterialTheme.colorScheme.primary
            isToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            else -> Color.Transparent
        },
        label = "bg_color"
    )
    
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(3.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(backgroundColor)
            .then(
                if (isToday && !isCheckedIn) {
                    Modifier.border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isCheckedIn) {
            // 打卡日显示勾号
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    isFuture -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    isToday -> MaterialTheme.colorScheme.primary
                    isWeekend -> MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
}

@Composable
private fun stringResource(id: Int): String {
    return LocalContext.current.getString(id)
}
