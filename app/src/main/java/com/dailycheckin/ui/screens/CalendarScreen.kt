package com.dailycheckin.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dailycheckin.R
import com.dailycheckin.data.CheckInDataStore
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun CalendarScreen(
    dataStore: CheckInDataStore,
    onNavigateBack: () -> Unit
) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    val checkInDates by dataStore.checkInDates.collectAsState(
        initial = dataStore.getCheckInDatesSync()
    )
    val parsedDates = remember(checkInDates) {
        checkInDates.mapNotNull { raw ->
            runCatching { LocalDate.parse(raw) }.getOrNull()
        }.toSet()
    }
    val checkedInDates = remember(parsedDates, currentMonth) {
        parsedDates.filter { date ->
            date.year == currentMonth.year && date.monthValue == currentMonth.monthValue
        }.toSet()
    }
    val consecutiveDays = remember(checkInDates) { dataStore.getConsecutiveDays() }
    val today = LocalDate.now()
    val isCurrentMonth = currentMonth == YearMonth.now()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        TopBar(onNavigateBack = onNavigateBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            StatsSection(
                totalCheckIns = parsedDates.size,
                consecutiveDays = consecutiveDays,
                thisMonthCheckIns = checkedInDates.size,
                isCurrentMonth = isCurrentMonth
            )

            Spacer(modifier = Modifier.height(16.dp))
            CalendarCard(
                currentMonth = currentMonth,
                isCurrentMonth = isCurrentMonth,
                checkedInDates = checkedInDates,
                today = today,
                onPreviousMonth = { currentMonth = currentMonth.minusMonths(1) },
                onNextMonth = { currentMonth = currentMonth.plusMonths(1) },
                onTodayClick = { currentMonth = YearMonth.now() }
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TopBar(onNavigateBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onNavigateBack) {
            Icon(
                Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = stringResource(R.string.back),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        Text(
            text = stringResource(R.string.calendar_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.semantics { heading() }
        )
    }
}

@Composable
private fun StatsSection(
    totalCheckIns: Int,
    consecutiveDays: Int,
    thisMonthCheckIns: Int,
    isCurrentMonth: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatItem(
            value = totalCheckIns.toString(),
            label = stringResource(R.string.stat_total),
            modifier = Modifier.weight(1f)
        )
        StatItem(
            value = consecutiveDays.toString(),
            label = stringResource(R.string.stat_streak),
            modifier = Modifier.weight(1f)
        )
        StatItem(
            value = thisMonthCheckIns.toString(),
            label = if (isCurrentMonth) {
                stringResource(R.string.stat_this_month)
            } else {
                stringResource(R.string.stat_shown_month)
            },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatItem(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp)
                .semantics(mergeDescendants = true) {},
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 26.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CalendarCard(
    currentMonth: YearMonth,
    isCurrentMonth: Boolean,
    checkedInDates: Set<LocalDate>,
    today: LocalDate,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onTodayClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            MonthSelector(
                currentMonth = currentMonth,
                isCurrentMonth = isCurrentMonth,
                onPreviousMonth = onPreviousMonth,
                onNextMonth = onNextMonth,
                onTodayClick = onTodayClick
            )
            CalendarGrid(
                yearMonth = currentMonth,
                checkedInDates = checkedInDates,
                today = today
            )
            if (checkedInDates.isEmpty()) {
                Text(
                    text = stringResource(R.string.calendar_empty_month),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
            CalendarLegend()
        }
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
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(
                Icons.Rounded.ChevronLeft,
                contentDescription = stringResource(R.string.previous_month),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = currentMonth.format(formatter),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Box(
                modifier = Modifier.height(40.dp),
                contentAlignment = Alignment.Center
            ) {
                if (!isCurrentMonth) {
                    TextButton(onClick = onTodayClick) {
                        Text(
                            text = stringResource(R.string.back_to_today),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }

        IconButton(
            onClick = onNextMonth,
            enabled = !isCurrentMonth
        ) {
            Icon(
                Icons.Rounded.ChevronRight,
                contentDescription = stringResource(R.string.next_month),
                tint = if (isCurrentMonth) {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
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
    val weekDays = listOf(
        stringResource(R.string.sun) to stringResource(R.string.weekday_sun),
        stringResource(R.string.mon) to stringResource(R.string.weekday_mon),
        stringResource(R.string.tue) to stringResource(R.string.weekday_tue),
        stringResource(R.string.wed) to stringResource(R.string.weekday_wed),
        stringResource(R.string.thu) to stringResource(R.string.weekday_thu),
        stringResource(R.string.fri) to stringResource(R.string.weekday_fri),
        stringResource(R.string.sat) to stringResource(R.string.weekday_sat)
    )
    val firstDayOfMonth = yearMonth.atDay(1)
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7
    val totalCells = firstDayOfWeek + daysInMonth
    val rows = (totalCells + 6) / 7

    Column(modifier = Modifier.padding(horizontal = 8.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            weekDays.forEach { (shortLabel, fullLabel) ->
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = shortLabel,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.semantics { contentDescription = fullLabel }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        for (row in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0..6) {
                    val cellIndex = row * 7 + col
                    val dayOffset = cellIndex - firstDayOfWeek

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (dayOffset in 0 until daysInMonth) {
                            val date = yearMonth.atDay(dayOffset + 1)
                            DayCell(
                                date = date,
                                isCheckedIn = checkedInDates.contains(date),
                                isToday = date == today,
                                isFuture = date.isAfter(today)
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
    date: LocalDate,
    isCheckedIn: Boolean,
    isToday: Boolean,
    isFuture: Boolean
) {
    val dateText = date.format(DateTimeFormatter.ofPattern("M月d日", Locale.CHINA))
    val description = when {
        isToday && isCheckedIn -> stringResource(R.string.cd_day_checked_today, dateText)
        isCheckedIn -> stringResource(R.string.cd_day_checked, dateText)
        isToday -> stringResource(R.string.cd_day_today, dateText)
        isFuture -> stringResource(R.string.cd_day_future, dateText)
        else -> stringResource(R.string.cd_day_missed, dateText)
    }
    val marker = MaterialTheme.colorScheme.primary
    val onMarker = MaterialTheme.colorScheme.onPrimary

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clearAndSetSemantics { contentDescription = description }
            .then(
                if (isToday) {
                    Modifier.border(1.5.dp, marker, CircleShape)
                } else {
                    Modifier
                }
            )
            .padding(if (isToday && isCheckedIn) 3.dp else 0.dp)
            .clip(CircleShape)
            .background(if (isCheckedIn) marker else Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isToday || isCheckedIn) FontWeight.SemiBold else FontWeight.Normal,
            color = when {
                isCheckedIn -> onMarker
                isToday -> marker
                isFuture -> MaterialTheme.colorScheme.onSurfaceVariant
                else -> MaterialTheme.colorScheme.onSurface
            }
        )
    }
}

@Composable
private fun CalendarLegend() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        LegendItem(filled = true, label = stringResource(R.string.legend_checked))
        Spacer(modifier = Modifier.size(16.dp))
        LegendItem(filled = false, label = stringResource(R.string.legend_today))
    }
}

@Composable
private fun LegendItem(filled: Boolean, label: String) {
    val color = MaterialTheme.colorScheme.primary
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .then(
                    if (filled) {
                        Modifier.background(color, CircleShape)
                    } else {
                        Modifier.border(1.5.dp, color, CircleShape)
                    }
                )
        )
        Spacer(modifier = Modifier.size(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
