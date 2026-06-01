package com.devindie.cmptemplate.data.auth

import com.devindie.cmptemplate.data.network.ApiPaths
import com.devindie.cmptemplate.data.network.ApiResult
import com.devindie.cmptemplate.data.network.NetworkConfig
import com.devindie.cmptemplate.data.network.dto.RefreshTokenRequestDto
import com.devindie.cmptemplate.data.network.dto.TokenResponseDto
import com.devindie.cmptemplate.data.network.safeApiCall
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

/**
 * Refreshes access tokens using a plain [HttpClient] without the Auth plugin (avoids refresh recursion).
 */
class TokenRefreshDataSource(
    private val refreshClient: HttpClient, private val networkConfig: NetworkConfig
) {
    suspend fun refresh(refreshToken: String): ApiResult<TokenResponseDto> = safeApiCall {
        refreshClient.post(networkConfig.baseUrl.trimEnd('/') + ApiPaths.AUTH_REFRESH) {
            contentType(ContentType.Application.Json)
            setBody(RefreshTokenRequestDto(refreshToken = refreshToken))
        }.body()
    }
}
