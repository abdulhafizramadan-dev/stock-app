package com.ahr.stock.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OhlcvDto(
    @SerialName("Date") val date: String,
    @SerialName("Open") val open: Double,
    @SerialName("High") val high: Double,
    @SerialName("Low") val low: Double,
    @SerialName("Close") val close: Double,
    @SerialName("Volume") val volume: Long,
)

@Serializable
data class OhlcvResponseDto(
    val ticker: String,
    val history: List<OhlcvDto>,
    val count: Int,
    val period: String,
    val interval: String,
    val cached: Boolean,
    val timestamp: String,
)

