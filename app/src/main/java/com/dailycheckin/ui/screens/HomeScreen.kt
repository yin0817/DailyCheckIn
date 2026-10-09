package com.dailycheckin.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dailycheckin.R
import com.dailycheckin.data.CheckInDataStore
import com.dailycheckin.service.ReminderService
import com.dailycheckin.util.NotificationHelper
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HomeScreen(
    dataStore: CheckInDataStore,
    onNavigateToCalendar: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val checkInDates by dataStore.checkInDates.collectAsState(
        initial = dataStore.getCheckInDatesSync()
    )

    val today = LocalDate.now()
    var pendingDate by remember { mutableStateOf<LocalDate?>(null) }
    val isCheckedIn = pendingDate == today || checkInDates.contains(
        today.format(DateTimeFormatter.ISO_LOCAL_DATE)
    )
    val consecutiveDays = remember(checkInDates) { dataStore.getConsecutiveDays() }
    val totalCheckIns = checkInDates.size
    val presence = when {
        consecutiveDays > 0 -> stringResource(
            R.string.presence_streak,
            consecutiveDays,
            totalCheckIns
        )
        totalCheckIns > 0 -> stringResource(R.string.presence_total, totalCheckIns)
        else -> stringResource(R.string.presence_empty)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp)
    ) {
        TopBar(
            onCalendarClick = onNavigateToCalendar,
            onSettingsClick = onNavigateToSettings
        )

        Spacer(modifier = Modifier.height(36.dp))

        Text(
            text = today.format(DateTimeFormatter.ofPattern("M月d日", Locale.CHINA)),
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.semantics { heading() }
        )
        Text(
            text = today.format(DateTimeFormatter.ofPattern("EEEE", Locale.CHINA)),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(48.dp))

        CheckInStamp(
            isCheckedIn = isCheckedIn,
            onClick = {
                if (!isCheckedIn) {
                    val date = LocalDate.now()
                    pendingDate = date
                    scope.launch {
                        try {
                            dataStore.addCheckIn(date)
                            NotificationHelper.scheduleDailyReminder(context)
                            ReminderService.refresh(context)
                        } catch (_: Exception) {
                            if (pendingDate == date) pendingDate = null
                        }
                    }
                }
            }
        )

        Text(
            text = if (isCheckedIn) {
                stringResource(R.string.marked_today)
            } else {
                stringResource(R.string.check_in_hint)
            },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .padding(top = 20.dp)
                .clearAndSetSemantics {}
        )

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = presence,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .clickable(
                    onClickLabel = stringResource(R.string.open_calendar),
                    role = Role.Button,
                    onClick = onNavigateToCalendar
                )
                .padding(vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun TopBar(
    onCalendarClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onCalendarClick) {
            Icon(
                painter = painterResource(R.drawable.ic_lucide_calendar),
                contentDescription = stringResource(R.string.calendar),
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
        IconButton(onClick = onSettingsClick) {
            Icon(
                painter = painterResource(R.drawable.ic_lucide_settings),
                contentDescription = stringResource(R.string.settings),
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
private fun CheckInStamp(
    isCheckedIn: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && !isCheckedIn) 0.97f else 1f,
        animationSpec = tween(120),
        label = "stampScale"
    )
    val fill by animateColorAsState(
        targetValue = if (isCheckedIn) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            Color.Transparent
        },
        animationSpec = tween(180),
        label = "stampFill"
    )
    val content by animateColorAsState(
        targetValue = if (isCheckedIn) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onBackground
        },
        animationSpec = tween(180),
        label = "stampContent"
    )
    val description = if (isCheckedIn) {
        stringResource(R.string.checked_in_today)
    } else {
        stringResource(R.string.check_in_today)
    }
    val shape = RoundedCornerShape(20.dp)

    Box(
        modifier = Modifier
            .size(156.dp)
            .scale(scale)
            .clip(shape)
            .background(fill)
            .border(
                width = if (isCheckedIn) 0.dp else 1.5.dp,
                color = MaterialTheme.colorScheme.onBackground,
                shape = shape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                enabled = !isCheckedIn,
                role = Role.Button,
                onClick = onClick
            )
            .semantics {
                contentDescription = description
                if (isCheckedIn) disabled()
            },
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = isCheckedIn,
            transitionSpec = {
                fadeIn(tween(160)) togetherWith fadeOut(tween(100))
            },
            label = "stampGlyph"
        ) { checked ->
            if (checked) {
                Icon(
                    painter = painterResource(R.drawable.ic_lucide_check),
                    contentDescription = null,
                    tint = content,
                    modifier = Modifier
                        .size(72.dp)
                        .clearAndSetSemantics {}
                )
            } else {
                Text(
                    text = stringResource(R.string.check_in_action),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Medium,
                    color = content,
                    modifier = Modifier.clearAndSetSemantics {}
                )
            }
        }
    }
}
