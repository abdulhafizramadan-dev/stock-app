package com.ahr.stock.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NewsItemDto(
    val id: String,
    val title: String,
    val summary: String? = null,
    val datePublished: String? = null,
    val provider: ProviderDto? = null,
    val articleUrl: String? = null,
    val thumbnail: String? = null,
    val thumbnails: ThumbnailsDto? = null,
    val isPremium: Boolean = false,
    val isEditorsPick: Boolean = false,
)

@Serializable
data class ProviderDto(
    val name: String,
    val url: String? = null,
)

@Serializable
data class ThumbnailsDto(
    val original: String? = null,
    @SerialName("resized_200") val resized200: String? = null,
)

@Serializable
data class NewsResponseDto(
    val news: List<NewsItemDto>,
    val count: Int,
    val ticker: String,
    val timestamp: String,
    val cached: Boolean,
)

