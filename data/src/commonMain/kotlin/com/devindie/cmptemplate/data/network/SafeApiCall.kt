package com.devindie.cmptemplate.data.network

import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.statement.bodyAsText
import io.ktor.serialization.JsonConvertException
import kotlinx.coroutines.CancellationException

internal suspend inline fun <T> safeApiCall(crossinline block: suspend () -> T): ApiResult<T> = try {
    ApiResult.Success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: ClientRequestException) {
    ApiResult.HttpError(e.response.status.value, e.response.bodyAsText())
} catch (e: ServerResponseException) {
    ApiResult.HttpError(e.response.status.value, e.response.bodyAsText())
} catch (e: JsonConvertException) {
    ApiResult.ParseError(e)
} catch (e: Exception) {
    ApiResult.NetworkError(e)
}
