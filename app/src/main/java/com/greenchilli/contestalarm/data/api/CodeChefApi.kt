package com.greenchilli.contestalarm.data.api

import retrofit2.http.GET

interface CodeChefApi {
    @GET("list/contests/all?sort_by=START&sorting_order=asc&offset=0&mode=all")
    suspend fun getContests(): CodeChefResponse
}

data class CodeChefResponse(
    val status: String?,
    val future_contests: List<CodeChefContest>?,
    val present_contests: List<CodeChefContest>?
)

data class CodeChefContest(
    val contest_code: String,
    val contest_name: String,
    val contest_start_date: String,
    val contest_end_date: String,
    val contest_start_date_iso: String?, // "2026-02-18T20:00:00+05:30"
    val contest_end_date_iso: String?,
    val contest_duration: String?
)
