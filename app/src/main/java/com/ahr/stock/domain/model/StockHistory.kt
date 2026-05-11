package com.ahr.stock.domain.model

data class StockHistory(
    val points: List<OhlcvPoint>,
    val previousClose: Double?,
)

