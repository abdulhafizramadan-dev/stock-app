package com.ahr.stock.di

import com.ahr.stock.data.mapper.IndexPointMapper
import com.ahr.stock.data.mapper.NewsItemMapper
import com.ahr.stock.data.mapper.OhlcvMapper
import com.ahr.stock.data.mapper.StockDetailMapper
import com.ahr.stock.data.mapper.StockMapper
import com.ahr.stock.data.remote.api.IndexApiService
import com.ahr.stock.data.remote.api.StockApiService
import com.ahr.stock.data.repository.IndexRepositoryImpl
import com.ahr.stock.data.repository.StockRepositoryImpl
import com.ahr.stock.domain.repository.IndexRepository
import com.ahr.stock.domain.repository.StockRepository
import org.koin.dsl.module

import com.ahr.stock.data.remote.api.NewsApiService
import com.ahr.stock.data.repository.NewsRepositoryImpl
import com.ahr.stock.domain.repository.NewsRepository

val dataModule = module {
    single { StockMapper() }
    single { StockDetailMapper() }
    single { OhlcvMapper() }
    single { NewsItemMapper() }
    single { IndexPointMapper() }

    single { StockApiService(get()) }
    single { IndexApiService(get()) }
    single { NewsApiService(get()) }

    single<StockRepository> { StockRepositoryImpl(get(), get(), get(), get(), get()) }
    single<IndexRepository> { IndexRepositoryImpl(get(), get()) }
    single<NewsRepository> { NewsRepositoryImpl(get(), get()) }
}

