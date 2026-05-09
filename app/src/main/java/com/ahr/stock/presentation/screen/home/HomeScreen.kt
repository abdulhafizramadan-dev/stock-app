package com.ahr.stock.presentation.screen.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahr.stock.presentation.components.FinancialStepChart
import com.ahr.stock.presentation.components.StockRow
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToDetail: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is HomeEffect.NavigateToDetail -> onNavigateToDetail(effect.ticker)
                is HomeEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Market", fontWeight = FontWeight.Bold)
                },
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { viewModel.onIntent(HomeIntent.Refresh) },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when {
                state.isLoading -> LoadingContent()
                state.error != null && state.gainers.isEmpty() -> ErrorContent(
                    message = state.error!!,
                    onRetry = { viewModel.onIntent(HomeIntent.LoadMarket) },
                )

                else -> MarketContent(state = state, onIntent = viewModel::onIntent)
            }
        }
    }
}

@Composable
private fun MarketContent(
    state: HomeState,
    onIntent: (HomeIntent) -> Unit,
) {
    val tabs = MarketTab.entries
    val stocks = if (state.selectedTab == MarketTab.GAINERS) state.gainers else state.losers

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp),
    ) {
        item {
            IndexChartSection(state = state, onIntent = onIntent)
        }

        item {
            TabRow(selectedTabIndex = tabs.indexOf(state.selectedTab)) {
                tabs.forEach { tab ->
                    Tab(
                        selected = state.selectedTab == tab,
                        onClick = { onIntent(HomeIntent.SelectTab(tab)) },
                        text = {
                            Text(
                                text = when (tab) {
                                    MarketTab.GAINERS -> "Top Gainers"
                                    MarketTab.LOSERS -> "Top Losers"
                                },
                            )
                        },
                    )
                }
            }
        }

        items(stocks, key = { it.ticker }) { stock ->
            StockRow(
                stock = stock,
                onClick = { onIntent(HomeIntent.SelectStock(it)) },
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        }
    }
}

@Composable
private fun IndexChartSection(
    state: HomeState,
    onIntent: (HomeIntent) -> Unit,
) {
    val displayPoint = state.draggedIndex
        ?.let { state.indexPoints.getOrNull(it) }
        ?: state.indexPoints.lastOrNull()

    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "IHSG",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
            )

            Spacer(modifier = Modifier.width(4.dp))

            if (displayPoint != null) {
                val sign = if (displayPoint.changePercent >= 0) "+" else ""
                Text(
                    text = "${"%.2f".format(displayPoint.close)}  $sign${"%.2f".format(displayPoint.changePercent)}%",
                    fontSize = 12.sp,
                    color = if (displayPoint.changePercent >= 0)
                        androidx.compose.ui.graphics.Color(0xFF00C853)
                    else
                        androidx.compose.ui.graphics.Color(0xFFE53935),
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (state.indexPoints.size >= 2) {
            FinancialStepChart(
                data = state.indexPoints,
                xSelector = { it.datetime },
                ySelector = { it.close },
                baselineValue = state.indexPoints.firstOrNull()?.close,
                showYAxisLabels = true,
                onDragIndexChange = { onIntent(HomeIntent.OnChartDrag(it)) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        androidx.compose.material3.CircularProgressIndicator()
    }
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = message, color = MaterialTheme.colorScheme.error)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text(text = "Retry")
        }
    }
}

