package com.ahr.stock.domain.model

data class IndexPoint(
    val datetime: String,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Long,
    val change: Double,
    val changePercent: Double,
)

