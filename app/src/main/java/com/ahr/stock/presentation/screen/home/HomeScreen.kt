package com.ahr.stock.presentation.screen.home

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahr.stock.domain.model.IndexHistory
import com.ahr.stock.domain.model.SectorSummary
import com.ahr.stock.domain.model.Stock
import com.ahr.stock.presentation.components.FinancialStepChart
import com.ahr.stock.presentation.components.NewsCard
import com.ahr.stock.presentation.components.PeriodSelector
import com.ahr.stock.presentation.components.SectionCard
import com.ahr.stock.presentation.components.SectorCard
import com.ahr.stock.presentation.components.StockRow
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
                title = { Text(text = "Market", fontWeight = FontWeight.Bold) },
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
            MarketContent(state = state, onIntent = viewModel::onIntent)
        }
    }
}

@Composable
private fun MarketContent(
    state: HomeState,
    onIntent: (HomeIntent) -> Unit,
) {
    val tabs = MarketTab.entries

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
                IndexChartSection(
                    indexHistory = state.indexHistory,
                    draggedIndex = state.draggedIndex,
                    selectedIndexPeriod = state.selectedIndexPeriod,
                    onIntent = onIntent,
                )
            }
        }

        item {
            SectionCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "MOVERS",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 16.dp, top = 12.dp, end = 16.dp),
                )
                val pagerState = rememberPagerState(
                    initialPage = tabs.indexOf(state.selectedTab),
                    pageCount = { tabs.size },
                )

                LaunchedEffect(pagerState) {
                    snapshotFlow { pagerState.currentPage }.collect { page ->
                        onIntent(HomeIntent.SelectTab(tabs[page]))
                    }
                }

                LaunchedEffect(state.selectedTab) {
                    val targetPage = tabs.indexOf(state.selectedTab)
                    if (pagerState.currentPage != targetPage) {
                        pagerState.animateScrollToPage(targetPage)
                    }
                }

                ScrollableTabRow(
                    selectedTabIndex = pagerState.currentPage,
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    edgePadding = 0.dp,
                    divider = {},
                ) {
                    tabs.forEachIndexed { index, tab ->
                        Tab(
                            selected = pagerState.currentPage == index,
                            onClick = { onIntent(HomeIntent.SelectTab(tab)) },
                            text = {
                                Text(
                                    text = when (tab) {
                                        MarketTab.GAINERS -> "Top Gainers"
                                        MarketTab.LOSERS -> "Top Losers"
                                        MarketTab.TOP_VALUES -> "Top Values"
                                        MarketTab.TOP_VOLUMES -> "Top Volumes"
                                    },
                                )
                            },
                        )
                    }
                }

                HorizontalPager(state = pagerState) { page ->
                    val section = when (tabs[page]) {
                        MarketTab.GAINERS -> state.gainersSection
                        MarketTab.LOSERS -> state.losersSection
                        MarketTab.TOP_VALUES -> state.topValuesSection
                        MarketTab.TOP_VOLUMES -> state.topVolumesSection
                    }
                    when (section) {
                        is SectionState.Idle -> Unit
                        is SectionState.Loading -> StockListShimmer()
                        is SectionState.Success -> StockList(
                            stocks = section.data,
                            onStockClick = { onIntent(HomeIntent.SelectStock(it)) },
                        )
                        is SectionState.Error -> SectionError(
                            message = section.message,
                            onRetry = { onIntent(HomeIntent.SelectTab(tabs[page])) },
                        )
                    }
                }
            }
        }

        item {
            SectionCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "SECTORS",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                )
                when (val section = state.sectorsSection) {
                    is SectionState.Loading -> SectorGridShimmer()
                    is SectionState.Success -> SectorGrid(
                        sectors = section.data,
                        onSectorClick = { onIntent(HomeIntent.SelectSector(it.key)) },
                    )
                    is SectionState.Error -> SectionError(
                        message = section.message,
                        onRetry = { onIntent(HomeIntent.LoadMarket) },
                    )
                    else -> Unit
                }
            }
        }

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
                when (val section = state.newsSection) {
                    is SectionState.Loading -> repeat(3) { NewsCardShimmer() }
                    is SectionState.Success -> {
                        section.data.forEach { news ->
                            NewsCard(
                                newsItem = news,
                                onClick = { onIntent(HomeIntent.OpenNewsArticle(it)) },
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    is SectionState.Error -> SectionError(
                        message = section.message,
                        onRetry = { onIntent(HomeIntent.LoadMarket) },
                    )
                    else -> Unit
                }
            }
        }
    }
}

@Composable
private fun IndexChartSection(
    indexHistory: IndexHistory,
    draggedIndex: Int?,
    selectedIndexPeriod: com.ahr.stock.domain.model.ChartPeriod,
    onIntent: (HomeIntent) -> Unit,
) {
    val displayPoint = draggedIndex
        ?.let { indexHistory.points.getOrNull(it) }
        ?: indexHistory.points.lastOrNull()

    val baseClose = indexHistory.points.firstOrNull()?.close ?: 0.0
    val computedChangePercent = if (baseClose != 0.0 && displayPoint != null)
        ((displayPoint.close - baseClose) / baseClose) * 100.0 else 0.0

    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(text = "IHSG", fontWeight = FontWeight.Bold, fontSize = 24.sp)
            Spacer(modifier = Modifier.width(6.dp))
            if (displayPoint != null) {
                val sign = if (computedChangePercent >= 0) "+" else ""
                Text(
                    text = "${"%.2f".format(displayPoint.close)}  $sign${"%.2f".format(computedChangePercent)}%",
                    fontSize = 14.sp,
                    color = if (computedChangePercent >= 0) Color(0xFF00C853) else Color(0xFFE53935),
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        FinancialStepChart(
            data = indexHistory.points,
            xSelector = { it.datetime },
            ySelector = { it.close },
            baselineValue = indexHistory.points.firstOrNull()?.close,
            showYAxisLabels = true,
            onDragIndexChange = { onIntent(HomeIntent.OnChartDrag(it)) },
            modifier = Modifier.fillMaxWidth(),
        )

        PeriodSelector(
            selectedPeriod = selectedIndexPeriod,
            onPeriodSelected = { onIntent(HomeIntent.ChangeIndexPeriod(it)) },
        )
    }
}

@Composable
private fun StockList(
    stocks: List<Stock>,
    onStockClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        stocks.forEachIndexed { index, stock ->
            StockRow(stock = stock, onClick = onStockClick)
            if (index < stocks.lastIndex) {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }
        }
    }
}

@Composable
private fun SectorGrid(
    sectors: List<SectorSummary>,
    onSectorClick: (SectorSummary) -> Unit,
) {
    val columns = 3
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

@Composable
private fun SectionError(
    message: String,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(text = message, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
        Button(onClick = onRetry) {
            Text(text = "Retry")
        }
    }
}
