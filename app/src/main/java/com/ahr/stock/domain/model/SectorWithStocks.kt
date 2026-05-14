package com.ahr.stock.domain.model

data class SectorWithStocks(
    val sectorDisplayName: String,
    val stocks: List<Stock>,
)

