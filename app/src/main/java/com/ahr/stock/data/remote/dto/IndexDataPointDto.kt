package com.ahr.stock.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class IndexHistoryResponseDto(
    val period: String,
    val interval: String,
    val data: List<OhlcvDto>,
    val count: Int,
    val timestamp: String,
    val cached: Boolean,
)

