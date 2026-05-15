package com.ahr.stock.presentation.screen.home

import com.ahr.stock.domain.model.ChartPeriod
import com.ahr.stock.domain.model.IndexHistory
import com.ahr.stock.domain.model.NewsItem
import com.ahr.stock.domain.model.SectorSummary
import com.ahr.stock.domain.model.Stock

enum class MarketTab { GAINERS, LOSERS, TOP_VALUES, TOP_VOLUMES }

sealed interface HomeIntent {
    data object LoadMarket : HomeIntent
    data object Refresh : HomeIntent
    data class SelectStock(val ticker: String) : HomeIntent
    data class SelectSector(val sectorKey: String) : HomeIntent
    data class SelectTab(val tab: MarketTab) : HomeIntent
    data class OnChartDrag(val index: Int?) : HomeIntent
    data class ChangeIndexPeriod(val period: ChartPeriod) : HomeIntent
    data class OpenNewsArticle(val url: String) : HomeIntent
}

data class HomeState(
    val isRefreshing: Boolean = false,
    val indexHistory: IndexHistory = IndexHistory(points = emptyList(), previousClose = null),
    val gainersSection: SectionState<List<Stock>> = SectionState.Loading,
    val losersSection: SectionState<List<Stock>> = SectionState.Idle,
    val topValuesSection: SectionState<List<Stock>> = SectionState.Idle,
    val topVolumesSection: SectionState<List<Stock>> = SectionState.Idle,
    val newsSection: SectionState<List<NewsItem>> = SectionState.Loading,
    val sectorsSection: SectionState<List<SectorSummary>> = SectionState.Loading,
    val selectedTab: MarketTab = MarketTab.GAINERS,
    val selectedIndexPeriod: ChartPeriod = ChartPeriod.ONE_DAY,
    val draggedIndex: Int? = null,
)

sealed interface HomeEffect {
    data class NavigateToDetail(val ticker: String) : HomeEffect
    data class NavigateToSectorStocks(val sectorKey: String) : HomeEffect
    data class OpenUrl(val url: String) : HomeEffect
    data class ShowSnackbar(val message: String) : HomeEffect
}
