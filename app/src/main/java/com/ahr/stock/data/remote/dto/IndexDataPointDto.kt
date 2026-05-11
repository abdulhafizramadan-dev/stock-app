package com.ahr.stock.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IndexHistoryResponseDto(
    val period: String,
    val interval: String,
    @SerialName("previous_close") val previousClose: Double? = null,
    val data: List<OhlcvDto>,
    val count: Int,
    val timestamp: String,
    val cached: Boolean,
)

