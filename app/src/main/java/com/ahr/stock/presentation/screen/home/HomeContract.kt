package com.ahr.stock.presentation.screen.home

import com.ahr.stock.domain.model.IndexPoint
import com.ahr.stock.domain.model.NewsItem
import com.ahr.stock.domain.model.SectorSummary
import com.ahr.stock.domain.model.Stock

enum class MarketTab { GAINERS, LOSERS, TOP_VALUES, TOP_VOLUMES }

sealed interface HomeIntent {
    data object LoadMarket : HomeIntent
    data object Refresh : HomeIntent
    data class SelectStock(val ticker: String) : HomeIntent
    data class SelectTab(val tab: MarketTab) : HomeIntent
    data class OnChartDrag(val index: Int?) : HomeIntent
    data class OpenNewsArticle(val url: String) : HomeIntent
}

data class HomeState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val gainers: List<Stock> = emptyList(),
    val losers: List<Stock> = emptyList(),
    val topValues: List<Stock> = emptyList(),
    val topVolumes: List<Stock> = emptyList(),
    val indexPoints: List<IndexPoint> = emptyList(),
    val indexPreviousClose: Double? = null,
    val news: List<NewsItem> = emptyList(),
    val sectors: List<SectorSummary> = emptyList(),
    val selectedTab: MarketTab = MarketTab.GAINERS,
    val draggedIndex: Int? = null,
    val error: String? = null,
)

sealed interface HomeEffect {
    data class NavigateToDetail(val ticker: String) : HomeEffect
    data class OpenUrl(val url: String) : HomeEffect
    data class ShowSnackbar(val message: String) : HomeEffect
}
