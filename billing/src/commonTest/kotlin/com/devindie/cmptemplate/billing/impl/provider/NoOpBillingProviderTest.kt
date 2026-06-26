package com.devindie.cmptemplate.billing.impl.provider

import com.devindie.cmptemplate.billing.api.BillingCustomerInfo
import com.devindie.cmptemplate.billing.api.BillingError
import com.devindie.cmptemplate.billing.api.BillingResult
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NoOpBillingProviderTest {
    private val provider = NoOpBillingProvider()

    @Test
    fun initialize_returnsSuccess() =
        runTest {
            assertTrue(provider.initialize() is BillingResult.Success)
        }

    @Test
    fun getOfferings_returnsEmpty() =
        runTest {
            val result = provider.getOfferings() as BillingResult.Success
            assertEquals(null, result.value.current)
            assertTrue(result.value.all.isEmpty())
        }

    @Test
    fun purchase_returnsNotConfigured() =
        runTest {
            val result = provider.purchase("monthly") as BillingResult.Failure
            assertEquals(BillingError.NotConfigured, result.error)
        }

    @Test
    fun observeCustomerInfo_emitsEmpty() =
        runTest {
            assertEquals(BillingCustomerInfo.Empty, provider.observeCustomerInfo().first())
        }
}
