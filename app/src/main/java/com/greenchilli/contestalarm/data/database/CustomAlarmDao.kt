package com.greenchilli.contestalarm.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomAlarmDao {
    @Query("SELECT * FROM custom_alarms ORDER BY triggerTimeMillis ASC")
    fun getAllCustomAlarms(): Flow<List<CustomAlarmEntity>>

    @Query("SELECT * FROM custom_alarms ORDER BY triggerTimeMillis ASC")
    fun getAllCustomAlarmsSync(): List<CustomAlarmEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomAlarm(alarm: CustomAlarmEntity)

    @Query("UPDATE custom_alarms SET isEnabled = :isEnabled WHERE id = :id")
    suspend fun updateAlarmStatus(id: String, isEnabled: Boolean)

    @Query("DELETE FROM custom_alarms WHERE id = :id")
    suspend fun deleteCustomAlarm(id: String)

    @Query("DELETE FROM custom_alarms WHERE triggerTimeMillis < :thresholdMillis")
    suspend fun deleteAlarmsOlderThan(thresholdMillis: Long)
}
