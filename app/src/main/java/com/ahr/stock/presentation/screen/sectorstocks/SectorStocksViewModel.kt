package com.ahr.stock.presentation.screen.sectorstocks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahr.stock.domain.usecase.sector.GetSectorStocksUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SectorStocksViewModel(
    private val getSectorStocksUseCase: GetSectorStocksUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(SectorStocksState())
    val state: StateFlow<SectorStocksState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<SectorStocksEffect>()
    val effect: SharedFlow<SectorStocksEffect> = _effect.asSharedFlow()

    private var currentSectorKey: String = ""

    fun onIntent(intent: SectorStocksIntent) {
        when (intent) {
            is SectorStocksIntent.Load -> {
                currentSectorKey = intent.sectorKey
                loadStocks()
            }
            is SectorStocksIntent.Refresh -> {
                _state.update { it.copy(isRefreshing = true) }
                loadStocks(isRefresh = true)
            }
            is SectorStocksIntent.SelectStock -> {
                viewModelScope.launch {
                    _effect.emit(SectorStocksEffect.NavigateToDetail(intent.ticker))
                }
            }
            is SectorStocksIntent.UpdateSearchQuery -> {
                _state.update { it.copy(searchQuery = intent.query, filteredStocks = applyFilter(it.stocks, intent.query)) }
            }
        }
    }

    private fun applyFilter(stocks: List<com.ahr.stock.domain.model.Stock>, query: String): List<com.ahr.stock.domain.model.Stock> {
        if (query.isBlank()) return stocks
        val lower = query.trim().lowercase()
        return stocks.filter {
            it.ticker.lowercase().contains(lower) || it.name.lowercase().contains(lower)
        }
    }

    private fun loadStocks(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (!isRefresh) {
                _state.update { it.copy(isLoading = true, error = null) }
            }
            getSectorStocksUseCase(GetSectorStocksUseCase.Params(sectorKey = currentSectorKey))
                .fold(
                    onSuccess = { result ->
                        _state.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                sectorName = result.sectorDisplayName,
                                stocks = result.stocks,
                                filteredStocks = applyFilter(result.stocks, it.searchQuery),
                                error = null,
                            )
                        }
                    },
                    onFailure = { error ->
                        _state.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                error = error.message,
                            )
                        }
                        _effect.emit(SectorStocksEffect.ShowSnackbar(error.message ?: "Unknown error"))
                    },
                )
        }
    }
}

