package com.ahr.stock.presentation.screen.sectorstocks

import com.ahr.stock.domain.model.Stock

sealed interface SectorStocksIntent {
    data class Load(val sectorKey: String) : SectorStocksIntent
    data object Refresh : SectorStocksIntent
    data class SelectStock(val ticker: String) : SectorStocksIntent
    data class UpdateSearchQuery(val query: String) : SectorStocksIntent
}

data class SectorStocksState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val sectorName: String = "",
    val stocks: List<Stock> = emptyList(),
    val filteredStocks: List<Stock> = emptyList(),
    val searchQuery: String = "",
    val error: String? = null,
)

sealed interface SectorStocksEffect {
    data class NavigateToDetail(val ticker: String) : SectorStocksEffect
    data class ShowSnackbar(val message: String) : SectorStocksEffect
}

