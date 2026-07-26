package com.greenchilli.contestalarm.data.api

import retrofit2.http.GET

interface AtCoderApi {
    @GET("contests.json")
    suspend fun getContests(): List<AtCoderContest>
}

data class AtCoderContest(
    val id: String,
    val start_epoch_second: Long,
    val duration_second: Long,
    val title: String,
    val rate_change: String?
)
