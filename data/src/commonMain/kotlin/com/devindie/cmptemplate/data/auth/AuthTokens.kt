package com.devindie.cmptemplate.data.auth

import kotlinx.serialization.Serializable

/** OAuth tokens persisted as one encrypted JSON blob in [KSafeTokenStore]. */
@Serializable
data class AuthTokens(
    val accessToken: String = "",
    val refreshToken: String = "",
)
