package com.ahr.stock.data.repository

import com.ahr.stock.data.mapper.NewsItemMapper
import com.ahr.stock.data.mapper.OhlcvMapper
import com.ahr.stock.data.mapper.StockDetailMapper
import com.ahr.stock.data.mapper.StockMapper
import com.ahr.stock.data.remote.api.StockApiService
import com.ahr.stock.domain.model.NewsItem
import com.ahr.stock.domain.model.Stock
import com.ahr.stock.domain.model.StockDetail
import com.ahr.stock.domain.model.StockHistory
import com.ahr.stock.domain.repository.StockRepository

class StockRepositoryImpl(
    private val apiService: StockApiService,
    private val stockMapper: StockMapper,
    private val stockDetailMapper: StockDetailMapper,
    private val ohlcvMapper: OhlcvMapper,
    private val newsItemMapper: NewsItemMapper,
) : StockRepository {

    override suspend fun getGainers(limit: Int): Result<List<Stock>> =
        apiService.getGainers(limit).map { stockMapper.mapList(it.stocks) }

    override suspend fun getLosers(limit: Int): Result<List<Stock>> =
        apiService.getLosers(limit).map { stockMapper.mapList(it.stocks) }

    override suspend fun getStockDetail(ticker: String): Result<StockDetail> =
        apiService.getStockDetail(ticker).map { stockDetailMapper.map(it) }

    override suspend fun getStockHistory(
        ticker: String,
        period: String,
        interval: String,
        limit: Int,
    ): Result<StockHistory> =
        apiService.getStockHistory(ticker, period, interval, limit).map { dto ->
            StockHistory(
                points = ohlcvMapper.mapList(dto.history),
                previousClose = dto.previousClose,
            )
        }

    override suspend fun getStockNews(ticker: String, count: Int): Result<List<NewsItem>> =
        apiService.getStockNews(ticker, count).map { newsItemMapper.mapList(it.news) }
}

