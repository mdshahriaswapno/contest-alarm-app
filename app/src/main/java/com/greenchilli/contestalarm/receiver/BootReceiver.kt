package com.greenchilli.contestalarm.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

// In a real app, you would inject the repository or use a Worker to reschedule all alarms from DB
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val pendingResult = goAsync()
            kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    val db = com.greenchilli.contestalarm.data.database.AppDatabase.getDatabase(context)
                    val alarmScheduler = com.greenchilli.contestalarm.domain.AlarmScheduler(context)
                    
                    val customAlarms = db.customAlarmDao().getAllCustomAlarmsSync()
                    for (alarm in customAlarms) {
                        if (alarm.isEnabled && alarm.triggerTimeMillis > System.currentTimeMillis()) {
                            alarmScheduler.scheduleAlarm(alarm.id, alarm.note, alarm.triggerTimeMillis, "Custom Alarm")
                        } else if (alarm.isEnabled) {
                            db.customAlarmDao().updateAlarmStatus(alarm.id, false)
                        }
                    }
                    
                    val contestAlarms = db.contestDao().getAlarmsSetSync()
                    for (contest in contestAlarms) {
                        val triggerTime = (contest.startTimeSeconds * 1000) - (contest.alarmOffsetSeconds * 1000L)
                        if (triggerTime > System.currentTimeMillis()) {
                            alarmScheduler.scheduleAlarm(contest.id, contest.name, triggerTime)
                        } else {
                            db.contestDao().updateAlarmStatus(contest.id, false, contest.alarmOffsetSeconds)
                        }
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
