package com.greenchilli.contestalarm.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

data class AutoAlarmSettings(
    val defaultOffsetSeconds: Long,
    val div1: Boolean,
    val div2: Boolean,
    val div3: Boolean,
    val div4: Boolean,
    val educational: Boolean,
    val global: Boolean,
    val atcoderBeginner: Boolean,
    val atcoderRegular: Boolean,
    val atcoderGrand: Boolean,
    val codechefStarters: Boolean
)

class UserPreferencesManager(private val context: Context) {

    companion object {
        val DEFAULT_OFFSET_SECONDS = longPreferencesKey("default_offset_seconds")
        val AUTO_ALARM_DIV1 = booleanPreferencesKey("auto_alarm_div1")
        val AUTO_ALARM_DIV2 = booleanPreferencesKey("auto_alarm_div2")
        val AUTO_ALARM_DIV3 = booleanPreferencesKey("auto_alarm_div3")
        val AUTO_ALARM_DIV4 = booleanPreferencesKey("auto_alarm_div4")
        val AUTO_ALARM_EDU = booleanPreferencesKey("auto_alarm_edu")
        val AUTO_ALARM_GLOBAL = booleanPreferencesKey("auto_alarm_global")
        
        val AUTO_ALARM_ATCODER_BEGINNER = booleanPreferencesKey("auto_alarm_atcoder_beginner")
        val AUTO_ALARM_ATCODER_REGULAR = booleanPreferencesKey("auto_alarm_atcoder_regular")
        val AUTO_ALARM_ATCODER_GRAND = booleanPreferencesKey("auto_alarm_atcoder_grand")
        val AUTO_ALARM_CODECHEF_STARTERS = booleanPreferencesKey("auto_alarm_codechef_starters")
    }

    val autoAlarmSettings: Flow<AutoAlarmSettings> = context.dataStore.data.map { preferences ->
        AutoAlarmSettings(
            defaultOffsetSeconds = preferences[DEFAULT_OFFSET_SECONDS] ?: 1800L, // default 30 min
            div1 = preferences[AUTO_ALARM_DIV1] ?: false,
            div2 = preferences[AUTO_ALARM_DIV2] ?: false,
            div3 = preferences[AUTO_ALARM_DIV3] ?: false,
            div4 = preferences[AUTO_ALARM_DIV4] ?: false,
            educational = preferences[AUTO_ALARM_EDU] ?: false,
            global = preferences[AUTO_ALARM_GLOBAL] ?: false,
            atcoderBeginner = preferences[AUTO_ALARM_ATCODER_BEGINNER] ?: false,
            atcoderRegular = preferences[AUTO_ALARM_ATCODER_REGULAR] ?: false,
            atcoderGrand = preferences[AUTO_ALARM_ATCODER_GRAND] ?: false,
            codechefStarters = preferences[AUTO_ALARM_CODECHEF_STARTERS] ?: false
        )
    }

    suspend fun updateDefaultOffset(seconds: Long) {
        context.dataStore.edit { it[DEFAULT_OFFSET_SECONDS] = seconds }
    }

    suspend fun updateAutoAlarm(key: Preferences.Key<Boolean>, enabled: Boolean) {
        context.dataStore.edit { it[key] = enabled }
    }
}
