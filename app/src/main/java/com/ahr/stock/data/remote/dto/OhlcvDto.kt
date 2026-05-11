package com.ahr.stock.data.remote.dto

import com.ahr.stock.utils.convertUtcToIndonesiaTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OhlcvDto(
    @SerialName("Date") val date: String? = null,
    @SerialName("Datetime") val datetime: String? = null,
    @SerialName("Open") val open: Double,
    @SerialName("High") val high: Double,
    @SerialName("Low") val low: Double,
    @SerialName("Close") val close: Double,
    @SerialName("Volume") val volume: Long,
    @SerialName("Dividends") val dividends: Double? = null,
    @SerialName("Stock Splits") val stockSplits: Double? = null,
) {
    val resolvedDate: String
        get() {
            val rawDate = date ?: datetime ?: return ""
            return convertUtcToIndonesiaTime(rawDate)
        }
}

@Serializable
data class OhlcvResponseDto(
    val ticker: String,
    @SerialName("previous_close") val previousClose: Double? = null,
    val history: List<OhlcvDto>,
    val count: Int,
    val period: String,
    val interval: String,
    val cached: Boolean,
    val timestamp: String,
)

