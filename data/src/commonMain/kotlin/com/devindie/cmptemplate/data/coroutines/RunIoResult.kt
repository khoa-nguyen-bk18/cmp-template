package com.devindie.cmptemplate.data.coroutines

import kotlinx.coroutines.CancellationException

/**
 * Like [kotlin.runCatching], but rethrows [CancellationException] so caller scopes cancel promptly.
 */
internal inline fun <T> runIoResult(block: () -> T): Result<T> = try {
    Result.success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    Result.failure(e)
}
