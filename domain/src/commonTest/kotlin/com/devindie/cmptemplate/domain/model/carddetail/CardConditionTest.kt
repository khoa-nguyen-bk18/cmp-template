package com.devindie.cmptemplate.domain.model.carddetail

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CardConditionTest {
    @Test
    fun fromCode_matchesCaseInsensitively() {
        assertEquals(CardCondition.NearMint, CardCondition.fromCode("nm"))
        assertEquals(CardCondition.LightlyPlayed, CardCondition.fromCode("LP"))
    }

    @Test
    fun fromCode_returnsNullForUnknownCode() {
        assertNull(CardCondition.fromCode("UNKNOWN"))
    }

    @Test
    fun selectorOrder_containsAllEntries() {
        assertEquals(CardCondition.entries, CardCondition.selectorOrder)
    }
}
