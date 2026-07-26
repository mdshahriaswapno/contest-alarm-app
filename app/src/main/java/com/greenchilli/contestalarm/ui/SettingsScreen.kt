package com.greenchilli.contestalarm.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.greenchilli.contestalarm.data.preferences.AutoAlarmSettings
import com.greenchilli.contestalarm.data.preferences.UserPreferencesManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    preferencesManager: UserPreferencesManager,
    onNavigateBack: () -> Unit
) {
    val settings by preferencesManager.autoAlarmSettings.collectAsState(
        initial = AutoAlarmSettings(1800L, false, false, false, false, false, false, false, false, false, false)
    )
    val coroutineScope = rememberCoroutineScope()
    
    var showOffsetDialog by remember { mutableStateOf(false) }

    if (showOffsetDialog) {
        DefaultOffsetDialog(
            currentOffset = settings.defaultOffsetSeconds,
            onDismiss = { showOffsetDialog = false },
            onConfirm = { offset ->
                coroutineScope.launch {
                    preferencesManager.updateDefaultOffset(offset)
                }
                showOffsetDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF16213E))
            )
        },
        containerColor = Color(0xFF1A1A2E)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            
            Text(
                "Global Auto-Alarm Offset",
                color = Color.Gray,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF222831)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                onClick = { showOffsetDialog = true }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Default Time Before Contest", color = Color.White)
                    
                    val offsetLabel = when (settings.defaultOffsetSeconds) {
                        -1L -> "Test (5s)"
                        900L -> "15 min"
                        1800L -> "30 min"
                        3600L -> "1 hour"
                        7200L -> "2 hours"
                        else -> "${settings.defaultOffsetSeconds / 60} min"
                    }
                    Text(offsetLabel, color = Color(0xFFE94560), fontWeight = FontWeight.Bold)
                }
            }

            Text(
                "Auto-Alarm Rules (Codeforces)",
                color = Color.Gray,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                "If enabled, alarms will be automatically set for new contests matching these categories.",
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            RuleSwitch(
                label = "Div. 1",
                checked = settings.div1,
                onCheckedChange = { coroutineScope.launch { preferencesManager.updateAutoAlarm(UserPreferencesManager.AUTO_ALARM_DIV1, it) } }
            )
            RuleSwitch(
                label = "Div. 2",
                checked = settings.div2,
                onCheckedChange = { coroutineScope.launch { preferencesManager.updateAutoAlarm(UserPreferencesManager.AUTO_ALARM_DIV2, it) } }
            )
            RuleSwitch(
                label = "Div. 3",
                checked = settings.div3,
                onCheckedChange = { coroutineScope.launch { preferencesManager.updateAutoAlarm(UserPreferencesManager.AUTO_ALARM_DIV3, it) } }
            )
            RuleSwitch(
                label = "Div. 4",
                checked = settings.div4,
                onCheckedChange = { coroutineScope.launch { preferencesManager.updateAutoAlarm(UserPreferencesManager.AUTO_ALARM_DIV4, it) } }
            )
            RuleSwitch(
                label = "Educational Rounds",
                checked = settings.educational,
                onCheckedChange = { coroutineScope.launch { preferencesManager.updateAutoAlarm(UserPreferencesManager.AUTO_ALARM_EDU, it) } }
            )
            RuleSwitch(
                label = "Global / Hello / Good Bye",
                checked = settings.global,
                onCheckedChange = { coroutineScope.launch { preferencesManager.updateAutoAlarm(UserPreferencesManager.AUTO_ALARM_GLOBAL, it) } }
            )

            Text(
                "Auto-Alarm Rules (AtCoder)",
                color = Color.Gray,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
            RuleSwitch(
                label = "Beginner Contest (ABC)",
                checked = settings.atcoderBeginner,
                onCheckedChange = { coroutineScope.launch { preferencesManager.updateAutoAlarm(UserPreferencesManager.AUTO_ALARM_ATCODER_BEGINNER, it) } }
            )
            RuleSwitch(
                label = "Regular Contest (ARC)",
                checked = settings.atcoderRegular,
                onCheckedChange = { coroutineScope.launch { preferencesManager.updateAutoAlarm(UserPreferencesManager.AUTO_ALARM_ATCODER_REGULAR, it) } }
            )
            RuleSwitch(
                label = "Grand Contest (AGC)",
                checked = settings.atcoderGrand,
                onCheckedChange = { coroutineScope.launch { preferencesManager.updateAutoAlarm(UserPreferencesManager.AUTO_ALARM_ATCODER_GRAND, it) } }
            )

            Text(
                "Auto-Alarm Rules (CodeChef)",
                color = Color.Gray,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
            RuleSwitch(
                label = "Starters",
                checked = settings.codechefStarters,
                onCheckedChange = { coroutineScope.launch { preferencesManager.updateAutoAlarm(UserPreferencesManager.AUTO_ALARM_CODECHEF_STARTERS, it) } }
            )
        }
    }
}

@Composable
fun RuleSwitch(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Color.White, fontSize = 16.sp)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFFE94560),
                checkedTrackColor = Color(0xFFE94560).copy(alpha = 0.5f)
            )
        )
    }
}

@Composable
fun DefaultOffsetDialog(
    currentOffset: Long,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF222831),
        titleContentColor = Color.White,
        textContentColor = Color.White,
        title = { Text("Select Default Offset") },
        text = {
            Column {
                val options = listOf(
                    "Test (Ring in 5s)" to -1L,
                    "15 Minutes" to 900L,
                    "30 Minutes" to 1800L,
                    "1 Hour" to 3600L,
                    "2 Hours" to 7200L
                )
                
                options.forEach { (label, seconds) ->
                    val isSelected = currentOffset == seconds
                    TextButton(
                        onClick = { onConfirm(seconds) },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            .background(if (isSelected) Color(0xFFE94560).copy(alpha = 0.2f) else Color.Transparent, RoundedCornerShape(8.dp)),
                        colors = ButtonDefaults.textButtonColors(contentColor = if (isSelected) Color(0xFFE94560) else Color.White)
                    ) {
                        Text(label, fontSize = 16.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { 
            TextButton(onClick = onDismiss) { Text("Cancel", color = Color.Gray) } 
        }
    )
}
