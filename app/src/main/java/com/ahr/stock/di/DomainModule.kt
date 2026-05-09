package com.ahr.stock.di

import com.ahr.stock.domain.usecase.index.GetIndexHistoryUseCase
import com.ahr.stock.domain.usecase.news.GetHighlightedNewsUseCase
import com.ahr.stock.domain.usecase.stock.GetGainersUseCase
import com.ahr.stock.domain.usecase.stock.GetLosersUseCase
import com.ahr.stock.domain.usecase.stock.GetStockDetailUseCase
import com.ahr.stock.domain.usecase.stock.GetStockHistoryUseCase
import com.ahr.stock.domain.usecase.stock.GetStockNewsUseCase
import org.koin.dsl.module

val domainModule = module {
    factory { GetGainersUseCase(get()) }
    factory { GetLosersUseCase(get()) }
    factory { GetStockDetailUseCase(get()) }
    factory { GetStockHistoryUseCase(get()) }
    factory { GetStockNewsUseCase(get()) }
    factory { GetIndexHistoryUseCase(get()) }
    factory { GetHighlightedNewsUseCase(get()) }
}

