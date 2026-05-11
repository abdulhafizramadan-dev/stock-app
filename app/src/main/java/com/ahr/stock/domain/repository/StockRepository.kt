package com.ahr.stock.domain.repository

import com.ahr.stock.domain.model.NewsItem
import com.ahr.stock.domain.model.Stock
import com.ahr.stock.domain.model.StockDetail
import com.ahr.stock.domain.model.StockHistory

interface StockRepository {
    suspend fun getGainers(limit: Int): Result<List<Stock>>
    suspend fun getLosers(limit: Int): Result<List<Stock>>
    suspend fun getStockDetail(ticker: String): Result<StockDetail>
    suspend fun getStockHistory(ticker: String, period: String, interval: String, limit: Int): Result<StockHistory>
    suspend fun getStockNews(ticker: String, count: Int): Result<List<NewsItem>>
}

