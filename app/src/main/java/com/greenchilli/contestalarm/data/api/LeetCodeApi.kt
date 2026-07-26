package com.greenchilli.contestalarm.data.api

import retrofit2.http.Body
import retrofit2.http.POST

interface LeetCodeApi {
    @POST("graphql")
    suspend fun getContests(@Body query: LeetCodeQuery): LeetCodeResponse
}

data class LeetCodeQuery(val query: String)

data class LeetCodeResponse(val data: LeetCodeData?)

data class LeetCodeData(val topTwoContests: List<LeetCodeContest>?)

data class LeetCodeContest(
    val title: String,
    val titleSlug: String,
    val startTime: Long, // Seconds
    val duration: Long, // Seconds
    val originStartTime: Long,
    val isVirtual: Boolean
)
