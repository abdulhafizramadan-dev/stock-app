package com.ahr.stock.domain.model

data class StockDetail(
    val ticker: String,
    val name: String,
    val currency: String,
    val price: Double,
    val previousClose: Double,
    val change: Double,
    val changePercent: Double,
    val volume: Long,
    val averageVolume: Long,
    val marketCap: Long,
    val week52High: Double,
    val week52Low: Double,
    val fiftyDayAverage: Double,
    val twoHundredDayAverage: Double,
    val pe: Double?,
    val forwardPe: Double?,
    val dividendRate: Double?,
    val dividendYield: Double?,
    val beta: Double?,
    val bookValue: Double?,
    val priceToBook: Double?,
    val sector: String?,
    val industry: String?,
)

