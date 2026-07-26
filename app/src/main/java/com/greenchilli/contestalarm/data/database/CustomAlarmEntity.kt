package com.greenchilli.contestalarm.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_alarms")
data class CustomAlarmEntity(
    @PrimaryKey val id: String, // e.g. UUID
    val note: String,
    val triggerTimeMillis: Long,
    val isEnabled: Boolean = true
)
