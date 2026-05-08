package com.ahr.stock.data.remote

sealed interface NetworkError {
    data object NoInternet : NetworkError
    data object Timeout : NetworkError
    data class ServerError(val code: Int) : NetworkError
    data object Unknown : NetworkError
}

class NetworkException(val error: NetworkError) : Exception(error.toString())

