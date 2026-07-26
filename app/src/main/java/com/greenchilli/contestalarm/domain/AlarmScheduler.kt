package com.greenchilli.contestalarm.domain

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.greenchilli.contestalarm.receiver.AlarmReceiver
import android.provider.Settings

class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleAlarm(contestId: String, contestName: String, triggerTimeMillis: Long, alarmTitle: String? = null): Boolean {
        // Double check permission for Android 12+ (API 31+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                // Cannot schedule exact alarms, need user permission
                return false
            }
        }

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("CONTEST_ID", contestId)
            putExtra("CONTEST_NAME", contestName)
            if (alarmTitle != null) {
                putExtra("ALARM_TITLE", alarmTitle)
            }
        }

        // Use hashCode of the String ID for the PendingIntent RequestCode (needs Int)
        val requestCode = (contestId.hashCode() and 0x7FFFFFFF)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerTimeMillis, pendingIntent)
        alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
        return true
    }

    fun cancelAlarm(contestId: String) {
        val requestCode = (contestId.hashCode() and 0x7FFFFFFF)
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
        }
    }
}
