package com.dailycheckin.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dailycheckin.R
import com.dailycheckin.data.CheckInDataStore
import com.dailycheckin.service.ReminderService
import com.dailycheckin.ui.theme.doneContainerColor
import com.dailycheckin.ui.theme.doneContentColor
import com.dailycheckin.util.NotificationHelper
import androidx.compose.runtime.rememberCoroutineScope
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
    val dateFormatter = DateTimeFormatter.ofPattern("M月d日 EEEE", Locale.CHINA)
    var pendingDate by remember { mutableStateOf<LocalDate?>(null) }
    val isCheckedIn = pendingDate == today || checkInDates.contains(
        today.format(DateTimeFormatter.ISO_LOCAL_DATE)
    )
    val consecutiveDays = remember(checkInDates) { dataStore.getConsecutiveDays() }
    val totalCheckIns = checkInDates.size
    val streakDescription = if (consecutiveDays > 0) {
        stringResource(R.string.consecutive_days, consecutiveDays)
    } else {
        stringResource(R.string.cd_streak_empty)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
            TopBar(
                onCalendarClick = onNavigateToCalendar,
                onSettingsClick = onNavigateToSettings
            )

            Text(
                text = today.format(dateFormatter),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 12.dp)
            )

            Spacer(modifier = Modifier.height(56.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clearAndSetSemantics {
                    contentDescription = streakDescription
                }
            ) {
                Text(
                    text = consecutiveDays.toString(),
                    style = MaterialTheme.typography.displayLarge,
                    color = if (consecutiveDays == 0) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onBackground
                    }
                )
                Text(
                    text = if (consecutiveDays > 0) {
                        stringResource(R.string.streak_caption)
                    } else {
                        stringResource(R.string.streak_empty_caption)
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            CheckInButton(
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
                    stringResource(R.string.checked_in_today)
                } else {
                    stringResource(R.string.check_in_hint)
                },
                style = MaterialTheme.typography.bodyLarge,
                color = if (isCheckedIn) {
                    doneContentColor()
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(top = 16.dp, start = 32.dp, end = 32.dp)
                    .then(
                        if (isCheckedIn) Modifier.clearAndSetSemantics {} else Modifier
                    )
            )

            Spacer(modifier = Modifier.height(48.dp))

            BottomStats(
                totalCheckIns = totalCheckIns,
                onCalendarClick = onNavigateToCalendar
            )

            Spacer(modifier = Modifier.height(28.dp))
    }
}

@Composable
private fun TopBar(
    onCalendarClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 4.dp)
    ) {
        IconButton(
            onClick = onCalendarClick,
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Icon(
                Icons.Outlined.CalendarMonth,
                contentDescription = stringResource(R.string.calendar),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .align(Alignment.Center)
                .semantics { heading() }
        )

        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Icon(
                Icons.Outlined.Settings,
                contentDescription = stringResource(R.string.settings),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CheckInButton(
    isCheckedIn: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && !isCheckedIn) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "checkInScale"
    )
    val elevation by animateDpAsState(
        targetValue = if (isCheckedIn) 0.dp else 12.dp,
        label = "checkInElevation"
    )
    val containerColor by animateColorAsState(
        targetValue = if (isCheckedIn) doneContainerColor() else MaterialTheme.colorScheme.primary,
        animationSpec = tween(220),
        label = "checkInContainer"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isCheckedIn) doneContentColor() else MaterialTheme.colorScheme.onPrimary,
        animationSpec = tween(220),
        label = "checkInContent"
    )
    val description = if (isCheckedIn) {
        stringResource(R.string.checked_in_today)
    } else {
        stringResource(R.string.check_in_today)
    }

    Box(
        modifier = Modifier
            .size(156.dp)
            .scale(scale)
            .shadow(
                elevation = elevation,
                shape = CircleShape,
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.32f)
            )
            .clip(CircleShape)
            .background(containerColor)
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
                fadeIn(animationSpec = tween(180)) togetherWith fadeOut(animationSpec = tween(120))
            },
            label = "checkInGlyph"
        ) { checked ->
            if (checked) {
                Icon(
                    Icons.Rounded.Check,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier
                        .size(56.dp)
                        .clearAndSetSemantics {}
                )
            } else {
                Text(
                    text = stringResource(R.string.check_in_action),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor,
                    modifier = Modifier.clearAndSetSemantics {}
                )
            }
        }
    }
}

@Composable
private fun BottomStats(
    totalCheckIns: Int,
    onCalendarClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onCalendarClick)
                .semantics(mergeDescendants = true) {}
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.total_check_in_label),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = totalCheckIns.toString(),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = " ${stringResource(R.string.day_unit)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.open_calendar),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Icon(
                Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
