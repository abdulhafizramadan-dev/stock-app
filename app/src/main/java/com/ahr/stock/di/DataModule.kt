package com.ahr.stock.di

import com.ahr.stock.data.mapper.IndexPointMapper
import com.ahr.stock.data.mapper.NewsItemMapper
import com.ahr.stock.data.mapper.OhlcvMapper
import com.ahr.stock.data.mapper.SectorSummaryMapper
import com.ahr.stock.data.mapper.StockDetailMapper
import com.ahr.stock.data.mapper.StockMapper
import com.ahr.stock.data.remote.api.IndexApiService
import com.ahr.stock.data.remote.api.NewsApiService
import com.ahr.stock.data.remote.api.SectorApiService
import com.ahr.stock.data.remote.api.StockApiService
import com.ahr.stock.data.repository.IndexRepositoryImpl
import com.ahr.stock.data.repository.NewsRepositoryImpl
import com.ahr.stock.data.repository.SectorRepositoryImpl
import com.ahr.stock.data.repository.StockRepositoryImpl
import com.ahr.stock.domain.repository.IndexRepository
import com.ahr.stock.domain.repository.NewsRepository
import com.ahr.stock.domain.repository.SectorRepository
import com.ahr.stock.domain.repository.StockRepository
import org.koin.dsl.module

val dataModule = module {
    single { StockMapper() }
    single { StockDetailMapper() }
    single { OhlcvMapper() }
    single { NewsItemMapper() }
    single { IndexPointMapper() }
    single { SectorSummaryMapper() }

    single { StockApiService(get()) }
    single { IndexApiService(get()) }
    single { NewsApiService(get()) }
    single { SectorApiService(get()) }

    single<StockRepository> { StockRepositoryImpl(get(), get(), get(), get(), get()) }
    single<IndexRepository> { IndexRepositoryImpl(get(), get()) }
    single<NewsRepository> { NewsRepositoryImpl(get(), get()) }
    single<SectorRepository> { SectorRepositoryImpl(get(), get(), get()) }
}

