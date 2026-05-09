package com.ahr.stock.data.remote.api

import com.ahr.stock.data.remote.dto.HighlightedNewsResponseDto
import com.ahr.stock.data.remote.safeApiCall
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class NewsApiService(private val client: HttpClient) {

    suspend fun getHighlightedNews(
        count: Int = 10,
        minId: Long? = null,
    ): Result<HighlightedNewsResponseDto> =
        safeApiCall {
            client.get("news/highlighted") {
                parameter("count", count)
                if (minId != null) parameter("min_id", minId)
            }.body()
        }
}

