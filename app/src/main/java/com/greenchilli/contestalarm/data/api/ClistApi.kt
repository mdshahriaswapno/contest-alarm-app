package com.greenchilli.contestalarm.data.api

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface ClistApi {
    @GET("contest/")
    suspend fun getContests(
        @Header("Authorization") authorization: String,
        @Query("limit") limit: Int,
        @Query("order_by") orderBy: String, // e.g. "start"
        @Query("start__gt") startGt: String, // e.g. "2023-10-27T00:00:00"
        @Query("resource__in") resourceIn: String? = null // Optional filter
    ): ClistResponse
}

data class ClistResponse(
    val objects: List<ClistContest>
)

data class ClistContest(
    val id: Long,
    val event: String,
    val href: String,
    val start: String, // "2023-10-27T12:00:00"
    val end: String,
    val duration: Long, // seconds
    val resource: String // e.g. "codeforces.com"
)

// data class ClistResource removed as it returns a String
