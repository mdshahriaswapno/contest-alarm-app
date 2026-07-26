package com.greenchilli.contestalarm.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.os.Build
import androidx.core.app.NotificationCompat
import com.greenchilli.contestalarm.ui.alarm.AlarmActivity

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "CANCEL_SNOOZE") {
            val cancelId = intent.getStringExtra("CONTEST_ID") ?: return
            val scheduler = com.greenchilli.contestalarm.domain.AlarmScheduler(context)
            scheduler.cancelAlarm(cancelId)
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel((cancelId.hashCode() and 0x7FFFFFFF) + 1)
            return
        }

        val contestId = intent.getStringExtra("CONTEST_ID") ?: return
        val alarmDesc = intent.getStringExtra("ALARM_DESC") ?: intent.getStringExtra("CONTEST_NAME") ?: "Alarm"
        val alarmTitle = intent.getStringExtra("ALARM_TITLE") ?: "Upcoming Contest"

        // Clear any existing snooze notification when alarm triggers
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel((contestId.hashCode() and 0x7FFFFFFF) + 1)

        val serviceIntent = Intent(context, com.greenchilli.contestalarm.service.AlarmService::class.java).apply {
            putExtra("CONTEST_ID", contestId)
            putExtra("ALARM_DESC", alarmDesc)
            putExtra("ALARM_TITLE", alarmTitle)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }
}
