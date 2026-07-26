package com.greenchilli.contestalarm.data.api

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET

interface KontestsApi {
    @GET("all")
    suspend fun getAllContests(): List<KontestDTO>
}

data class KontestDTO(
    val name: String,
    val url: String,
    @SerializedName("start_time") val startTime: String,
    @SerializedName("end_time") val endTime: String,
    val duration: String,
    val site: String?,
    val status: String
)
