package com.ahr.stock.domain.repository

import com.ahr.stock.domain.model.NewsItem

interface NewsRepository {
    suspend fun getHighlightedNews(count: Int, minId: Long?): Result<List<NewsItem>>
}

