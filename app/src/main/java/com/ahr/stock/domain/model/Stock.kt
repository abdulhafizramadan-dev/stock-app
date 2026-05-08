package com.ahr.stock.domain.model

data class Stock(
    val ticker: String,
    val name: String,
    val price: Double,
    val changePercent: Double,
    val volume: Long,
    val marketCap: Long,
)

