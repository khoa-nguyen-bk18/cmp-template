package com.devindie.cmptemplate.data.auth

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class KSafeTokenStoreTest {
    @Test
    fun authTokens_emptyByDefault() {
        val tokens = AuthTokens()

        assertEquals("", tokens.accessToken)
        assertEquals("", tokens.refreshToken)
    }

    @Test
    fun authTokens_nonEmptyStrings_roundTripFields() {
        val tokens = AuthTokens(accessToken = "access", refreshToken = "refresh")

        assertEquals("access", tokens.accessToken)
        assertEquals("refresh", tokens.refreshToken)
    }

    @Test
    fun emptyTokenStrings_mapToNullForStoreContract() {
        val access = AuthTokens().accessToken.takeIf { it.isNotEmpty() }
        val refresh = AuthTokens().refreshToken.takeIf { it.isNotEmpty() }

        assertNull(access)
        assertNull(refresh)
    }
}
