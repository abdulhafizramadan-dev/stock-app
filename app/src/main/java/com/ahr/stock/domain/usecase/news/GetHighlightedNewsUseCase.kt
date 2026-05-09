package com.ahr.stock.domain.usecase.news

import com.ahr.stock.domain.model.NewsItem
import com.ahr.stock.domain.repository.NewsRepository
import com.ahr.stock.domain.usecase.UseCase

class GetHighlightedNewsUseCase(
    private val repository: NewsRepository,
) : UseCase<GetHighlightedNewsUseCase.Params, Result<List<NewsItem>>> {

    data class Params(
        val count: Int = 10,
        val minId: Long? = null,
    )

    override suspend fun invoke(params: Params): Result<List<NewsItem>> =
        repository.getHighlightedNews(count = params.count, minId = params.minId)
}

