package com.greenchilli.contestalarm.ui.alarm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.greenchilli.contestalarm.data.database.CustomAlarmDao
import com.greenchilli.contestalarm.data.database.CustomAlarmEntity
import com.greenchilli.contestalarm.domain.AlarmScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class CustomAlarmViewModel(
    private val dao: CustomAlarmDao,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    init {
        viewModelScope.launch {
            // Auto delete alarms older than 2 days (48 hours)
            val threshold = System.currentTimeMillis() - (48 * 60 * 60 * 1000L)
            dao.deleteAlarmsOlderThan(threshold)
        }
    }

    val customAlarms: StateFlow<List<CustomAlarmEntity>> = dao.getAllCustomAlarms()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addAlarm(note: String, triggerTimeMillis: Long) {
        viewModelScope.launch {
            val id = UUID.randomUUID().toString()
            val alarm = CustomAlarmEntity(id, note, triggerTimeMillis, true)
            dao.insertCustomAlarm(alarm)
            
            // Schedule the alarm
            alarmScheduler.scheduleAlarm(id, note, triggerTimeMillis, "Custom Alarm")
        }
    }

    fun toggleAlarm(alarm: CustomAlarmEntity, isEnabled: Boolean) {
        viewModelScope.launch {
            dao.updateAlarmStatus(alarm.id, isEnabled)
            if (isEnabled) {
                alarmScheduler.scheduleAlarm(alarm.id, alarm.note, alarm.triggerTimeMillis, "Custom Alarm")
            } else {
                alarmScheduler.cancelAlarm(alarm.id)
            }
        }
    }

    fun deleteAlarm(alarm: CustomAlarmEntity) {
        viewModelScope.launch {
            dao.deleteCustomAlarm(alarm.id)
            alarmScheduler.cancelAlarm(alarm.id)
        }
    }
}
