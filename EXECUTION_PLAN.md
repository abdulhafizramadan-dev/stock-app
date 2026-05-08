# EXECUTION_PLAN.md — StockBit Mini Android App

> Step-by-step technical implementation guide. Follow phases sequentially — each phase produces a compilable state before the next begins.

---

## Overview

```
Phase 1 → Project Foundation      (dependencies, BuildConfig, App.kt)
Phase 2 → Domain Layer            (models, interfaces, use cases)
Phase 3 → Data Layer              (DTOs, mappers, API services, repositories)
Phase 4 → DI Wiring              (Koin modules, full graph verification)
Phase 5 → Presentation Core      (navigation, contracts, shared components)
Phase 6 → Screens & Integration  (ViewModels, Screens, end-to-end)
```

**Rule:** Never start a phase until all steps of the previous phase compile cleanly.

---

## Phase 1 — Project Foundation

**Goal:** Clean, compilable project scaffold with all dependencies declared and `BuildConfig.BASE_URL` available.

---

### Step 1.1 — Dependency Declarations

**Modify:** `gradle/libs.versions.toml`

| Action | Detail |
|---|---|
| ➕ Add | `ktor-client-android`, `ktor-client-content-negotiation`, `ktor-serialization-kotlinx-json`, `ktor-client-logging` |
| ➕ Add | `kotlinx-serialization-json` |
| ➕ Add | `koin-android`, `koin-androidx-compose` |
| ➕ Add | `coil-compose` |
| ➕ Add | `androidx-navigation-compose` |
| ➕ Add | `androidx-lifecycle-viewmodel-compose` |
| ➕ Add | `kotlin-serialization` plugin alias |
| ❌ Remove | `vico-compose`, `vico-compose-m3` library aliases and version |

**Depends on:** nothing

---

### Step 1.2 — Build Script Wiring

**Modify:** `app/build.gradle.kts` and root `build.gradle.kts`

- Apply `kotlin-serialization` plugin
- Enable `buildConfig = true` in `buildFeatures`
- Add `buildConfigField("String", "BASE_URL", "\"<url>\"")` inside `defaultConfig`
  - `debug` → `http://10.0.2.2:8000` (Android emulator localhost)
  - `release` → production Render URL
- Wire all new `libs.*` aliases as `implementation(...)` dependencies
- Remove Vico `implementation` lines

**Depends on:** Step 1.1

---

### Step 1.3 — Application Class

**Create:** `com/ahr/stock/App.kt`

```kotlin
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(/* populated in Phase 4 */)
        }
    }
}
```

**Modify:** `AndroidManifest.xml` — set `android:name=".App"`

**Depends on:** Step 1.2

---

## Phase 2 — Domain Layer

**Goal:** Pure-Kotlin domain with zero Android/framework imports. Independently unit-testable.

---

### Step 2.1 — Domain Models

**Create:**

| File | Content |
|---|---|
| `domain/model/Stock.kt` | `ticker`, `name`, `price`, `changePercent`, `volume`, `marketCap` |
| `domain/model/StockDetail.kt` | `ticker`, `name`, `price`, `change`, `changePercent`, `marketCap`, `pe`, `dividendYield`, `week52High`, `week52Low`, `sector` |
| `domain/model/OhlcvPoint.kt` | `date: String`, `open`, `high`, `low`, `close: Double`, `volume` |
| `domain/model/NewsItem.kt` | `id`, `title`, `summary`, `publishedAt`, `providerName`, `articleUrl`, `thumbnailUrl`, `isPremium` |
| `domain/model/IndexPoint.kt` | `datetime: String`, `open`, `high`, `low`, `close: Double`, `change`, `changePercent` |
| `domain/model/ChartPeriod.kt` | Enum per ARCHITECTURE.md §14 |

> `OhlcvPoint.date` and `IndexPoint.datetime` are plain `String` — they serve directly as `xSelector` input for `FinancialStepChart`.

**Depends on:** nothing

---

### Step 2.2 — Repository Interfaces

**Create:** `domain/repository/StockRepository.kt`, `IndexRepository.kt`

All functions are `suspend` and return `Result<T>`:

```kotlin
interface StockRepository {
    suspend fun getGainers(limit: Int): Result<List<Stock>>
    suspend fun getLosers(limit: Int): Result<List<Stock>>
    suspend fun getStockDetail(ticker: String): Result<StockDetail>
    suspend fun getStockHistory(ticker: String, period: String, interval: String, limit: Int): Result<List<OhlcvPoint>>
    suspend fun getStockNews(ticker: String, count: Int): Result<List<NewsItem>>
}

interface IndexRepository {
    suspend fun getIndexHistory(symbol: String, period: String, interval: String, limit: Int): Result<List<IndexPoint>>
}
```

**Depends on:** Step 2.1

---

### Step 2.3 — UseCase Base Interface

**Create:** `domain/usecase/UseCase.kt`

```kotlin
interface UseCase<in Params, out Result> {
    suspend operator fun invoke(params: Params): Result
}
```

**Depends on:** nothing

---

### Step 2.4 — Use Case Implementations

**Create:**

| File | Params | Returns |
|---|---|---|
| `usecase/stock/GetGainersUseCase.kt` | `Params(limit: Int = 10)` | `Result<List<Stock>>` |
| `usecase/stock/GetLosersUseCase.kt` | `Params(limit: Int = 10)` | `Result<List<Stock>>` |
| `usecase/stock/GetStockDetailUseCase.kt` | `Params(ticker: String)` | `Result<StockDetail>` |
| `usecase/stock/GetStockHistoryUseCase.kt` | `Params(ticker, period, interval, limit)` | `Result<List<OhlcvPoint>>` |
| `usecase/stock/GetStockNewsUseCase.kt` | `Params(ticker, count)` | `Result<List<NewsItem>>` |
| `usecase/index/GetIndexHistoryUseCase.kt` | `Params(symbol, period, interval, limit)` | `Result<List<IndexPoint>>` |

Each nests its own `data class Params(...)` and has one `override suspend fun invoke(params: Params)` that delegates to a repository.

**Depends on:** Steps 2.2 and 2.3

---

## Phase 3 — Data Layer

**Goal:** Full network plumbing, type-safe DTOs, clean mappers, repository implementations. Project compiles against real API contract.

---

### Step 3.1 — NetworkError + safeApiCall

**Create:** `data/remote/NetworkError.kt`

```kotlin
sealed interface NetworkError {
    data object NoInternet : NetworkError
    data object Timeout : NetworkError
    data class ServerError(val code: Int) : NetworkError
    data object Unknown : NetworkError
}
```

**Create:** `data/remote/SafeApiCall.kt`

```kotlin
suspend fun <T> safeApiCall(block: suspend () -> T): Result<T> = try {
    Result.success(block())
} catch (e: ResponseException) {
    Result.failure(NetworkException(NetworkError.ServerError(e.response.status.value)))
} catch (e: SocketTimeoutException) {
    Result.failure(NetworkException(NetworkError.Timeout))
} catch (e: IOException) {
    Result.failure(NetworkException(NetworkError.NoInternet))
} catch (e: Exception) {
    Result.failure(NetworkException(NetworkError.Unknown))
}
```

- HTTP 429 retry logic (exponential backoff: 2 s → 4 s → 8 s) implemented here
- HTTP 500 retry up to 3 times; HTTP 400 no retry

**Depends on:** Step 1.2 (Ktor on classpath)

---

### Step 3.2 — DTOs

**Create:** `data/remote/dto/*.kt` — one file per endpoint response shape

All classes annotated `@Serializable`. Use `@SerialName("snake_case")` for every field whose JSON key differs from the Kotlin property name.

| File | Key Notes |
|---|---|
| `StockDto.kt` | Flat object; matches gainers/losers list item |
| `StockDetailDto.kt` | Contains nested `RawInfoDto` for `raw_info` field |
| `OhlcvDto.kt` | Fields: `Date`, `Open`, `High`, `Low`, `Close`, `Volume` (capitalized per Yahoo Finance convention) |
| `NewsItemDto.kt` | Contains nested `ThumbnailsDto` for `thumbnails.resized_200` |
| `IndexDataPointDto.kt` | Fields: `datetime`, `open`, `high`, `low`, `close`, `change`, `change_percent` |

**Depends on:** Step 1.2

---

### Step 3.3 — Mapper Interface + Implementations

**Create:** `data/mapper/Mapper.kt`

```kotlin
interface Mapper<in From, out To> {
    fun map(from: From): To
    fun mapList(from: List<From>): List<To> = from.map { map(it) }
}
```

**Create mapper implementations:**

| File | From → To | Key Mapping |
|---|---|---|
| `StockMapper.kt` | `StockDto → Stock` | Direct camelCase rename |
| `StockDetailMapper.kt` | `StockDetailDto → StockDetail` | Flatten `raw_info` nested object |
| `OhlcvMapper.kt` | `OhlcvDto → OhlcvPoint` | `Date → date`, `Close → close` |
| `NewsItemMapper.kt` | `NewsItemDto → NewsItem` | Extract `thumbnails.resized_200` for `thumbnailUrl` |
| `IndexPointMapper.kt` | `IndexDataPointDto → IndexPoint` | `datetime → datetime`, `close → close` |

**Depends on:** Steps 2.1 and 3.2

---

### Step 3.4 — API Services

**Create:** `data/remote/api/StockApiService.kt`, `IndexApiService.kt`

- Plain classes injected with `HttpClient`
- Every function wraps its Ktor call in `safeApiCall {}`
- Returns `Result<XxxDto>` or `Result<List<XxxDto>>`
- Query params aligned to ARCHITECTURE.md §9 endpoint table

**Depends on:** Steps 3.1 and 3.2

---

### Step 3.5 — Repository Implementations

**Create:** `data/repository/StockRepositoryImpl.kt`, `IndexRepositoryImpl.kt`

- Implement domain repository interfaces
- Inject API service + relevant mappers
- Pattern: `apiService.call().map { mapper.map(it) }`

**Depends on:** Steps 2.2, 3.3, 3.4

---

### Step 3.6 — Network + Data Koin Modules

**Create:** `di/NetworkModule.kt`

```kotlin
val networkModule = module {
    single { Json { ignoreUnknownKeys = true } }
    single {
        HttpClient(Android) {
            install(ContentNegotiation) { json(get()) }
            install(Logging) { level = LogLevel.BODY }
            install(HttpTimeout) { requestTimeoutMillis = 30_000 }
            defaultRequest { url(BuildConfig.BASE_URL) }
        }
    }
}
```

**Create:** `di/DataModule.kt`

```kotlin
val dataModule = module {
    single { StockMapper() }
    single { StockDetailMapper() }
    single { OhlcvMapper() }
    single { NewsItemMapper() }
    single { IndexPointMapper() }
    single { StockApiService(get()) }
    single { IndexApiService(get()) }
    single<StockRepository> { StockRepositoryImpl(get(), get(), get(), get(), get()) }
    single<IndexRepository> { IndexRepositoryImpl(get(), get()) }
}
```

**Depends on:** Steps 3.4 and 3.5

---

## Phase 4 — DI Wiring

**Goal:** Full Koin graph compiles and starts without crash. All layers wired end-to-end.

---

### Step 4.1 — Domain Koin Module

**Create:** `di/DomainModule.kt`

```kotlin
val domainModule = module {
    factory { GetGainersUseCase(get()) }
    factory { GetLosersUseCase(get()) }
    factory { GetStockDetailUseCase(get()) }
    factory { GetStockHistoryUseCase(get()) }
    factory { GetStockNewsUseCase(get()) }
    factory { GetIndexHistoryUseCase(get()) }
}
```

**Depends on:** Steps 2.4 and 3.6

---

### Step 4.2 — Presentation Koin Module (stub)

**Create:** `di/PresentationModule.kt` with stub `viewModel {}` blocks.
Real ViewModel constructors are filled in during Step 6.5.

**Depends on:** Step 4.1

---

### Step 4.3 — Wire Modules into App.kt

**Modify:** `App.kt` — replace placeholder with:

```kotlin
modules(networkModule, dataModule, domainModule, presentationModule)
```

▶️ **Build & run** — verify Koin starts with no missing-binding exceptions before continuing.

**Depends on:** Steps 4.1 and 4.2

---

## Phase 5 — Presentation Core

**Goal:** Navigation skeleton and all reusable composables ready. Screens can be assembled without rework.

---

### Step 5.1 — MVI Contracts

**Create:** `presentation/screen/home/HomeContract.kt`

```kotlin
sealed interface HomeIntent { ... }
data class HomeState( ... )
sealed interface HomeEffect { ... }
```

**Create:** `presentation/screen/detail/DetailContract.kt`

```kotlin
sealed interface DetailIntent { ... }
data class DetailState( ... )
sealed interface DetailEffect { ... }
```

Full definitions per ARCHITECTURE.md §4.

**Depends on:** Step 2.1

---

### Step 5.2 — Navigation Setup

**Create:** `presentation/navigation/Screen.kt`

```kotlin
sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data class StockDetail(val ticker: String) : Screen("detail/{ticker}") {
        fun createRoute() = "detail/$ticker"
    }
}
```

**Create:** `presentation/navigation/NavGraph.kt` — `NavHost` with placeholder composables for `home` and `detail/{ticker}` routes. Replace with real screens in Step 6.5.

**Depends on:** Step 5.1

---

### Step 5.3 — Shared Components

> ⚠️ Highest-effort step. Build and `@Preview` each component in isolation before moving to screens.

**Create:**

| File | Responsibility |
|---|---|
| `components/PriceChip.kt` | Colored chip showing `+x.xx%` / `−x.xx%`; green for positive, red for negative |
| `components/StockRow.kt` | Ticker, name, price, `PriceChip` — used in gainers/losers `LazyColumn` |
| `components/NewsCard.kt` | Coil `AsyncImage` thumbnail, title, provider name, published date, premium badge |
| `components/PeriodSelector.kt` | `FilterChip` row from `ChartPeriod.entries`; highlights selected period |
| `components/FinancialStepChart.kt` | Full Canvas chart per ARCHITECTURE.md §5; crosshair/drag, gradient fill, Y-axis labels |

**Depends on:** Steps 2.1 and 5.1

---

## Phase 6 — Screens & Integration

**Goal:** Fully working app with both screens end-to-end.

---

### Step 6.1 — HomeViewModel

**Create:** `presentation/screen/home/HomeViewModel.kt`

- `StateFlow<HomeState>` + `SharedFlow<HomeEffect>`
- `onIntent()` handles: `LoadMarket` (parallel fetch of gainers + losers + index history via `async/await`), `Refresh`, `SelectStock` → emits `NavigateToDetail`, `SelectTab`, `OnChartDrag`
- `result.fold` pattern per ARCHITECTURE.md §12

**Depends on:** Steps 2.4, 4.1, 5.1

---

### Step 6.2 — HomeScreen

**Create:** `presentation/screen/home/HomeScreen.kt`

- `Scaffold` with `TopAppBar`
- `FinancialStepChart` for IHSG index history
- `TabRow` for Gainers / Losers tabs
- `LazyColumn` of `StockRow` items
- `LaunchedEffect` collecting `HomeEffect` → navigate or show snackbar
- Inline error composable with retry button when `state.error != null`

**Depends on:** Steps 5.3 and 6.1

---

### Step 6.3 — DetailViewModel

**Create:** `presentation/screen/detail/DetailViewModel.kt`

- Fetches `StockDetail`, `List<OhlcvPoint>` (default `ChartPeriod.ONE_MONTH`), and `List<NewsItem>` in parallel on `LoadDetail`
- Re-fetches only history on `ChangePeriod`
- Emits `OpenUrl` effect on `OpenNewsArticle`

**Depends on:** Steps 2.4, 4.1, 5.1

---

### Step 6.4 — DetailScreen

**Create:** `presentation/screen/detail/DetailScreen.kt`

- Header: stock name, price, change amount + percent
- `PeriodSelector` row → `ChangePeriod` intent
- `FinancialStepChart` for price history with drag-to-inspect
- Dragged point info overlay (date + price from `state.draggedIndex`)
- `LazyColumn` of `NewsCard` items
- Inline error composable with retry button
- Back navigation via `navController.popBackStack()`

**Depends on:** Steps 5.3 and 6.3

---

### Step 6.5 — Finalize DI + Navigation

**Modify:** `di/PresentationModule.kt` — replace stubs with real constructors:

```kotlin
val presentationModule = module {
    viewModel { HomeViewModel(get(), get(), get()) }
    viewModel { DetailViewModel(get(), get(), get(), get()) }
}
```

**Modify:** `presentation/navigation/NavGraph.kt` — replace placeholder composables with real screen calls, pass `navController` to handle `NavigateToDetail` effects.

▶️ **Final build & smoke test** — run on emulator, verify Home loads, stock row tap navigates to Detail, chart drag works, period selector re-fetches data.

**Depends on:** Steps 6.2 and 6.4

---

## Dependency Graph Summary

```
1.1 → 1.2 → 1.3
      1.2 → 2.1 → 2.2 → 2.4
                   2.3 → 2.4
             2.1 → 3.2 → 3.3 → 3.5
             3.1 → 3.4 → 3.5 → 3.6 → 4.1 → 4.2 → 4.3
             2.2 → 3.5
      2.4 + 4.1 → 6.1 → 6.2 → 6.5
                → 6.3 → 6.4 → 6.5
      2.1 + 5.1 → 5.3 → 6.2
                       → 6.4
```

---

## Notes

1. **BASE_URL per build type** — Use `buildTypes` in `app/build.gradle.kts` to set different `buildConfigField` values: `debug` → emulator localhost, `release` → Render production URL.
2. **Parallel loading in ViewModels** — Use `viewModelScope.launch { val a = async { }; val b = async { }; awaitAll(a, b) }` in both `HomeViewModel` and `DetailViewModel` to avoid sequential loading delays.
3. **FinancialStepChart isolation** — Build and preview the chart composable standalone in Step 5.3 before wiring it to screen state in Phase 6.

---

*Follow phases in order. Each phase boundary is a safe checkpoint to commit, review, and verify before proceeding.*

