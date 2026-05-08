package com.ahr.stock.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IndexDataPointDto(
    val datetime: String,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Long,
    val change: Double,
    @SerialName("changePercent") val changePercent: Double,
)

@Serializable
data class IndexHistoryResponseDto(
    val period: String,
    val interval: String,
    val data: List<IndexDataPointDto>,
    val count: Int,
    val timestamp: String,
    val cached: Boolean,
)

