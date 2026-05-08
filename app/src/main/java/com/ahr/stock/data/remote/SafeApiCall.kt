package com.ahr.stock.data.remote

import io.ktor.client.plugins.ResponseException
import kotlinx.coroutines.delay
import java.io.IOException
import java.net.SocketTimeoutException

private const val RETRY_DELAY_MS = 2_000L
private const val MAX_RETRIES = 3

suspend fun <T> safeApiCall(block: suspend () -> T): Result<T> {
    return executeWithRetry(block, attempt = 0)
}

private suspend fun <T> executeWithRetry(block: suspend () -> T, attempt: Int): Result<T> {
    return try {
        Result.success(block())
    } catch (e: ResponseException) {
        when (val code = e.response.status.value) {
            429 if attempt < MAX_RETRIES -> {
                delay(RETRY_DELAY_MS * (1 shl attempt))
                executeWithRetry(block, attempt + 1)
            }
            in 500..599 if attempt < MAX_RETRIES -> {
                delay(RETRY_DELAY_MS)
                executeWithRetry(block, attempt + 1)
            }
            else -> Result.failure(NetworkException(NetworkError.ServerError(code)))
        }
    } catch (_: SocketTimeoutException) {
        Result.failure(NetworkException(NetworkError.Timeout))
    } catch (_: IOException) {
        Result.failure(NetworkException(NetworkError.NoInternet))
    } catch (_: Exception) {
        Result.failure(NetworkException(NetworkError.Unknown))
    }
}

