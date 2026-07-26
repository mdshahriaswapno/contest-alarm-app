package com.greenchilli.contestalarm.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.greenchilli.contestalarm.data.database.ContestEntity
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
sealed class ContestUiState {
    object Loading : ContestUiState()
    data class Success(val contests: List<ContestEntity>) : ContestUiState()
    data class Error(val message: String) : ContestUiState()
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ContestListScreen(
    uiState: ContestUiState,
    onToggleAlarm: (ContestEntity, Long?) -> Unit,
    onRetry: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    // State for Dialog
    var showDialog by remember { mutableStateOf(false) }
    var selectedContest by remember { mutableStateOf<ContestEntity?>(null) }

    if (showDialog && selectedContest != null) {
        AlarmOffsetDialog(
            contest = selectedContest!!,
            onDismiss = { 
                showDialog = false 
                selectedContest = null
            },
            onConfirm = { offset ->
                onToggleAlarm(selectedContest!!, offset)
                showDialog = false
                selectedContest = null
            }
        )
    }

    // Pull to Refresh State
    val isRefreshing = uiState is ContestUiState.Loading
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = onRetry
    )

    // Premium Gradient Background
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1A1A2E), // Dark Navy
            Color(0xFF16213E), // Deep Blue
            Color(0xFF0F3460)  // Cyber Blue
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .pullRefresh(pullRefreshState)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Aesthetic Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Upcoming Contests",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                )
                IconButton(onClick = onNavigateToSettings) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                }
            }

            when (uiState) {
                is ContestUiState.Loading -> {
                    // Show only if list is empty, otherwise PullIndicator handles it
                    if (uiState !is ContestUiState.Success) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            // CircularProgressIndicator(color = Color(0xFFE94560)) // Duplicate with PullRefresh
                        }
                    }
                }
                is ContestUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = uiState.message,
                                color = Color(0xFFFF5555),
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            Button(
                                onClick = onRetry,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE94560))
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                }
                is ContestUiState.Success -> {
                    if (uiState.contests.isEmpty()) {
                         Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No upcoming contests found.", color = Color.Gray)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = onRetry) { Text("Refresh") }
                         }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(uiState.contests) { contest ->
                                ContestItem(
                                    contest = contest, 
                                    onToggleAlarm = { 
                                        if (it.isAlarmSet) {
                                            onToggleAlarm(it, null)
                                        } else {
                                            selectedContest = it
                                            showDialog = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
        
        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            backgroundColor = Color(0xFF222831),
            contentColor = Color(0xFFE94560)
        )
    }
}

@Composable
fun AlarmOffsetDialog(
    contest: ContestEntity,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF222831),
        titleContentColor = Color.White,
        textContentColor = Color.White,
        title = { Text("Set Alarm Before") },
        text = {
            Column {
                val timeRemainingSeconds = contest.startTimeSeconds - (System.currentTimeMillis() / 1000)
                
                val options = listOf(
                    "Test (Ring in 5s)" to -1L,
                    "15 Minutes" to 900L,
                    "30 Minutes" to 1800L,
                    "1 Hour" to 3600L,
                    "2 Hours" to 7200L
                )
                
                options.filter { (_, seconds) ->
                     // Key Check: Only show option if Time Remaining > Option Duration
                     // (Except for Test option -1, always show that)
                     seconds == -1L || timeRemainingSeconds > seconds
                }.forEach { (label, seconds) ->
                     TextButton(
                         onClick = { onConfirm(seconds) },
                         modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                         colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFE94560))
                     ) {
                         Text(label, fontSize = 16.sp)
                     }
                }
            }
        },
        confirmButton = {},
        dismissButton = { 
            TextButton(onClick = onDismiss) { 
                Text("Cancel", color = Color.Gray) 
            } 
        }
    )
}

@Composable
fun ContestItem(
    contest: ContestEntity,
    onToggleAlarm: (ContestEntity) -> Unit
) {
    val platformColor = when (contest.platform.lowercase()) {
        "codeforces" -> Color(0xFFE94560)
        "atcoder" -> Color(0xFF000000)
        "codechef" -> Color(0xFF5D4037)
        "leetcode" -> Color(0xFFFFA116)
        else -> Color(0xFF4B4B4B)
    }

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF222831)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    color = platformColor,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Text(
                        text = contest.platform.uppercase(),
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                
                Switch(
                    checked = contest.isAlarmSet,
                    onCheckedChange = { onToggleAlarm(contest) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFFE94560),
                        checkedTrackColor = Color(0xFFE94560).copy(alpha = 0.5f)
                    )
                )
            }

            Text(
                text = contest.name,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            
            Spacer(modifier = Modifier.height(8.dp))

            val date = Date(contest.startTimeSeconds * 1000)
            val format = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
            
            val hours = TimeUnit.SECONDS.toHours(contest.durationSeconds.toLong())
            val minutes = TimeUnit.SECONDS.toMinutes(contest.durationSeconds.toLong()) % 60
            val durationStr = if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
            
            // Calculate Starts In
            val timeDiff = (contest.startTimeSeconds * 1000) - System.currentTimeMillis()
            val startsInStr = if (timeDiff > 0) {
                val h = TimeUnit.MILLISECONDS.toHours(timeDiff)
                val m = TimeUnit.MILLISECONDS.toMinutes(timeDiff) % 60
                "Starts in: ${h}h ${m}m"
            } else {
                "Started"
            }

            Column {
                Text(
                    text = "${format.format(date)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFDDDDDD)
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = "Duration: $durationStr",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Text(
                         text = startsInStr,
                         style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                         color = if (timeDiff < 3600000) Color(0xFFE94560) else Color(0xFF4CAF50) // Red if < 1h
                    )
                }
            }
            
            if (contest.isAlarmSet) {
                 Spacer(modifier = Modifier.height(4.dp))
                 val offsetMin = contest.alarmOffsetSeconds / 60
                 Text(
                     text = "🔔 Alarm set for $offsetMin min before",
                     style = MaterialTheme.typography.labelSmall,
                     color = Color(0xFFE94560)
                 )
            }
        }
    }
}
