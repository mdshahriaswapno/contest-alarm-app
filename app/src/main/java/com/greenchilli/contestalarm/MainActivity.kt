package com.greenchilli.contestalarm

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.material3.Scaffold
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Notifications
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.foundation.layout.padding
import com.greenchilli.contestalarm.ui.alarm.CustomAlarmViewModel
import com.greenchilli.contestalarm.ui.alarm.CustomAlarmsScreen
import androidx.navigation.compose.composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.asSharedFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.core.content.ContextCompat
import com.greenchilli.contestalarm.data.api.RetrofitClient
import com.greenchilli.contestalarm.data.database.AppDatabase
import com.greenchilli.contestalarm.data.database.ContestEntity
import com.greenchilli.contestalarm.data.repository.ContestRepository
import com.greenchilli.contestalarm.domain.AlarmScheduler
import com.greenchilli.contestalarm.ui.ContestListScreen
import com.greenchilli.contestalarm.ui.theme.ContestAlarmTheme
import com.greenchilli.contestalarm.worker.ContestSyncWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.greenchilli.contestalarm.ui.ContestUiState
import java.util.concurrent.TimeUnit

class ContestAlarmApp : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val alarmScheduler by lazy { AlarmScheduler(this) }
    val preferencesManager by lazy { com.greenchilli.contestalarm.data.preferences.UserPreferencesManager(this) }
    val repository by lazy { 
        ContestRepository(
            RetrofitClient.instance, 
            database.contestDao(),
            alarmScheduler,
            preferencesManager
        ) 
    }

    override fun onCreate() {
        super.onCreate()
        setupBackgroundSync()
    }

    private fun setupBackgroundSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<ContestSyncWorker>(6, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "ContestSync",
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val app = application as ContestAlarmApp
        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                    return MainViewModel(app.repository, app.alarmScheduler) as T
                }
                if (modelClass.isAssignableFrom(CustomAlarmViewModel::class.java)) {
                    return CustomAlarmViewModel(app.database.customAlarmDao(), app.alarmScheduler) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }



        setContent {
            ContestAlarmTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val viewModel: MainViewModel = viewModel(factory = factory)
                    val customAlarmViewModel: CustomAlarmViewModel = viewModel(factory = factory)
                    val uiState by viewModel.uiState.collectAsState()
                    
                    // Permission Logic
                    val context = androidx.compose.ui.platform.LocalContext.current
                    val launcher = rememberLauncherForActivityResult(
                        ActivityResultContracts.RequestPermission()
                    ) { isGranted: Boolean ->
                        // Handle permission result
                    }

                    LaunchedEffect(Unit) {
                        viewModel.refresh()
                        
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                            val notificationManager = context.getSystemService(android.app.NotificationManager::class.java)
                            if (!notificationManager.canUseFullScreenIntent()) {
                                android.widget.Toast.makeText(
                                    context, 
                                    "Please allow Full Screen Intents for Alarms to work.", 
                                    android.widget.Toast.LENGTH_LONG
                                ).show()
                                val intent = android.content.Intent(android.provider.Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT)
                                intent.data = android.net.Uri.parse("package:${context.packageName}")
                                context.startActivity(intent)
                            }
                        }

                        // Check battery optimization
                        val powerManager = context.getSystemService(android.content.Context.POWER_SERVICE) as android.os.PowerManager
                        if (!powerManager.isIgnoringBatteryOptimizations(context.packageName)) {
                            android.widget.Toast.makeText(
                                context,
                                "Please select 'Unrestricted' battery usage for Alarms to ring accurately on this phone.",
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                            try {
                                val intent = android.content.Intent(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                                intent.data = android.net.Uri.parse("package:${context.packageName}")
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        } else if (!android.provider.Settings.canDrawOverlays(context)) {
                            // Check overlay permission (force background popup)
                            android.widget.Toast.makeText(
                                context,
                                "Please allow 'Display over other apps' to force Alarms to wake up your phone.",
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                            try {
                                val intent = android.content.Intent(
                                    android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    android.net.Uri.parse("package:${context.packageName}")
                                )
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        } else {
                            // Check auto start
                            val prefs = context.getSharedPreferences("contest_alarm_prefs", android.content.Context.MODE_PRIVATE)
                            if (!prefs.getBoolean("auto_start_checked", false)) {
                                prefs.edit().putBoolean("auto_start_checked", true).apply()
                                com.greenchilli.contestalarm.utils.AutoStartHelper.requestAutoStartPermission(context)
                            }
                        }
                    }

                    LaunchedEffect(viewModel) {
                        viewModel.permissionEvent.collect { needsPermission ->
                            if (needsPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                android.widget.Toast.makeText(
                                    context, 
                                    "Please allow 'Alarms & Reminders' for Contest Alarm to function correctly.", 
                                    android.widget.Toast.LENGTH_LONG
                                ).show()
                                val intent = android.content.Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                    data = android.net.Uri.parse("package:${context.packageName}")
                                }
                                context.startActivity(intent)
                            }
                        }
                    }

                    val navController = androidx.navigation.compose.rememberNavController()
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route
                    val preferencesManager = remember { com.greenchilli.contestalarm.data.preferences.UserPreferencesManager(context) }

                    Scaffold(
                        bottomBar = {
                            if (currentRoute == "list" || currentRoute == "custom_alarms") {
                                NavigationBar {
                                    NavigationBarItem(
                                        icon = { Icon(Icons.Default.List, contentDescription = "Contests") },
                                        label = { Text("Contests") },
                                        selected = currentRoute == "list",
                                        onClick = {
                                            navController.navigate("list") {
                                                popUpTo("list") { inclusive = true }
                                            }
                                        }
                                    )
                                    NavigationBarItem(
                                        icon = { Icon(Icons.Default.Notifications, contentDescription = "Custom Alarms") },
                                        label = { Text("Custom Alarms") },
                                        selected = currentRoute == "custom_alarms",
                                        onClick = {
                                            navController.navigate("custom_alarms") {
                                                popUpTo("list")
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    ) { innerPadding ->
                        androidx.navigation.compose.NavHost(
                            navController = navController, 
                            startDestination = "list",
                            modifier = Modifier.padding(innerPadding)
                        ) {
                            composable("list") {
                                ContestListScreen(
                                    uiState = uiState,
                                    onToggleAlarm = { contest, offset -> viewModel.toggleAlarm(contest, offset) },
                                    onRetry = { viewModel.refresh() },
                                    onNavigateToSettings = { navController.navigate("settings") }
                                )
                            }
                            composable("settings") {
                                com.greenchilli.contestalarm.ui.SettingsScreen(
                                    preferencesManager = preferencesManager,
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                            composable("custom_alarms") {
                                CustomAlarmsScreen(viewModel = customAlarmViewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}

class MainViewModel(
    private val repository: ContestRepository,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow<ContestUiState>(ContestUiState.Loading)
    val uiState: StateFlow<ContestUiState> = _uiState.asStateFlow()

    private val _permissionEvent = kotlinx.coroutines.flow.MutableSharedFlow<Boolean>()
    val permissionEvent = _permissionEvent.asSharedFlow()

    init {
        // Observe database changes and update UI state
        viewModelScope.launch {
            repository.upcomingContests.collect { contests ->
                if (contests.isNotEmpty()) {
                    _uiState.value = ContestUiState.Success(contests)
                } else if (_uiState.value !is ContestUiState.Error) {
                    // Only stay in loading if we haven't hit an error yet
                    // If DB is empty, it might be loading or just empty. 
                    // We'll let the refresh() result dictate Error state if needed.
                    // But for now, if DB has data, show it.
                }
            }
        }

        // NEW: Observe Settings and sync alarms retroactively
        viewModelScope.launch {
            repository.observeAndSyncSettings()
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = ContestUiState.Loading
            val result = repository.refreshContests()
            
            if (result.isFailure) {
                // If fallback also failed, show the combined error
                val exception = result.exceptionOrNull()
                val errorMessage = exception?.message ?: "Unknown error"
                
                if (repository.getCurrentContestCount() == 0) {
                     _uiState.value = ContestUiState.Error("All sources failed.\n$errorMessage")
                } else {
                     // We have old data, just show a snackbar or toast (not handled here yet, but at least don't wipe screen)
                }
            } else {
                // Success
                 if (repository.getCurrentContestCount() == 0) {
                     _uiState.value = ContestUiState.Error("No upcoming contests found (List is empty).")
                } else {
                    _uiState.value = ContestUiState.Success(
                        // We could filter or sort here if needed
                        repository.upcomingContests.stateIn(viewModelScope).value
                    )
                }
            }
        }
    }

    fun scheduleTestAlarm() {
        val triggerTime = System.currentTimeMillis() + 5000 // 5 seconds from now
        alarmScheduler.scheduleAlarm("-1", "TEST CONTEST", triggerTime)
    }

    fun toggleAlarm(contest: ContestEntity, offsetSeconds: Long? = null) {
        viewModelScope.launch {
            if (contest.isAlarmSet) {
                // Turn OFF
                repository.toggleAlarm(contest.id, false, 0)
                alarmScheduler.cancelAlarm(contest.id)
            } else {
                // Turn ON
                val offset = offsetSeconds ?: 1800L // Default 30 min if null
                repository.toggleAlarm(contest.id, true, offset)
                
                val triggerTime = if (offset == -1L) {
                    System.currentTimeMillis() + 5000 // Test: Ring in 5 seconds
                } else {
                    (contest.startTimeSeconds * 1000) - (offset * 1000)
                }
                val success = alarmScheduler.scheduleAlarm(contest.id, contest.name, triggerTime)
                if (!success) {
                    // Revert database toggle since scheduling failed
                    repository.toggleAlarm(contest.id, false, 0)
                    _permissionEvent.emit(true)
                }
            }
        }
    }
}
