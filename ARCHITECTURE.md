# ARCHITECTURE.md — StockBit Mini Android App

> **Source of truth** for all architectural decisions, layer responsibilities, and integration patterns.  
> Last updated: May 2026

---

## 1. Overview & Tech Stack

| Concern | Choice |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose |
| Architecture | MVI + Clean Architecture |
| Networking | Ktor Client (Android engine) |
| Dependency Injection | Koin |
| Image Loading | Coil |
| Charting | Custom `FinancialStepChart` composable (Canvas-based) |
| Navigation | Jetpack Compose Navigation |
| Serialization | `kotlinx.serialization` |

---

## 2. Project Structure

```
app/src/main/java/com/ahr/stock/
├── data/
│   ├── remote/
│   │   ├── api/
│   │   │   ├── StockApiService.kt
│   │   │   ├── IndexApiService.kt
│   │   │   ├── SectorApiService.kt
│   │   │   └── NewsApiService.kt
│   │   ├── dto/
│   │   │   ├── StockDto.kt
│   │   │   ├── StockDetailDto.kt
│   │   │   ├── OhlcvDto.kt
│   │   │   ├── NewsItemDto.kt
│   │   │   ├── IndexDataPointDto.kt
│   │   │   └── SectorDto.kt             ← SectorSummaryResponseDto, SectorStocksResponseDto, SectorInfoDto
│   └── mapper/
│       │   ├── Mapper.kt               ← base interface contract
│       │   ├── StockMapper.kt
│       │   ├── StockDetailMapper.kt
│       │   ├── OhlcvMapper.kt
│       │   ├── NewsItemMapper.kt
│       │   ├── IndexPointMapper.kt
│       │   └── SectorSummaryMapper.kt
│   └── repository/
│       ├── StockRepositoryImpl.kt
│       ├── IndexRepositoryImpl.kt
│       ├── NewsRepositoryImpl.kt
│       └── SectorRepositoryImpl.kt
├── domain/
│   ├── model/
│   │   ├── Stock.kt
│   │   ├── StockDetail.kt
│   │   ├── OhlcvPoint.kt
│   │   ├── NewsItem.kt
│   │   ├── IndexPoint.kt
│   │   ├── SectorSummary.kt
│   │   ├── SectorWithStocks.kt         ← sectorDisplayName + stocks list
│   │   ├── StockHistory.kt
│   │   ├── IndexHistory.kt
│   │   └── ChartPeriod.kt
│   ├── repository/
│   │   ├── StockRepository.kt
│   │   ├── IndexRepository.kt
│   │   ├── NewsRepository.kt
│   │   └── SectorRepository.kt
│   └── usecase/
│       ├── UseCase.kt                  ← base interface contract
│       ├── stock/
│       │   ├── GetGainersUseCase.kt
│       │   ├── GetLosersUseCase.kt
│       │   ├── GetTopValuesUseCase.kt
│       │   ├── GetTopVolumesUseCase.kt
│       │   ├── GetStockDetailUseCase.kt
│       │   ├── GetStockHistoryUseCase.kt
│       │   ├── GetStockNewsUseCase.kt
│       ├── index/
│       │   └── GetIndexHistoryUseCase.kt
│       ├── news/
│       │   └── GetHighlightedNewsUseCase.kt
│       └── sector/
│           ├── GetSectorsSummaryUseCase.kt
│           └── GetSectorStocksUseCase.kt
├── presentation/
│   ├── navigation/
│   │   ├── NavGraph.kt
│   │   └── Screen.kt
│   ├── components/
│   │   ├── StockRow.kt
│   │   ├── NewsCard.kt
│   │   ├── PriceChip.kt
│   │   ├── PeriodSelector.kt
│   │   ├── SectionCard.kt
│   │   ├── SectorCard.kt
│   │   └── FinancialStepChart.kt
│   └── screen/
│       ├── home/
│       │   ├── HomeScreen.kt
│       │   ├── HomeViewModel.kt
│       │   └── HomeContract.kt
│       ├── detail/
│       │   ├── DetailScreen.kt
│       │   ├── DetailViewModel.kt
│       │   └── DetailContract.kt
│       └── sectorstocks/
│           ├── SectorStocksScreen.kt
│           ├── SectorStocksViewModel.kt
│           └── SectorStocksContract.kt
├── di/
│   ├── NetworkModule.kt
│   ├── DataModule.kt
│   ├── DomainModule.kt
│   └── PresentationModule.kt
├── utils/
│   └── DateUtils.kt
└── App.kt
```

---

## 3. Layer Responsibilities

### Data Layer
- Owns all network communication via Ktor.
- `StockApiService` / `IndexApiService` — Ktor extension functions returning `Result<T>`.
- All response bodies are deserialized into `@Serializable` DTOs under `remote/dto/`.
- Mappers in `remote/mapper/` convert DTOs to domain models — the only place DTOs are referenced outside the data layer.
- `StockRepositoryImpl` / `IndexRepositoryImpl` implement the domain repository interfaces, call API services, and map results.
- **No local caching in this phase.** Every call goes to the network.

### Domain Layer
- Pure Kotlin — no Android or framework dependencies.
- Defines repository interfaces (`StockRepository`, `IndexRepository`) that the data layer implements.
- Contains all domain models (`Stock`, `StockDetail`, `OhlcvPoint`, `NewsItem`, `IndexPoint`).
- Each use case is a single-responsibility class with an `invoke` operator that delegates to a repository.

### Presentation Layer
- Implements MVI via `Intent → ViewModel → State + Effect`.
- ViewModels hold `StateFlow<State>` and `SharedFlow<Effect>`.
- Screens collect state and effects; all user actions are dispatched as `Intent` objects.
- Shared composables live in `components/`; screen-specific composables stay within their screen package.
- Navigation is handled centrally in `NavGraph.kt`.

---

## 4. MVI Pattern

```
User Action
    ↓
Intent (sealed interface)
    ↓
ViewModel.onIntent()
    ↓
UseCase → Repository → API
    ↓
Result<T>
    ↓
reduce(state) → StateFlow<State>   (triggers recomposition)
    ↓ (side effects)
SharedFlow<Effect>                 (one-shot: navigation, snackbar, open URL)
```

### HomeContract

```kotlin
sealed interface HomeIntent {
    data object LoadMarket : HomeIntent
    data object Refresh : HomeIntent
    data class SelectStock(val ticker: String) : HomeIntent
    data class SelectSector(val sectorKey: String) : HomeIntent
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
    data class NavigateToSectorStocks(val sectorKey: String) : HomeEffect
    data class OpenUrl(val url: String) : HomeEffect
    data class ShowSnackbar(val message: String) : HomeEffect
}
```

### SectorStocksContract

```kotlin
sealed interface SectorStocksIntent {
    data class Load(val sectorKey: String) : SectorStocksIntent
    data object Refresh : SectorStocksIntent
    data class SelectStock(val ticker: String) : SectorStocksIntent
}

data class SectorStocksState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val sectorName: String = "",        // populated from API response sector.displayName
    val stocks: List<Stock> = emptyList(),
    val error: String? = null,
)

sealed interface SectorStocksEffect {
    data class NavigateToDetail(val ticker: String) : SectorStocksEffect
    data class ShowSnackbar(val message: String) : SectorStocksEffect
}
```

### DetailContract

```kotlin
sealed interface DetailIntent {
    data class LoadDetail(val ticker: String) : DetailIntent
    data object Refresh : DetailIntent
    data class ChangePeriod(val period: ChartPeriod) : DetailIntent
    data class OnChartDrag(val index: Int?) : DetailIntent
    data class OpenNewsArticle(val url: String) : DetailIntent
}

data class DetailState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val detail: StockDetail? = null,
    val history: List<OhlcvPoint> = emptyList(),
    val news: List<NewsItem> = emptyList(),
    val selectedPeriod: ChartPeriod = ChartPeriod.ONE_MONTH,
    val draggedIndex: Int? = null,
    val error: String? = null
)

sealed interface DetailEffect {
    data class OpenUrl(val url: String) : DetailEffect
    data class ShowSnackbar(val message: String) : DetailEffect
}
```

---

## 5. FinancialStepChart Integration

`FinancialStepChart` is a Canvas-based composable. **Do not add Vico or any third-party charting library.**

### Signature

```kotlin
@Composable
fun <T> FinancialStepChart(
    data: List<T>,
    xSelector: (T) -> String,
    ySelector: (T) -> Double,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 2.dp,
    backgroundColor: Color = Color.Transparent,
    bullishColor: Color = Color(0xFF00C853),
    bearishColor: Color = Color(0xFFE53935),
    gridColor: Color = Color(0xFFBDBBBB),
    baselineValue: Double? = null,
    labelFontSize: TextUnit = 11.sp,
    showYAxisLabels: Boolean = true,
    onDragIndexChange: ((index: Int?) -> Unit)? = null,
)
```

### Capabilities
- Crosshair and drag interaction
- Peak/valley callout annotations
- Bullish (green) / bearish (red) gradient fill relative to `baselineValue`
- Y-axis labels

### Home Screen — IHSG Index Chart

```kotlin
FinancialStepChart(
    data = state.indexPoints,
    xSelector = { it.datetime },
    ySelector = { it.close },
    baselineValue = state.indexPoints.firstOrNull()?.close,
    showYAxisLabels = true,
    onDragIndexChange = { vm.onIntent(HomeIntent.OnChartDrag(it)) }
)
```

### Detail Screen — Stock Price History Chart

```kotlin
FinancialStepChart(
    data = state.history,
    xSelector = { it.date },
    ySelector = { it.close },
    baselineValue = state.history.firstOrNull()?.close,
    showYAxisLabels = true,
    onDragIndexChange = { vm.onIntent(DetailIntent.OnChartDrag(it)) }
)
```

- `xSelector` maps a domain model to a date/time string displayed on the X-axis.
- `ySelector` maps a domain model to a `Double` price for the Y-axis.
- `baselineValue` should always be the first data point's `close` to determine bullish/bearish coloring.
- `onDragIndexChange` feeds the dragged index back into the ViewModel so the UI can highlight the selected point's metadata.

---

## 6. Domain Models

| Model | Key Fields |
|---|---|
| `Stock` | `ticker`, `name`, `price`, `changePercent`, `volume`, `marketCap` |
| `StockDetail` | `ticker`, `name`, `price`, `change`, `changePercent`, `marketCap`, `pe`, `dividendYield`, `week52High`, `week52Low`, `sector` |
| `OhlcvPoint` | `date: String` *(xSelector)*, `open`, `high`, `low`, `close: Double` *(ySelector)*, `volume` |
| `NewsItem` | `id`, `title`, `summary`, `publishedAt`, `providerName`, `articleUrl`, `thumbnailUrl`, `isPremium` |
| `IndexPoint` | `datetime: String` *(xSelector)*, `open`, `high`, `low`, `close: Double` *(ySelector)*, `change`, `changePercent` |

---

## 7. DTO → Domain Mapping

| DTO | Domain Model | Mapping Notes |
|---|---|---|
| `StockDto` | `Stock` | Direct field rename to camelCase |
| `StockDetailDto` | `StockDetail` | Flatten nested `raw_info` object; camelCase keys |
| `OhlcvDto` | `OhlcvPoint` | `Date` → `date` (xLabel); `Close` → `close` (yValue) |
| `NewsItemDto` | `NewsItem` | Extract `thumbnails.resized_200` for `thumbnailUrl` |
| `IndexDataPointDto` | `IndexPoint` | `datetime` → xLabel; `close` → yValue |

All mapping is done exclusively in `remote/mapper/`. Domain models must never import DTO types.

---

## 8. Use Cases

| Use Case | Repository Call |
|---|---|
| `GetGainersUseCase` | `StockRepository.getGainers(limit)` |
| `GetLosersUseCase` | `StockRepository.getLosers(limit)` |
| `GetStockDetailUseCase` | `StockRepository.getStockDetail(ticker)` |
| `GetStockHistoryUseCase` | `StockRepository.getStockHistory(ticker, period, interval)` |
| `GetStockNewsUseCase` | `StockRepository.getStockNews(ticker, count)` |
| `GetIndexHistoryUseCase` | `IndexRepository.getIndexHistory(symbol, period, interval, limit)` |

Each use case implements the `UseCase` interface defined in `domain/usecase/UseCase.kt` and is registered as `factory {}` in Koin.

### UseCase Interface Contract

```kotlin
interface UseCase<in Params, out Result> {
    suspend operator fun invoke(params: Params): Result
}
```

- `Params` — input type; use `Unit` for use cases that require no parameters
- `Result` — return type, always `kotlin.Result<T>` for network-backed use cases

**Example:**
```kotlin
class GetGainersUseCase(
    private val repository: StockRepository
) : UseCase<GetGainersUseCase.Params, Result<List<Stock>>> {

    data class Params(val limit: Int = 10)

    override suspend fun invoke(params: Params): Result<List<Stock>> =
        repository.getGainers(params.limit)
}
```

For parameterless use cases, declare `UseCase<Unit, Result<T>>` and call with `invoke(Unit)`.

---

## 9. API Endpoints

| Endpoint | Driven By |
|---|---|
| `GET /stocks/gainers?limit=10&region=id` | `GetGainersUseCase` |
| `GET /stocks/losers?limit=10&region=id` | `GetLosersUseCase` |
| `GET /stocks/{ticker}` | `GetStockDetailUseCase` |
| `GET /stocks/{ticker}/history?period=1mo&interval=1d&limit=30` | `GetStockHistoryUseCase` |
| `GET /stocks/{ticker}/news?count=10&tab=all` | `GetStockNewsUseCase` |
| `GET /index/{symbol}/history?period=2d&interval=15m&limit=50` | `GetIndexHistoryUseCase` |

---

## 10. DI Modules

| Module | File | Provides |
|---|---|---|
| `networkModule` | `di/NetworkModule.kt` | `HttpClient`, `Json` instance |
| `dataModule` | `di/DataModule.kt` | All API services, all mappers, repository bindings |
| `domainModule` | `di/DomainModule.kt` | All use cases as `factory {}` |
| `presentationModule` | `di/PresentationModule.kt` | `HomeViewModel`, `DetailViewModel`, `SectorStocksViewModel` as `viewModel {}` |

### Koin Bootstrap

```kotlin
// App.kt — Application.onCreate()
startKoin {
    androidContext(this@App)
    modules(networkModule, dataModule, domainModule, presentationModule)
}
```

---

## 11. Navigation

| Screen | Route | Arguments |
|---|---|---|
| `Screen.Home` | `"home"` | None |
| `Screen.StockDetail` | `"detail/{ticker}"` | `ticker: String` |
| `Screen.SectorStocks` | `"sector/{sectorKey}"` | `sectorKey: String` |

Back navigation uses the standard Compose Navigation back-stack pop (`navController.popBackStack()`). No custom back-stack manipulation needed.

---

## 12. Error Handling Strategy

### `safeApiCall` Wrapper (Data Layer)

Wraps every Ktor call and maps exceptions to a sealed `NetworkError`:

```kotlin
sealed interface NetworkError {
    data object NoInternet : NetworkError
    data object Timeout : NetworkError
    data class ServerError(val code: Int) : NetworkError
    data object Unknown : NetworkError
}
```

| Exception | Mapped To |
|---|---|
| `ResponseException` | `NetworkError.ServerError(code)` |
| `SocketTimeoutException` | `NetworkError.Timeout` |
| `IOException` | `NetworkError.NoInternet` |
| Everything else | `NetworkError.Unknown` |

### Repository → ViewModel

Repositories return `Result<T>`. ViewModels consume via:

```kotlin
result.fold(
    onSuccess = { data -> _state.update { it.copy(data = data, isLoading = false) } },
    onFailure = { error -> _state.update { it.copy(error = error.message, isLoading = false) } }
)
```

### UI Feedback
- **Persistent errors:** `State.error` drives an inline error composable with a retry button.
- **Transient alerts:** `Effect.ShowSnackbar` for non-blocking messages.

### Retry Policy
- HTTP 429: exponential backoff — 2 s → 4 s → 8 s, then surface error.
- HTTP 500: retry up to 3 times.
- HTTP 400: no retry (client error).

---

## 13. Ktor Configuration

```kotlin
HttpClient(Android) {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
    install(Logging) {
        level = LogLevel.BODY
    }
    install(HttpTimeout) {
        requestTimeoutMillis = 30_000
    }
    defaultRequest {
        url(BuildConfig.BASE_URL)
    }
}
```

Configured in `di/NetworkModule.kt`. Injected into `StockApiService` and `IndexApiService` via Koin.

---

## 14. ChartPeriod Enum

```kotlin
enum class ChartPeriod(val period: String, val interval: String, val label: String) {
    ONE_DAY("2d", "15m", "1D"),
    ONE_WEEK("5d", "1h", "1W"),
    ONE_MONTH("1mo", "1d", "1M"),
    SIX_MONTHS("6mo", "1d", "6M"),
    ONE_YEAR("1y", "1wk", "1Y"),
    FIVE_YEARS("5y", "1mo", "5Y")
}
```

> **Note:** `ONE_DAY` uses `period=2d` because `^JKSE` does not support intraday queries with `period=1d`.

`PeriodSelector.kt` renders a row of `FilterChip` components from `ChartPeriod.entries`, dispatching `DetailIntent.ChangePeriod` on tap. The ViewModel re-fetches history using the updated period/interval values.

---

## 15. Deferred to Future Phases

| Feature | Reason Deferred |
|---|---|
| Local caching (Room / DataStore) | Complexity vs. MVP scope; repositories call API directly for now |
| In-memory cache (`InMemoryCache`) | Not needed without offline support requirement |
| Watchlist / Portfolio | Requires authenticated user model not yet defined |
| Push notifications | Depends on backend event infrastructure |
| Authentication / Login | Out of scope for this phase |
| Pagination | API supports `limit` param; pagination UI deferred |

---

*This document is the single source of truth. Any deviation must be discussed and reflected here before implementation.*

