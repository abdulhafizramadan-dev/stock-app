package com.ahr.stock.domain.usecase.stock

import com.ahr.stock.domain.model.NewsItem
import com.ahr.stock.domain.repository.StockRepository
import com.ahr.stock.domain.usecase.UseCase

class GetStockNewsUseCase(
    private val repository: StockRepository,
) : UseCase<GetStockNewsUseCase.Params, Result<List<NewsItem>>> {

    data class Params(
        val ticker: String,
        val count: Int = 10,
    )

    override suspend fun invoke(params: Params): Result<List<NewsItem>> =
        repository.getStockNews(params.ticker, params.count)
}

