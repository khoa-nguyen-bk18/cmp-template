package com.devindie.cmptemplate.domain.model.user

/**
 * Authenticated session backed by OAuth-style access and refresh tokens.
 *
 * @see com.devindie.cmptemplate.domain.repository.UserRepository
 */
data class UserSession(
    val accessToken: String,
    val refreshToken: String,
)
