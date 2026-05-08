package com.ahr.stock.domain.model

data class NewsItem(
    val id: String,
    val title: String,
    val summary: String,
    val publishedAt: String,
    val providerName: String,
    val articleUrl: String,
    val thumbnailUrl: String?,
    val isPremium: Boolean,
    val isEditorsPick: Boolean,
)

