package com.ahr.stock.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StockDetailResponseDto(
    val ticker: String,
    @SerialName("raw_info") val rawInfo: RawInfoDto,
    val timestamp: String,
)

@Serializable
data class RawInfoDto(
    val symbol: String,
    val shortName: String? = null,
    val longName: String? = null,
    val currency: String? = null,
    val regularMarketPrice: Double? = null,
    val regularMarketPreviousClose: Double? = null,
    val regularMarketChange: Double? = null,
    val regularMarketChangePercent: Double? = null,
    val regularMarketVolume: Long? = null,
    val averageVolume: Long? = null,
    val marketCap: Long? = null,
    val fiftyTwoWeekHigh: Double? = null,
    val fiftyTwoWeekLow: Double? = null,
    val fiftyDayAverage: Double? = null,
    val twoHundredDayAverage: Double? = null,
    val trailingPE: Double? = null,
    val forwardPE: Double? = null,
    val dividendRate: Double? = null,
    val dividendYield: Double? = null,
    val beta: Double? = null,
    val bookValue: Double? = null,
    val priceToBook: Double? = null,
    val sector: String? = null,
    val industry: String? = null,
)

