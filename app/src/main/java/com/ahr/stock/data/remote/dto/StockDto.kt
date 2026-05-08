package com.ahr.stock.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StockDto(
    val ticker: String,
    val name: String,
    val price: Double,
    @SerialName("change_percent") val changePercent: Double,
    val volume: Long,
    @SerialName("market_cap") val marketCap: Long,
)

@Serializable
data class StockListResponseDto(
    val stocks: List<StockDto>,
    val count: Int,
    val region: String,
    val timestamp: String,
    val cached: Boolean,
)

