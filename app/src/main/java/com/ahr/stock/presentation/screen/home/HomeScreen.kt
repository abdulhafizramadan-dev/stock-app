package com.ahr.stock.presentation.screen.home

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahr.stock.presentation.components.FinancialStepChart
import com.ahr.stock.presentation.components.NewsCard
import com.ahr.stock.presentation.components.SectionCard
import com.ahr.stock.presentation.components.SectorCard
import com.ahr.stock.presentation.components.StockRow
import com.ahr.stock.domain.model.SectorSummary
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToDetail: (String) -> Unit,
    onNavigateToSectorStocks: (sectorKey: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is HomeEffect.NavigateToDetail -> onNavigateToDetail(effect.ticker)
                is HomeEffect.NavigateToSectorStocks -> onNavigateToSectorStocks(effect.sectorKey)
                is HomeEffect.OpenUrl -> {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(effect.url))
                    context.startActivity(intent)
                }
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
    val stocks = when (state.selectedTab) {
        MarketTab.GAINERS -> state.gainers
        MarketTab.LOSERS -> state.losers
        MarketTab.TOP_VALUES -> state.topValues
        MarketTab.TOP_VOLUMES -> state.topVolumes
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            SectionCard(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 12.dp),
            ) {
                IndexChartSection(state = state, onIntent = onIntent)
            }
        }

        item {
            SectionCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "TOP MOVERS",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 16.dp, top = 12.dp, end = 16.dp),
                )
                ScrollableTabRow(
                    selectedTabIndex = tabs.indexOf(state.selectedTab),
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    edgePadding = 0.dp,
                    divider = {},
                ) {
                    tabs.forEach { tab ->
                        Tab(
                            selected = state.selectedTab == tab,
                            onClick = { onIntent(HomeIntent.SelectTab(tab)) },
                            text = {
                                Text(
                                    text = when (tab) {
                                        MarketTab.GAINERS -> "Gainers"
                                        MarketTab.LOSERS -> "Losers"
                                        MarketTab.TOP_VALUES -> "Top Values"
                                        MarketTab.TOP_VOLUMES -> "Top Volumes"
                                    },
                                )
                            },
                        )
                    }
                }

                stocks.forEachIndexed { index, stock ->
                    StockRow(
                        stock = stock,
                        onClick = { onIntent(HomeIntent.SelectStock(it)) },
                    )
                    if (index < stocks.lastIndex) {
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }
            }
        }

        if (state.sectors.isNotEmpty()) {
            item {
                SectorsSummarySection(
                    sectors = state.sectors,
                    onSectorClick = { sector ->
                        onIntent(HomeIntent.SelectSector(sector.key))
                    },
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }

        if (state.news.isNotEmpty()) {
            item {
                SectionCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = "MARKET NEWS",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    )
                    state.news.forEach {news ->
                        NewsCard(
                            newsItem = news,
                            onClick = { onIntent(HomeIntent.OpenNewsArticle(it)) },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
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

    val baseClose = state.indexPreviousClose ?: state.indexPoints.firstOrNull()?.close ?: 0.0
    val computedChangePercent = if (baseClose != 0.0 && displayPoint != null)
        ((displayPoint.close - baseClose) / baseClose) * 100.0 else 0.0

    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = "IHSG",
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
            )

            Spacer(modifier = Modifier.width(6.dp))

            if (displayPoint != null) {
                val sign = if (computedChangePercent >= 0) "+" else ""
                Text(
                    text = "${"%.2f".format(displayPoint.close)}  $sign${"%.2f".format(computedChangePercent)}%",
                    fontSize = 14.sp,
                    color = if (computedChangePercent >= 0)
                        Color(0xFF00C853)
                    else
                        Color(0xFFE53935),
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
private fun SectorsSummarySection(
    sectors: List<SectorSummary>,
    onSectorClick: (SectorSummary) -> Unit,
    modifier: Modifier = Modifier,
) {
    val columns = 3
    SectionCard(modifier = modifier) {
        Text(
            text = "SECTORS",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .padding(bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            sectors.chunked(columns).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    rowItems.forEach { sector ->
                        SectorCard(sector = sector, onClick = onSectorClick, modifier = Modifier.weight(1f))
                    }
                    if (rowItems.size < columns) {
                        Spacer(modifier = Modifier.weight((columns - rowItems.size).toFloat()))
                    }
                }
            }
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
