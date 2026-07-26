package com.greenchilli.contestalarm.ui.alarm

import android.app.KeyguardManager
import android.content.Context
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.greenchilli.contestalarm.ui.theme.ContestAlarmTheme

class AlarmActivity : ComponentActivity() {

    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        showOnLockScreen()
        super.onCreate(savedInstanceState)
        
        val alarmDesc = intent.getStringExtra("ALARM_DESC") ?: intent.getStringExtra("CONTEST_NAME") ?: "Alarm"
        val alarmTitle = intent.getStringExtra("ALARM_TITLE") ?: "Upcoming Contest"
        val contestId = intent.getStringExtra("CONTEST_ID") ?: ""
        // Sound is played by Service

        setContent {
            ContestAlarmTheme {
                AlarmScreen(
                    alarmTitle = alarmTitle, 
                    alarmDesc = alarmDesc, 
                    onDismiss = {
                        stopAlarmService()
                        finish()
                    },
                    onSnooze = {
                        snoozeAlarm(contestId, alarmDesc, alarmTitle)
                        finish()
                    }
                )
            }
        }

        val filter = android.content.IntentFilter("com.greenchilli.contestalarm.ALARM_STOPPED")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(stopReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(stopReceiver, filter)
        }
    }

    private val stopReceiver = object : android.content.BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: android.content.Intent?) {
            finish()
        }
    }

    private fun snoozeAlarm(contestId: String, alarmDesc: String, alarmTitle: String) {
        val triggerTime = System.currentTimeMillis() + 5 * 60 * 1000L
        val scheduler = com.greenchilli.contestalarm.domain.AlarmScheduler(this)
        scheduler.scheduleAlarm(
            contestId = contestId,
            contestName = alarmDesc,
            triggerTimeMillis = triggerTime,
            alarmTitle = "Snoozed: $alarmTitle"
        )
        showSnoozeNotification(contestId, alarmTitle, triggerTime)
        stopAlarmService()
    }

    private fun showSnoozeNotification(contestId: String, alarmTitle: String, triggerTime: Long) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        val channelId = "snooze_channel_v2"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                channelId,
                "Snoozed Alarms",
                android.app.NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val sdf = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
        val timeStr = sdf.format(java.util.Date(triggerTime))

        val cancelIntent = android.content.Intent(this, com.greenchilli.contestalarm.receiver.AlarmReceiver::class.java).apply {
            action = "CANCEL_SNOOZE"
            putExtra("CONTEST_ID", contestId)
        }
        val reqCode = contestId.hashCode() and 0x7FFFFFFF
        val snoozeNotificationId = reqCode + 1
        
        val cancelPendingIntent = android.app.PendingIntent.getBroadcast(
            this,
            reqCode,
            cancelIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val contentIntent = android.content.Intent(this, com.greenchilli.contestalarm.MainActivity::class.java)
        val contentPendingIntent = android.app.PendingIntent.getActivity(
            this,
            reqCode,
            contentIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val notification = androidx.core.app.NotificationCompat.Builder(this, channelId)
            .setSmallIcon(com.greenchilli.contestalarm.R.mipmap.ic_launcher_round)
            .setContentTitle("Alarm Snoozed")
            .setContentText("$alarmTitle snoozed until $timeStr")
            .setContentIntent(contentPendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Cancel", cancelPendingIntent)
            .setAutoCancel(false)
            .setOngoing(true)
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(snoozeNotificationId, notification)
    }

    private fun showOnLockScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
    }


    private fun stopAlarmService() {
        val intent = android.content.Intent(this, com.greenchilli.contestalarm.service.AlarmService::class.java)
        stopService(intent)
    }

    override fun onDestroy() {
        try {
            unregisterReceiver(stopReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        // Ensure service stops if activity is closed
        stopAlarmService()
        super.onDestroy()
    }
}

@Composable
fun AlarmScreen(alarmTitle: String, alarmDesc: String, onDismiss: () -> Unit, onSnooze: () -> Unit) {
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }
    
    LaunchedEffect(Unit) {
        while(true) {
            currentTime = System.currentTimeMillis()
            kotlinx.coroutines.delay(1000)
        }
    }

    val timeFormat = remember { java.text.SimpleDateFormat("hh:mm", java.util.Locale.getDefault()) }
    val dateFormat = remember { java.text.SimpleDateFormat("EEEE, MMM dd", java.util.Locale.getDefault()) }
    
    val timeString = timeFormat.format(java.util.Date(currentTime))
    val dateString = dateFormat.format(java.util.Date(currentTime))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f)), // Translucent dark overlay
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Section: Clock and Date
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 48.dp)) {
                Text(
                    text = timeString,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 80.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Light
                    ),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = dateString,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }

            // Middle Section: Note
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = alarmTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = alarmDesc,
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
            }

            // Bottom Section: Actions
            Column(
                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = onSnooze,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D6EFD)),
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(30.dp)
                ) {
                    Text(text = "Snooze for 5 min", color = Color.White, style = MaterialTheme.typography.titleMedium)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    modifier = Modifier.width(200.dp).height(60.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(30.dp)
                ) {
                    Text(text = "Stop", color = Color.Black, style = MaterialTheme.typography.titleLarge)
                }
            }
        }
    }
}
