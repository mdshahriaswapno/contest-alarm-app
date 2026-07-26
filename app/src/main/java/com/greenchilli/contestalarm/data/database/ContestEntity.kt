package com.greenchilli.contestalarm.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contests")
data class ContestEntity(
    @PrimaryKey val id: String, // Generated from "Name + StartTime"
    val name: String,
    val startTimeSeconds: Long,
    val durationSeconds: Int,
    val items: String? = null, // JSON or list of related items if any
    val platform: String, // "Codeforces", "AtCoder", "CodeChef"
    val url: String,
    val status: String, // "BEFORE", "CODING"
    val isAlarmSet: Boolean = false,
    val alarmOffsetSeconds: Long = 1800 // Default 30 mins (30 * 60)
)
