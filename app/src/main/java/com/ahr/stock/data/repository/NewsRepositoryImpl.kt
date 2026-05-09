package com.ahr.stock.data.repository

import com.ahr.stock.data.mapper.NewsItemMapper
import com.ahr.stock.data.remote.api.NewsApiService
import com.ahr.stock.domain.model.NewsItem
import com.ahr.stock.domain.repository.NewsRepository

class NewsRepositoryImpl(
    private val apiService: NewsApiService,
    private val newsItemMapper: NewsItemMapper,
) : NewsRepository {

    override suspend fun getHighlightedNews(count: Int, minId: Long?): Result<List<NewsItem>> =
        apiService.getHighlightedNews(count = count, minId = minId)
            .map { newsItemMapper.mapList(it.news) }
}

