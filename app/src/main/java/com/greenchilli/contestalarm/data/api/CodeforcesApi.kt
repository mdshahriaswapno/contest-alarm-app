package com.greenchilli.contestalarm.data.api

import retrofit2.http.GET
import retrofit2.http.Query

interface CodeforcesApi {
    @GET("contest.list?gym=false")
    suspend fun getContests(): CodeforcesResponse
}

data class CodeforcesResponse(
    val status: String,
    val result: List<CodeforcesContest>?
)

data class CodeforcesContest(
    val id: Int,
    val name: String,
    val type: String,
    val phase: String,
    val frozen: Boolean,
    val durationSeconds: Int,
    val startTimeSeconds: Long,
    val relativeTimeSeconds: Long?
)
