package com.ahr.stock.domain.model

data class IndexHistory(
    val points: List<IndexPoint>,
    val previousClose: Double?,
)

