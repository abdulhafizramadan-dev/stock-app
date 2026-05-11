package com.ahr.stock.data.remote.api

import com.ahr.stock.data.remote.dto.NewsResponseDto
import com.ahr.stock.data.remote.dto.OhlcvResponseDto
import com.ahr.stock.data.remote.dto.StockDetailResponseDto
import com.ahr.stock.data.remote.dto.StockListResponseDto
import com.ahr.stock.data.remote.safeApiCall
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class StockApiService(private val client: HttpClient) {

    suspend fun getGainers(limit: Int, region: String = "id"): Result<StockListResponseDto> =
        safeApiCall {
            client.get("stocks/gainers") {
                parameter("limit", limit)
                parameter("region", region)
            }.body()
        }

    suspend fun getLosers(limit: Int, region: String = "id"): Result<StockListResponseDto> =
        safeApiCall {
            client.get("stocks/losers") {
                parameter("limit", limit)
                parameter("region", region)
            }.body()
        }

    suspend fun getTopValues(limit: Int, region: String = "id"): Result<StockListResponseDto> =
        safeApiCall {
            client.get("stocks/top-values") {
                parameter("limit", limit)
                parameter("region", region)
            }.body()
        }

    suspend fun getTopVolumes(limit: Int, region: String = "id"): Result<StockListResponseDto> =
        safeApiCall {
            client.get("stocks/top-volumes") {
                parameter("limit", limit)
                parameter("region", region)
            }.body()
        }

    suspend fun getStockDetail(ticker: String): Result<StockDetailResponseDto> =
        safeApiCall {
            client.get("stocks/$ticker").body()
        }

    suspend fun getStockHistory(
        ticker: String,
        period: String,
        interval: String,
        limit: Int,
    ): Result<OhlcvResponseDto> =
        safeApiCall {
            client.get("stocks/$ticker/history") {
                parameter("period", period)
                parameter("interval", interval)
                parameter("limit", limit)
            }.body()
        }

    suspend fun getStockNews(ticker: String, count: Int): Result<NewsResponseDto> =
        safeApiCall {
            client.get("stocks/$ticker/news") {
                parameter("count", count)
                parameter("tab", "all")
            }.body()
        }
}


