package com.dailycheckin.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dailycheckin.R
import com.dailycheckin.data.CheckInDataStore
import com.dailycheckin.util.NotificationHelper
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
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
    
    // 动画状态
    val scale by animateFloatAsState(
        targetValue = if (isCheckedIn) 1f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.settings)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 连续打卡天数显示
            AnimatedVisibility(
                visible = consecutiveDays > 0,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                Card(
                    modifier = Modifier.padding(bottom = 48.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = consecutiveDays.toString(),
                            style = MaterialTheme.typography.displayLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.consecutive_days, consecutiveDays),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            
            // 首次打卡提示
            AnimatedVisibility(
                visible = consecutiveDays == 0 && !isCheckedIn,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Text(
                    text = stringResource(R.string.first_check_in),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 48.dp)
                )
            }
            
            // 打卡按钮
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .scale(if (!isCheckedIn) scale else 1f)
                    .background(
                        color = if (isCheckedIn) {
                            MaterialTheme.colorScheme.surfaceVariant
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                        shape = CircleShape
                    )
                    .clickable(
                        enabled = !isCheckedIn,
                        onClick = {
                            scope.launch {
                                dataStore.addCheckIn(LocalDate.now())
                                isCheckedIn = true
                                consecutiveDays = dataStore.getConsecutiveDays()
                                // 重新设置明天的提醒
                                NotificationHelper.scheduleDailyReminder(context)
                            }
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = if (isCheckedIn) {
                            Icons.Default.Check
                        } else {
                            Icons.Default.ThumbUp
                        },
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = if (isCheckedIn) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onPrimary
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isCheckedIn) {
                            stringResource(R.string.checked_in_today)
                        } else {
                            stringResource(R.string.check_in_today)
                        },
                        style = MaterialTheme.typography.titleLarge,
                        color = if (isCheckedIn) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onPrimary
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(64.dp))
            
            // 日历入口
            OutlinedButton(
                onClick = onNavigateToCalendar,
                modifier = Modifier.fillMaxWidth(0.7f),
                shape = MaterialTheme.shapes.large
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.calendar))
            }
        }
    }
}

@Composable
private fun stringResource(id: Int): String {
    return LocalContext.current.getString(id)
}

@Composable
private fun stringResource(id: Int, vararg formatArgs: Any): String {
    return LocalContext.current.getString(id, *formatArgs)
}
