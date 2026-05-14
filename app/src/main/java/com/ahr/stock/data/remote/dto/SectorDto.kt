package com.ahr.stock.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SectorSummaryResponseDto(
    val sectors: List<SectorDto>,
    val count: Int,
    val region: String,
    val timestamp: String,
    val cached: Boolean,
)

@Serializable
data class SectorStocksResponseDto(
    val stocks: List<StockDto>,
    val count: Int,
    val sector: SectorInfoDto,
    val region: String,
    val timestamp: String,
    val cached: Boolean,
)

@Serializable
data class SectorInfoDto(
    val name: String,
    val key: String,
    val displayName: String,
)

@Serializable
data class SectorDto(
    val name: String,
    val key: String,
    val displayName: String,
    @SerialName("change_percent") val changePercent: Double,
    @SerialName("stock_count") val stockCount: Int,
    val direction: String,
)

