package com.dailycheckin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
    val summary = calendarSummary(
        isCurrentMonth = isCurrentMonth,
        monthValue = currentMonth.monthValue,
        monthCount = checkedInDates.size,
        consecutiveDays = consecutiveDays
    )

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
                .padding(horizontal = 20.dp)
        ) {
            MonthSelector(
                currentMonth = currentMonth,
                isCurrentMonth = isCurrentMonth,
                onPreviousMonth = { currentMonth = currentMonth.minusMonths(1) },
                onNextMonth = { currentMonth = currentMonth.plusMonths(1) },
                onTodayClick = { currentMonth = YearMonth.now() }
            )
            Text(
                text = summary,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )
            CalendarGrid(
                yearMonth = currentMonth,
                checkedInDates = checkedInDates,
                today = today
            )
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun calendarSummary(
    isCurrentMonth: Boolean,
    monthValue: Int,
    monthCount: Int,
    consecutiveDays: Int
): String {
    if (monthCount == 0) {
        return if (consecutiveDays > 0 && isCurrentMonth) {
            stringResource(R.string.calendar_summary_streak_blank, consecutiveDays)
        } else {
            stringResource(R.string.calendar_empty_month)
        }
    }
    return when {
        consecutiveDays > 0 && isCurrentMonth ->
            stringResource(R.string.calendar_summary_streak_month, consecutiveDays, monthCount)
        consecutiveDays > 0 ->
            stringResource(
                R.string.calendar_summary_streak_other,
                consecutiveDays,
                monthValue,
                monthCount
            )
        isCurrentMonth -> stringResource(R.string.calendar_summary_month, monthCount)
        else -> stringResource(R.string.calendar_summary_other, monthValue, monthCount)
    }
}

@Composable
private fun TopBar(onNavigateBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 20.dp, top = 4.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onNavigateBack) {
            Icon(
                painter = painterResource(R.drawable.ic_lucide_arrow_left),
                contentDescription = stringResource(R.string.back),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Text(
            text = stringResource(R.string.calendar_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.semantics { heading() }
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
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = currentMonth.format(formatter),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .weight(1f)
                .semantics { heading() }
        )
        IconButton(onClick = onPreviousMonth) {
            Icon(
                painter = painterResource(R.drawable.ic_lucide_chevron_left),
                contentDescription = stringResource(R.string.previous_month),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        IconButton(
            onClick = onNextMonth,
            enabled = !isCurrentMonth
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_lucide_chevron_right),
                contentDescription = stringResource(R.string.next_month),
                tint = if (isCurrentMonth) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )
        }
    }
    if (!isCurrentMonth) {
        Text(
            text = stringResource(R.string.back_to_today),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(top = 2.dp, bottom = 4.dp)
                .clickable(onClick = onTodayClick)
        )
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
    val firstDayOfWeek = yearMonth.atDay(1).dayOfWeek.value % 7
    val daysInMonth = yearMonth.lengthOfMonth()
    val rows = (firstDayOfWeek + daysInMonth + 6) / 7

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            weekDays.forEach { (shortLabel, fullLabel) ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = shortLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.semantics { contentDescription = fullLabel }
                    )
                }
            }
        }
        HorizontalDivider(
            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outline
        )
        for (row in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0..6) {
                    val dayOffset = row * 7 + col - firstDayOfWeek
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f),
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
    val seal = MaterialTheme.colorScheme.primary
    val onSeal = MaterialTheme.colorScheme.onPrimary
    val numberColor = when {
        isCheckedIn -> onSeal
        isToday -> seal
        isFuture -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.onBackground
    }

    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(if (isCheckedIn) seal else androidx.compose.ui.graphics.Color.Transparent)
            .then(
                if (isToday && !isCheckedIn) {
                    Modifier.border(2.dp, seal, CircleShape)
                } else {
                    Modifier
                }
            )
            .clearAndSetSemantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isToday || isCheckedIn) FontWeight.Medium else FontWeight.Normal,
            color = numberColor
        )
    }
}
