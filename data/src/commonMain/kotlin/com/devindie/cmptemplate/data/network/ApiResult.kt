package com.devindie.cmptemplate.data.network

/**
 * HTTP-layer outcome for remote calls. Stays in [com.devindie.cmptemplate.data]; map to [Result] before domain.
 */
sealed interface ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>

    data class HttpError(val statusCode: Int, val rawBody: String?) : ApiResult<Nothing>

    data class NetworkError(val cause: Throwable) : ApiResult<Nothing>

    data class ParseError(val cause: Throwable) : ApiResult<Nothing>
}
