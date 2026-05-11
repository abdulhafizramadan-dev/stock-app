package com.ahr.stock.domain.model

data class SectorSummary(
    val name: String,
    val key: String,
    val displayName: String,
    val changePercent: Double,
    val stockCount: Int,
    val direction: String,
)

