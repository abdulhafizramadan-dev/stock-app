package com.ahr.stock.data.remote.api

import com.ahr.stock.data.remote.dto.IndexHistoryResponseDto
import com.ahr.stock.data.remote.safeApiCall
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class IndexApiService(private val client: HttpClient) {

    suspend fun getIndexHistory(
        symbol: String,
        period: String,
        interval: String,
        limit: Int,
    ): Result<IndexHistoryResponseDto> =
        safeApiCall {
            client.get("index/$symbol/history") {
                parameter("period", period)
                parameter("interval", interval)
                parameter("limit", limit)
            }.body()
        }
}

