package com.greenchilli.contestalarm.ui.alarm

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.greenchilli.contestalarm.data.database.CustomAlarmEntity
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomAlarmsScreen(viewModel: CustomAlarmViewModel) {
    val alarms by viewModel.customAlarms.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Custom Alarms", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Alarm")
            }
        }
    ) { padding ->
        val currentTime = System.currentTimeMillis()
        val upcomingAlarms = alarms.filter { it.triggerTimeMillis >= currentTime }
        val pastAlarms = alarms.filter { it.triggerTimeMillis < currentTime }

        if (alarms.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No custom alarms set", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (upcomingAlarms.isNotEmpty()) {
                    item {
                        Text("Upcoming Alarms", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp))
                    }
                    items(upcomingAlarms, key = { it.id }) { alarm ->
                        CustomAlarmItem(
                            alarm = alarm,
                            isPast = false,
                            onToggle = { isEnabled -> viewModel.toggleAlarm(alarm, isEnabled) },
                            onDelete = { viewModel.deleteAlarm(alarm) }
                        )
                    }
                }
                
                if (pastAlarms.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Past Alarms", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.padding(bottom = 4.dp))
                    }
                    items(pastAlarms, key = { it.id }) { alarm ->
                        CustomAlarmItem(
                            alarm = alarm,
                            isPast = true,
                            onToggle = { isEnabled -> viewModel.toggleAlarm(alarm, isEnabled) },
                            onDelete = { viewModel.deleteAlarm(alarm) }
                        )
                    }
                }
            }
        }

        if (showAddDialog) {
            AddCustomAlarmDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { note, timeMillis ->
                    viewModel.addAlarm(note, timeMillis)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun CustomAlarmItem(
    alarm: CustomAlarmEntity,
    isPast: Boolean,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    val formatter = remember { SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault()) }
    val timeString = formatter.format(Date(alarm.triggerTimeMillis))

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = alarm.note,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isPast) Color.Gray else MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = timeString,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isPast) Color.Gray else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (!isPast) {
                Switch(
                    checked = alarm.isEnabled,
                    onCheckedChange = onToggle
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun AddCustomAlarmDialog(
    onDismiss: () -> Unit,
    onAdd: (note: String, timeMillis: Long) -> Unit
) {
    var note by remember { mutableStateOf("") }
    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }

    // Date & Time states
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }
    var selectedTimeStr by remember { mutableStateOf<String?>(null) }

    val datePickerDialog = DatePickerDialog(
        context,
        com.greenchilli.contestalarm.R.style.CustomPickerTheme,
        { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            selectedDateMillis = calendar.timeInMillis
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )
    datePickerDialog.datePicker.minDate = System.currentTimeMillis()

    val timePickerDialog = TimePickerDialog(
        context,
        com.greenchilli.contestalarm.R.style.CustomPickerTheme,
        { _, hourOfDay, minute ->
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)
            calendar.set(Calendar.SECOND, 0)
            selectedDateMillis = calendar.timeInMillis
            val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
            selectedTimeStr = sdf.format(calendar.time)
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        false
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Custom Alarm") },
        text = {
            Column {
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Alarm Note / Reason") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (selectedDateMillis != null) {
                            SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(selectedDateMillis!!))
                        } else {
                            "Select Date"
                        }
                    )
                    Button(onClick = { datePickerDialog.show() }) {
                        Text("Date")
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = selectedTimeStr ?: "Select Time")
                    Button(onClick = { timePickerDialog.show() }) {
                        Text("Time")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (note.isNotBlank() && selectedDateMillis != null && selectedTimeStr != null) {
                        onAdd(note, calendar.timeInMillis)
                    }
                },
                enabled = note.isNotBlank() && selectedDateMillis != null && selectedTimeStr != null
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
