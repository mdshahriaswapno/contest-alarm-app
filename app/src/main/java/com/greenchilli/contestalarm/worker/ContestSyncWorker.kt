package com.greenchilli.contestalarm.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.greenchilli.contestalarm.data.api.RetrofitClient
import com.greenchilli.contestalarm.data.database.AppDatabase
import com.greenchilli.contestalarm.data.repository.ContestRepository
import com.greenchilli.contestalarm.domain.AlarmScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ContestSyncWorker(
    context: Context,
    userParameters: WorkerParameters
) : CoroutineWorker(context, userParameters) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val app = applicationContext as com.greenchilli.contestalarm.ContestAlarmApp
            val database = app.database
            val repository = app.repository
            val alarmScheduler = app.alarmScheduler

            // 1. Fetch new data
            repository.refreshContests()

            // 2. Refresh alarms
            val upcoming = database.contestDao().getUpcomingContestsSync()
            
            upcoming.forEach { contest ->
                if (contest.isAlarmSet) {
                    val triggerTime = (contest.startTimeSeconds * 1000) - (contest.alarmOffsetSeconds * 1000)
                    alarmScheduler.scheduleAlarm(contest.id, contest.name, triggerTime)
                }
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
