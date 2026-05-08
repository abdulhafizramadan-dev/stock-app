package com.ahr.stock.data.mapper

import com.ahr.stock.data.remote.dto.NewsItemDto
import com.ahr.stock.domain.model.NewsItem

class NewsItemMapper : Mapper<NewsItemDto, NewsItem> {
    override fun map(from: NewsItemDto) = NewsItem(
        id = from.id,
        title = from.title,
        summary = from.summary ?: "",
        publishedAt = from.datePublished ?: "",
        providerName = from.provider?.name ?: "",
        articleUrl = from.articleUrl ?: "",
        thumbnailUrl = from.thumbnails?.resized200 ?: from.thumbnail,
        isPremium = from.isPremium,
        isEditorsPick = from.isEditorsPick,
    )
}

