package com.greenchilli.contestalarm.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ContestDao {
    @Query("SELECT * FROM contests ORDER BY startTimeSeconds ASC")
    fun getAllContests(): Flow<List<ContestEntity>>

    @Query("SELECT * FROM contests WHERE startTimeSeconds > :currentTimeSeconds ORDER BY startTimeSeconds ASC")
    fun getUpcomingContests(currentTimeSeconds: Long = System.currentTimeMillis() / 1000): Flow<List<ContestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContests(contests: List<ContestEntity>)

    @Query("UPDATE contests SET isAlarmSet = :isSet, alarmOffsetSeconds = :offset WHERE id = :contestId")
    suspend fun updateAlarmStatus(contestId: String, isSet: Boolean, offset: Long)

    @Query("SELECT * FROM contests WHERE id = :contestId")
    suspend fun getContestById(contestId: String): ContestEntity?
    
    @Query("SELECT COUNT(*) FROM contests")
    suspend fun getContestCount(): Int

    @Query("SELECT * FROM contests WHERE isAlarmSet = 1")
    fun getAlarmsSetSync(): List<ContestEntity>
    
    // For Worker
    @Query("SELECT * FROM contests WHERE startTimeSeconds > :currentTimeSeconds ORDER BY startTimeSeconds ASC")
    fun getUpcomingContestsSync(currentTimeSeconds: Long = System.currentTimeMillis() / 1000): List<ContestEntity>
}
