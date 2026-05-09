package com.ahr.stock.presentation.screen.home

import com.ahr.stock.domain.model.ChartPeriod
import com.ahr.stock.domain.model.IndexPoint
import com.ahr.stock.domain.model.Stock

enum class MarketTab { GAINERS, LOSERS }

sealed interface HomeIntent {
    data object LoadMarket : HomeIntent
    data object Refresh : HomeIntent
    data class SelectStock(val ticker: String) : HomeIntent
    data class SelectTab(val tab: MarketTab) : HomeIntent
    data class OnChartDrag(val index: Int?) : HomeIntent
}

data class HomeState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val gainers: List<Stock> = emptyList(),
    val losers: List<Stock> = emptyList(),
    val indexPoints: List<IndexPoint> = emptyList(),
    val selectedTab: MarketTab = MarketTab.GAINERS,
    val draggedIndex: Int? = null,
    val error: String? = null,
)

sealed interface HomeEffect {
    data class NavigateToDetail(val ticker: String) : HomeEffect
    data class ShowSnackbar(val message: String) : HomeEffect
}

