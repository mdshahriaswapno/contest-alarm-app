package com.greenchilli.contestalarm.data.repository

import com.greenchilli.contestalarm.data.api.KontestsApi
import com.greenchilli.contestalarm.data.database.ContestDao
import com.greenchilli.contestalarm.data.database.ContestEntity
import com.greenchilli.contestalarm.data.preferences.UserPreferencesManager
import com.greenchilli.contestalarm.domain.AlarmScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class ContestRepository(
    private val api: KontestsApi,
    private val dao: ContestDao,
    private val alarmScheduler: AlarmScheduler,
    private val preferencesManager: UserPreferencesManager
) {
    val upcomingContests: Flow<List<ContestEntity>> = dao.getUpcomingContests()

    suspend fun refreshContests(): Result<Unit> {
        val errors = mutableListOf<String>()
        var successCount = 0

        // 1. Try Clist (GOLD STANDARD - Primary Source)
        try {
            fetchClistDirectly()
            successCount++
        } catch (e1: Exception) {
            errors.add("Clist: ${e1.message}")
            e1.printStackTrace()
        }

        // If Clist succeeded, we might have everything we need. 
        // But to be safe (and resilient), we can still fetch others OR just rely on Clist.
        // Let's rely on Clist primarily, but if it fails, try the others.
        if (successCount == 0) {
             // Fallbacks
            try {
                fetchCodeforcesDirectly()
                successCount++
            } catch (e2: Exception) {
                errors.add("Codeforces: ${e2.message}")
            }
            
            try {
                fetchCodeChefDirectly()
                successCount++
            } catch (e3: Exception) {
                errors.add("CodeChef: ${e3.message}")
            }

            try {
                fetchLeetCodeDirectly()
                successCount++
            } catch (e4: Exception) {
                errors.add("LeetCode: ${e4.message}")
            }
            
            try {
                fetchAtCoderDirectly()
                successCount++
            } catch (e5: Exception) {
                errors.add("AtCoder: ${e5.message}")
            }
        }

        return if (successCount > 0) {
            Result.success(Unit)
        } else {
            Result.failure(Exception("All sources failed:\n${errors.joinToString("\n")}"))
        }
    }

    private suspend fun fetchClistDirectly() {
        val username = "contestalarm3"
        val apiKey = "da5f44a115af042be3239bcd833a1c3f980c1f8d"
        val authHeader = "ApiKey $username:$apiKey"
        
        val now = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US).apply {
            timeZone = java.util.TimeZone.getTimeZone("UTC")
        }.format(java.util.Date())

        // Fetch upcoming contests
        val response = com.greenchilli.contestalarm.data.api.RetrofitClient.clistInstance.getContests(
            authorization = authHeader,
            limit = 50,
            orderBy = "start",
            startGt = now
        )
        
        val entities = mutableListOf<ContestEntity>()
        for (dto in response.objects) {
            val platform = mapResourceToPlatform(dto.resource) ?: continue // Skip unknown platforms for now
            
            val id = "${platform}_${dto.id}"
            
            // Parse Dates (ISO 8601: 2023-10-27T12:00:00)
            val format = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)
            format.timeZone = java.util.TimeZone.getTimeZone("UTC")
            val startTime = format.parse(dto.start)?.time?.div(1000) ?: 0L

            var isAlarmSet = false
            var alarmOffset = 1800L

            val existing = dao.getContestById(id)
            if (existing != null) {
                isAlarmSet = existing.isAlarmSet
                alarmOffset = existing.alarmOffsetSeconds
            } else {
                // NEW Contest! Evaluate Auto-Alarm rules
                val result = evaluateAutoAlarm(id, dto.event, startTime, platform)
                isAlarmSet = result.first
                alarmOffset = result.second
            }

            entities.add(ContestEntity(
                id = id,
                name = dto.event,
                startTimeSeconds = startTime,
                durationSeconds = dto.duration.toInt(),
                items = null,
                platform = platform,
                url = dto.href,
                status = "BEFORE",
                isAlarmSet = isAlarmSet,
                alarmOffsetSeconds = alarmOffset
            ))
        }
        
        // Insert (Merge strategy would be better, but Replace is what we have)
        if (entities.isNotEmpty()) {
            dao.insertContests(entities)
        }
    }

    private fun mapResourceToPlatform(resource: String): String? {
        return when {
            resource.contains("codeforces") -> "CODEFORCES"
            resource.contains("codechef") -> "CODECHEF"
            resource.contains("leetcode") -> "LEETCODE"
            resource.contains("atcoder") -> "ATCODER"
            else -> null
        }
    }



    private suspend fun fetchAtCoderDirectly() {
        val allContests = com.greenchilli.contestalarm.data.api.RetrofitClient.atCoderInstance.getContests()
        val currentTime = System.currentTimeMillis() / 1000
        
        // Filter for FUTURE contests only
        val futureContests = allContests.filter { it.start_epoch_second > currentTime }
        
        val entities = mutableListOf<ContestEntity>()
        for (dto in futureContests) {
             val id = dto.id
             
             // Preserve alarm settings
             val existing = dao.getContestById(id)
             val isAlarmSet = existing?.isAlarmSet ?: false
             val alarmOffset = existing?.alarmOffsetSeconds ?: 1800
             
             entities.add(ContestEntity(
                id = id,
                name = dto.title,
                startTimeSeconds = dto.start_epoch_second,
                durationSeconds = dto.duration_second.toInt(),
                items = null,
                platform = "ATCODER",
                url = "https://atcoder.jp/contests/${dto.id}",
                status = "BEFORE",
                isAlarmSet = isAlarmSet,
                alarmOffsetSeconds = alarmOffset
            ))
        }
        dao.insertContests(entities)
    }

    private suspend fun fetchLeetCodeDirectly() {
        val query = """
            {
                topTwoContests {
                    title
                    titleSlug
                    startTime
                    duration
                    originStartTime
                    isVirtual
                }
            }
        """.trimIndent()
        
        val response = com.greenchilli.contestalarm.data.api.RetrofitClient.leetCodeInstance.getContests(
            com.greenchilli.contestalarm.data.api.LeetCodeQuery(query)
        )
        
        val contests = response.data?.topTwoContests ?: emptyList()
        val entities = mutableListOf<ContestEntity>()
        
        for (dto in contests) {
             val id = dto.titleSlug
             
             // Preserve alarm settings
             val existing = dao.getContestById(id)
             val isAlarmSet = existing?.isAlarmSet ?: false
             val alarmOffset = existing?.alarmOffsetSeconds ?: 1800
             
             entities.add(ContestEntity(
                id = id,
                name = dto.title,
                startTimeSeconds = dto.startTime,
                durationSeconds = dto.duration.toInt(),
                items = null,
                platform = "LEETCODE",
                url = "https://leetcode.com/contest/${dto.titleSlug}",
                status = "BEFORE", // simplified
                isAlarmSet = isAlarmSet,
                alarmOffsetSeconds = alarmOffset
            ))
        }
        dao.insertContests(entities)
    }

    private suspend fun fetchCodeforcesDirectly() {

        // We use a different API instance for Codeforces
        val response = com.greenchilli.contestalarm.data.api.RetrofitClient.codeforcesInstance.getContests()
        
        if (response.status == "OK" && response.result != null) {
            val entities = mutableListOf<ContestEntity>()
            val validContests = response.result.filter { it.phase == "BEFORE" || it.phase == "CODING" }
            
            for (dto in validContests) {
                 val id = dto.id.toString()
                 
                 var isAlarmSet = false
                 var alarmOffset = 1800L

                 val existing = dao.getContestById(id)
                 if (existing != null) {
                     isAlarmSet = existing.isAlarmSet
                     alarmOffset = existing.alarmOffsetSeconds
                 } else {
                     val result = evaluateAutoAlarm(id, dto.name, dto.startTimeSeconds, "CODEFORCES")
                     isAlarmSet = result.first
                     alarmOffset = result.second
                 }
                 
                 entities.add(ContestEntity(
                    id = id,
                    name = dto.name,
                    startTimeSeconds = dto.startTimeSeconds,
                    durationSeconds = dto.durationSeconds,
                    items = null,
                    platform = "CODEFORCES",
                    url = "https://codeforces.com/contest/${dto.id}",
                    status = dto.phase,
                    isAlarmSet = isAlarmSet,
                    alarmOffsetSeconds = alarmOffset
                ))
            }
            // Use INSERT OR IGNORE strategy or separate Insert for each to avoid overwriting existing data if we want to merge sources
            // But current DAO uses OnConflictStrategy.REPLACE. 
            // If we fetch multiple sources, we should merge lists, not overwrite entire DB each time.
            // For now, let's just insert what we got.
            dao.insertContests(entities)
        } else {
            throw Exception("Codeforces Status: ${response.status}")
        }
    }

    private suspend fun fetchCodeChefDirectly() {
        val response = com.greenchilli.contestalarm.data.api.RetrofitClient.codeChefInstance.getContests()
        
        val allContests = (response.present_contests ?: emptyList()) + (response.future_contests ?: emptyList())
        val entities = mutableListOf<ContestEntity>()
        
        // Date Format: "2023-10-25 14:30:00" assuming India Standard Time usually, but let's parse carefully
        // CodeChef API usually returns times in IST? Or UTC?
        // Let's assume the string format needs parsing.
        
        for (dto in allContests) {
             val id = dto.contest_code
             
             // Preserve alarm settings
             val existing = dao.getContestById(id)
             val isAlarmSet = existing?.isAlarmSet ?: false
             val alarmOffset = existing?.alarmOffsetSeconds ?: 1800
             
             // Parse Date
             var startTime = 0L
             var endTime = 0L
             
             if (dto.contest_start_date_iso != null) {
                 startTime = parseIsoDate(dto.contest_start_date_iso)
                 endTime = parseIsoDate(dto.contest_end_date_iso ?: "")
             } else {
                 startTime = parseCodeChefDate(dto.contest_start_date)
                 endTime = parseCodeChefDate(dto.contest_end_date)
             }

             val duration = if (startTime > 0 && endTime > 0) (endTime - startTime) / 1000 else 0L

             entities.add(ContestEntity(
                id = id,
                name = dto.contest_name,
                startTimeSeconds = startTime / 1000,
                durationSeconds = duration.toInt(),
                items = null,
                platform = "CODECHEF",
                url = "https://www.codechef.com/${dto.contest_code}",
                status = "BEFORE", // simplified
                isAlarmSet = isAlarmSet,
                alarmOffsetSeconds = alarmOffset
            ))
        }
        dao.insertContests(entities)
    }

    private fun parseIsoDate(dateString: String): Long {
        // "2026-02-18T20:00:00+05:30"
        try {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            // The string has timezone offset, but SimpleDateFormat with 'X' or 'Z' might be tricky on older Android
            // Let's try standard ISO parsing if possible or handle manually
            // Actually, for strict ISO 8601 with offset, standard Java Time is best, but we are using SimpleDateFormat
            // Let's rely on the fact that CodeChef returns +05:30.
            
            // Easier approach: Use a pattern that supports timezone if possible, or string manipulation
            // "yyyy-MM-dd'T'HH:mm:ssXXX" works on Android N+
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                val formatIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US)
                return formatIso.parse(dateString)?.time ?: 0L
            } else {
                // Fallback for older devices (strip timezone and assume IST if it matches +05:30)
                // Or just use the custom parser since we know it's likely IST
                val cleanDate = dateString.substring(0, 19)
                val formatSimple = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
                formatSimple.timeZone = TimeZone.getTimeZone("GMT+05:30")
                return formatSimple.parse(cleanDate)?.time ?: 0L
            }
        } catch (e: Exception) {
            return 0L
        }
    }

    private fun parseCodeChefDate(dateString: String): Long {
        // "2023-10-25 14:30:00"
        try {
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            format.timeZone = TimeZone.getTimeZone("Asia/Kolkata") // CodeChef is Indian
            return format.parse(dateString)?.time ?: 0L
        } catch (e: Exception) {
            return 0L
        }
    }

    suspend fun getCurrentContestCount(): Int {
        // Ideally checking DB count directly, but for now checking if flow has value is hard in suspend.
        // We will add a count method to DAO. 
        return dao.getContestCount()
    }

    private fun parseDate(dateString: String): Long {
        val formats = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd HH:mm:ss UTC",
            "yyyy-MM-dd HH:mm:ss"
        )
        for (pattern in formats) {
            try {
                val format = SimpleDateFormat(pattern, Locale.US)
                format.timeZone = TimeZone.getTimeZone("UTC")
                return format.parse(dateString)?.time ?: continue
            } catch (e: Exception) {
                continue
            }
        }
        return 0L
    }

    suspend fun toggleAlarm(contestId: String, isSet: Boolean, offset: Long = 1800) {
        dao.updateAlarmStatus(contestId, isSet, offset)
    }

    private suspend fun evaluateAutoAlarm(
        contestId: String,
        contestName: String,
        startTimeSeconds: Long,
        platform: String
    ): Pair<Boolean, Long> {
        val settings = preferencesManager.autoAlarmSettings.first()
        val lowerName = contestName.lowercase()
        
        var shouldAlarm = false
        
        if (platform == "CODEFORCES") {
            if (settings.div1 && (lowerName.contains("div. 1") || lowerName.contains("div 1"))) shouldAlarm = true
            if (settings.div2 && (lowerName.contains("div. 2") || lowerName.contains("div 2"))) shouldAlarm = true
            if (settings.div3 && (lowerName.contains("div. 3") || lowerName.contains("div 3"))) shouldAlarm = true
            if (settings.div4 && (lowerName.contains("div. 4") || lowerName.contains("div 4"))) shouldAlarm = true
            if (settings.educational && lowerName.contains("educational")) shouldAlarm = true
            if (settings.global && (lowerName.contains("global") || lowerName.contains("hello") || lowerName.contains("good bye"))) shouldAlarm = true
        } else if (platform == "ATCODER") {
            if (settings.atcoderBeginner && (lowerName.contains("beginner") || lowerName.contains("abc"))) shouldAlarm = true
            if (settings.atcoderRegular && (lowerName.contains("regular") || lowerName.contains("arc"))) shouldAlarm = true
            if (settings.atcoderGrand && (lowerName.contains("grand") || lowerName.contains("agc"))) shouldAlarm = true
        } else if (platform == "CODECHEF") {
            if (settings.codechefStarters && lowerName.contains("starter")) shouldAlarm = true
        }

        if (shouldAlarm) {
            val triggerTime = (startTimeSeconds * 1000) - (settings.defaultOffsetSeconds * 1000)
            if (triggerTime > System.currentTimeMillis()) {
                val success = alarmScheduler.scheduleAlarm(contestId, contestName, triggerTime)
                return Pair(success, settings.defaultOffsetSeconds)
            }
        }
        return Pair(false, settings.defaultOffsetSeconds)
    }

    suspend fun observeAndSyncSettings() {
        preferencesManager.autoAlarmSettings.drop(1).collect { settings ->
            withContext(Dispatchers.IO) {
                val contests = dao.getUpcomingContestsSync()
                for (contest in contests) {
                    val platform = contest.platform
                    if (platform != "CODEFORCES" && platform != "ATCODER" && platform != "CODECHEF") continue
                    
                    val lowerName = contest.name.lowercase()
                    var shouldAlarm = false
                    
                    if (platform == "CODEFORCES") {
                        if (settings.div1 && (lowerName.contains("div. 1") || lowerName.contains("div 1"))) shouldAlarm = true
                        if (settings.div2 && (lowerName.contains("div. 2") || lowerName.contains("div 2"))) shouldAlarm = true
                        if (settings.div3 && (lowerName.contains("div. 3") || lowerName.contains("div 3"))) shouldAlarm = true
                        if (settings.div4 && (lowerName.contains("div. 4") || lowerName.contains("div 4"))) shouldAlarm = true
                        if (settings.educational && lowerName.contains("educational")) shouldAlarm = true
                        if (settings.global && (lowerName.contains("global") || lowerName.contains("hello") || lowerName.contains("good bye"))) shouldAlarm = true
                    } else if (platform == "ATCODER") {
                        if (settings.atcoderBeginner && (lowerName.contains("beginner") || lowerName.contains("abc"))) shouldAlarm = true
                        if (settings.atcoderRegular && (lowerName.contains("regular") || lowerName.contains("arc"))) shouldAlarm = true
                        if (settings.atcoderGrand && (lowerName.contains("grand") || lowerName.contains("agc"))) shouldAlarm = true
                    } else if (platform == "CODECHEF") {
                        if (settings.codechefStarters && lowerName.contains("starter")) shouldAlarm = true
                    }

                    if (shouldAlarm && !contest.isAlarmSet) {
                        val triggerTime = (contest.startTimeSeconds * 1000) - (settings.defaultOffsetSeconds * 1000)
                        if (triggerTime > System.currentTimeMillis()) {
                            val success = alarmScheduler.scheduleAlarm(contest.id, contest.name, triggerTime)
                            if (success) {
                                dao.updateAlarmStatus(contest.id, true, settings.defaultOffsetSeconds)
                            }
                        }
                    } else if (!shouldAlarm && contest.isAlarmSet) {
                        alarmScheduler.cancelAlarm(contest.id)
                        dao.updateAlarmStatus(contest.id, false, 0)
                    }
                }
            }
        }
    }
}
