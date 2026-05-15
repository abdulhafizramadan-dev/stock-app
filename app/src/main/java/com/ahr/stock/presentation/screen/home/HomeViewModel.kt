package com.ahr.stock.presentation.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahr.stock.domain.model.ChartPeriod
import com.ahr.stock.domain.usecase.index.GetIndexHistoryUseCase
import com.ahr.stock.domain.usecase.news.GetHighlightedNewsUseCase
import com.ahr.stock.domain.usecase.sector.GetSectorsSummaryUseCase
import com.ahr.stock.domain.usecase.stock.GetGainersUseCase
import com.ahr.stock.domain.usecase.stock.GetLosersUseCase
import com.ahr.stock.domain.usecase.stock.GetTopValuesUseCase
import com.ahr.stock.domain.usecase.stock.GetTopVolumesUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getGainers: GetGainersUseCase,
    private val getLosers: GetLosersUseCase,
    private val getTopValues: GetTopValuesUseCase,
    private val getTopVolumes: GetTopVolumesUseCase,
    private val getIndexHistory: GetIndexHistoryUseCase,
    private val getHighlightedNews: GetHighlightedNewsUseCase,
    private val getSectorsSummary: GetSectorsSummaryUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<HomeEffect>()
    val effect: SharedFlow<HomeEffect> = _effect.asSharedFlow()

    init {
        onIntent(HomeIntent.LoadMarket)
    }

    fun onIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.LoadMarket -> loadMarket(isRefresh = false)
            is HomeIntent.Refresh -> loadMarket(isRefresh = true)
            is HomeIntent.SelectStock -> navigateToDetail(intent.ticker)
            is HomeIntent.SelectSector -> navigateToSectorStocks(intent.sectorKey)
            is HomeIntent.SelectTab -> {
                _state.update { it.copy(selectedTab = intent.tab) }
                loadTabIfNeeded(intent.tab)
            }
            is HomeIntent.OnChartDrag -> _state.update { it.copy(draggedIndex = intent.index) }
            is HomeIntent.ChangeIndexPeriod -> {
                _state.update {
                    it.copy(
                        selectedIndexPeriod = intent.period,
                        draggedIndex = null,
                    )
                }
                viewModelScope.launch { loadIndex(intent.period) }
            }
            is HomeIntent.OpenNewsArticle -> openUrl(intent.url)
        }
    }

    private fun loadMarket(isRefresh: Boolean) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isRefreshing = isRefresh,
                    gainersSection = SectionState.Loading,
                    newsSection = SectionState.Loading,
                    sectorsSection = SectionState.Loading,
                    losersSection = SectionState.Idle,
                    topValuesSection = SectionState.Idle,
                    topVolumesSection = SectionState.Idle,
                )
            }

            val jobs = listOf(
                launch { loadIndex(_state.value.selectedIndexPeriod) },
                launch { loadGainers() },
                launch { loadNews() },
                launch { loadSectors() },
            )
            jobs.joinAll()

            _state.update { it.copy(isRefreshing = false) }
        }
    }

    private suspend fun loadIndex(period: ChartPeriod) {
        getIndexHistory(
            GetIndexHistoryUseCase.Params(
                symbol = "^JKSE",
                period = period.period,
                interval = period.interval,
            )
        ).fold(
            onSuccess = { result ->
                _state.update { it.copy(indexHistory = result) }
            },
            onFailure = { error ->
                _effect.emit(HomeEffect.ShowSnackbar(error.message ?: "Failed to load chart"))
            },
        )
    }

    private suspend fun loadGainers() {
        getGainers(GetGainersUseCase.Params(limit = 5)).fold(
            onSuccess = { data ->
                _state.update { it.copy(gainersSection = SectionState.Success(data)) }
            },
            onFailure = { error ->
                _state.update {
                    it.copy(gainersSection = SectionState.Error(error.message ?: "Failed to load gainers"))
                }
            },
        )
    }

    private suspend fun loadLosers() {
        _state.update { it.copy(losersSection = SectionState.Loading) }
        getLosers(GetLosersUseCase.Params(limit = 5)).fold(
            onSuccess = { data ->
                _state.update { it.copy(losersSection = SectionState.Success(data)) }
            },
            onFailure = { error ->
                _state.update {
                    it.copy(losersSection = SectionState.Error(error.message ?: "Failed to load losers"))
                }
            },
        )
    }

    private suspend fun loadTopValues() {
        _state.update { it.copy(topValuesSection = SectionState.Loading) }
        getTopValues(GetTopValuesUseCase.Params(limit = 5)).fold(
            onSuccess = { data ->
                _state.update { it.copy(topValuesSection = SectionState.Success(data)) }
            },
            onFailure = { error ->
                _state.update {
                    it.copy(topValuesSection = SectionState.Error(error.message ?: "Failed to load top values"))
                }
            },
        )
    }

    private suspend fun loadTopVolumes() {
        _state.update { it.copy(topVolumesSection = SectionState.Loading) }
        getTopVolumes(GetTopVolumesUseCase.Params(limit = 5)).fold(
            onSuccess = { data ->
                _state.update { it.copy(topVolumesSection = SectionState.Success(data)) }
            },
            onFailure = { error ->
                _state.update {
                    it.copy(topVolumesSection = SectionState.Error(error.message ?: "Failed to load top volumes"))
                }
            },
        )
    }

    private suspend fun loadNews() {
        getHighlightedNews(GetHighlightedNewsUseCase.Params(count = 5)).fold(
            onSuccess = { data ->
                _state.update { it.copy(newsSection = SectionState.Success(data)) }
            },
            onFailure = { error ->
                _state.update {
                    it.copy(newsSection = SectionState.Error(error.message ?: "Failed to load news"))
                }
            },
        )
    }

    private suspend fun loadSectors() {
        getSectorsSummary(GetSectorsSummaryUseCase.Params()).fold(
            onSuccess = { data ->
                _state.update { it.copy(sectorsSection = SectionState.Success(data)) }
            },
            onFailure = { error ->
                _state.update {
                    it.copy(sectorsSection = SectionState.Error(error.message ?: "Failed to load sectors"))
                }
            },
        )
    }

    private fun loadTabIfNeeded(tab: MarketTab) {
        when (tab) {
            MarketTab.LOSERS -> {
                if (_state.value.losersSection is SectionState.Idle) {
                    viewModelScope.launch { loadLosers() }
                }
            }
            MarketTab.TOP_VALUES -> {
                if (_state.value.topValuesSection is SectionState.Idle) {
                    viewModelScope.launch { loadTopValues() }
                }
            }
            MarketTab.TOP_VOLUMES -> {
                if (_state.value.topVolumesSection is SectionState.Idle) {
                    viewModelScope.launch { loadTopVolumes() }
                }
            }
            MarketTab.GAINERS -> Unit
        }
    }

    private fun navigateToDetail(ticker: String) {
        viewModelScope.launch {
            _effect.emit(HomeEffect.NavigateToDetail(ticker))
        }
    }

    private fun navigateToSectorStocks(sectorKey: String) {
        viewModelScope.launch {
            _effect.emit(HomeEffect.NavigateToSectorStocks(sectorKey))
        }
    }

    private fun openUrl(url: String) {
        viewModelScope.launch {
            _effect.emit(HomeEffect.OpenUrl(url))
        }
    }
}
