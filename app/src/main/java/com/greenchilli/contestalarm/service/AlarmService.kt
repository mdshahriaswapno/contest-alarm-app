package com.greenchilli.contestalarm.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import android.os.PowerManager
import com.greenchilli.contestalarm.ui.alarm.AlarmActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AlarmService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "STOP_ALARM") {
            sendBroadcast(Intent("com.greenchilli.contestalarm.ALARM_STOPPED"))
            stopSelf()
            return START_NOT_STICKY
        }

        val contestId = intent?.getStringExtra("CONTEST_ID") ?: ""
        val alarmDesc = intent?.getStringExtra("ALARM_DESC") ?: intent?.getStringExtra("CONTEST_NAME") ?: "Alarm"
        val alarmTitle = intent?.getStringExtra("ALARM_TITLE") ?: "Upcoming Contest"

        // Acquire WakeLock to keep CPU awake, but DO NOT wake screen here.
        // Waking screen here suppresses the FullScreenIntent! AlarmActivity will wake the screen.
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "ContestAlarm::AlarmWakeLock"
        )
        wakeLock?.acquire(10 * 60 * 1000L) // 10 minutes timeout

        // Disable it if it's a Custom Alarm (Custom IDs are UUIDs, they don't contain underscore)
        if (!contestId.contains("_") && contestId.isNotEmpty() && contestId != "-1") {
            GlobalScope.launch(Dispatchers.IO) {
                com.greenchilli.contestalarm.data.database.AppDatabase.getDatabase(this@AlarmService)
                    .customAlarmDao().updateAlarmStatus(contestId, false)
            }
        }

        startForegroundAlarm(contestId, alarmTitle, alarmDesc)
        
        // Play alarm in IO thread to avoid blocking main thread (prevents ANR on slow devices)
        GlobalScope.launch(Dispatchers.IO) {
            playAlarm()
        }

        return START_NOT_STICKY
    }

    private fun startForegroundAlarm(contestId: String, alarmTitle: String, alarmDesc: String) {
        val channelId = "contest_alarm_critical_v3" // Version 3 for silence
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create Channel (Same logic as before, ensuring it exists)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager.getNotificationChannel(channelId) == null) {
                val channel = NotificationChannel(
                    channelId,
                    "Contest & Custom Alarms (Critical)",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Full screen alarms for contests and custom notes"
                    enableVibration(true)
                    // SILENT CHANNEL: We handle sound via MediaPlayer
                    setSound(null, null) 
                    lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                }
                notificationManager.createNotificationChannel(channel)
            }
        }

        // Full Screen Intent
        val fullScreenIntent = Intent(this, AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("ALARM_DESC", alarmDesc)
            putExtra("ALARM_TITLE", alarmTitle)
            putExtra("CONTEST_ID", contestId)
        }
        val reqCode = (contestId.hashCode() and 0x7FFFFFFF)
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            reqCode,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Rely strictly on setFullScreenIntent so Android can properly downgrade to Heads-Up if screen is ON

        // Dismiss Action
        val stopIntent = Intent(this, AlarmService::class.java).apply { 
            action = "STOP_ALARM" 
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            reqCode,
            stopIntent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build Notification
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(com.greenchilli.contestalarm.R.mipmap.ic_launcher_round)
            .setContentTitle(alarmTitle)
            .setContentText(alarmDesc)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setContentIntent(fullScreenPendingIntent)
            .setAutoCancel(false) // Don't auto-cancel, wait for explicit stop
            .setOngoing(true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Dismiss", stopPendingIntent)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(reqCode, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(reqCode, notification)
        }
    }

    private fun playAlarm() {
        try {
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            
            mediaPlayer = MediaPlayer.create(this, alarmUri).apply {
                isLooping = true
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                start()
            }

            vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 500, 1000), 0))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(longArrayOf(0, 500, 1000), 0)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        vibrator?.cancel()
        wakeLock?.release()
    }
}
