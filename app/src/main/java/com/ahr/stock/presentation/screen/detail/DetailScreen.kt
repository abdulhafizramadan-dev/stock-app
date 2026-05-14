package com.ahr.stock.presentation.screen.detail

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import com.ahr.stock.domain.model.OhlcvPoint
import com.ahr.stock.domain.model.StockDetail
import com.ahr.stock.presentation.components.FinancialStepChart
import com.ahr.stock.presentation.components.NewsCard
import com.ahr.stock.presentation.components.PeriodSelector
import com.ahr.stock.presentation.components.PriceChip
import com.ahr.stock.presentation.components.SectionCard
import org.koin.androidx.compose.koinViewModel

private val BullishGreen = Color(0xFF00C853)
private val BearishRed = Color(0xFFE53935)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    ticker: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DetailViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(ticker) {
        viewModel.onIntent(DetailIntent.LoadDetail(ticker))
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is DetailEffect.OpenUrl -> {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(effect.url))
                    context.startActivity(intent)
                }
                is DetailEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = ticker, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { viewModel.onIntent(DetailIntent.Refresh) },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when {
                state.isLoading -> LoadingContent()
                state.error != null && state.detail == null -> ErrorContent(
                    message = state.error!!,
                    onRetry = { viewModel.onIntent(DetailIntent.LoadDetail(ticker)) },
                )
                else -> DetailContent(state = state, onIntent = viewModel::onIntent)
            }
        }
    }
}

@Composable
private fun DetailContent(
    state: DetailState,
    onIntent: (DetailIntent) -> Unit,
) {
    val baselineClose = state.previousClose ?: state.history.firstOrNull()?.close
    val displayPoint = state.draggedIndex?.let { state.history.getOrNull(it) }
        ?: state.history.lastOrNull()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        state.detail?.let { detail ->
            item {
                SectionCard(modifier = Modifier.padding(horizontal = 16.dp).padding(top = 12.dp)) {
                    PriceHeaderSection(
                        detail = detail,
                        displayPoint = displayPoint,
                        baselineClose = baselineClose,
                    )
                    ChartSection(state = state, onIntent = onIntent)
                    PeriodSelector(
                        selectedPeriod = state.selectedPeriod,
                        onPeriodSelected = { onIntent(DetailIntent.ChangePeriod(it)) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
            }

            item {
                SectionCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                    StockMetaSection(detail = detail)
                }
            }
        }

        if (state.news.isNotEmpty()) {
            item {
                SectionCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                    NewsSectionHeader()
                    state.news.forEachIndexed { index, news ->
                        NewsCard(
                            newsItem = news,
                            onClick = { onIntent(DetailIntent.OpenNewsArticle(it)) },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        )
                        if (index < state.news.lastIndex) {
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun NewsSectionHeader() {
    Text(
        text = "NEWS",
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
    )
}

@Composable
private fun PriceHeaderSection(
    detail: StockDetail,
    displayPoint: OhlcvPoint?,
    baselineClose: Double?,
) {
    val displayPrice = displayPoint?.close ?: 0.0
    val displayChange = if (displayPoint != null && baselineClose != null && baselineClose != 0.0)
        displayPoint.close - baselineClose else 0.0
    val displayChangePercent = if (baselineClose != null && baselineClose != 0.0)
        (displayChange / baselineClose) * 100.0 else 0.0

    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
        Text(
            text = detail.name,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "${"%.0f".format(displayPrice)} ${detail.currency}",
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        PriceChip(changePercent = displayChangePercent, changeValue = displayChange)
    }
}

@Composable
private fun ChartSection(
    state: DetailState,
    onIntent: (DetailIntent) -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        if (state.history.size >= 2) {
            FinancialStepChart(
                data = state.history,
                xSelector = { it.date },
                ySelector = { it.close },
                baselineValue = state.history.firstOrNull()?.close,
                showYAxisLabels = true,
                onDragIndexChange = { onIntent(DetailIntent.OnChartDrag(it)) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun StockMetaSection(detail: StockDetail) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        Text(
            text = "DETAILS",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.sp,
        )
        Spacer(modifier = Modifier.height(12.dp))
        MetaRow(label = "Market Cap", value = formatLargeNumber(detail.marketCap))
        MetaRow(label = "Volume", value = formatLargeNumber(detail.volume))
        MetaRow(label = "52W High", value = "%.2f".format(detail.week52High))
        MetaRow(label = "52W Low", value = "%.2f".format(detail.week52Low))
        detail.pe?.let { MetaRow(label = "P/E Ratio", value = "%.2f".format(it)) }
        detail.dividendYield?.let {
            MetaRow(label = "Dividend Yield", value = "${"%.2f".format(it * 100)}%")
        }
        detail.sector?.let { MetaRow(label = "Sector", value = it) }
        detail.industry?.let { MetaRow(label = "Industry", value = it) }
    }
}

@Composable
private fun MetaRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        Text(text = value, fontWeight = FontWeight.Medium, fontSize = 13.sp)
    }
}

@Composable
private fun LoadingContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
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

private fun formatLargeNumber(value: Long): String = when {
    value >= 1_000_000_000_000 -> "${"%.2f".format(value / 1_000_000_000_000.0)}T"
    value >= 1_000_000_000 -> "${"%.2f".format(value / 1_000_000_000.0)}B"
    value >= 1_000_000 -> "${"%.2f".format(value / 1_000_000.0)}M"
    value >= 1_000 -> "${"%.2f".format(value / 1_000.0)}K"
    else -> value.toString()
}
