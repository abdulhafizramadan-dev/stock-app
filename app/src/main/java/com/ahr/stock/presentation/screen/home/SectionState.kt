package com.ahr.stock.presentation.screen.home

sealed interface SectionState<out T> {
    data object Idle : SectionState<Nothing>
    data object Loading : SectionState<Nothing>
    data class Success<T>(val data: T) : SectionState<T>
    data class Error(val message: String) : SectionState<Nothing>
}

