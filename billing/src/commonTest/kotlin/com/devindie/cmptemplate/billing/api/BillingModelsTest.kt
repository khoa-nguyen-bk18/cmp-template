package com.devindie.cmptemplate.billing.api

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BillingModelsTest {
    @Test
    fun billingCustomerInfo_emptyHasNoEntitlements() {
        val info = BillingCustomerInfo(emptySet(), emptyMap())
        assertTrue(info.activeEntitlements.isEmpty())
    }

    @Test
    fun billingResult_successWrapsValue() {
        val result = BillingResult.Success("ok")
        assertEquals("ok", result.value)
    }
}
