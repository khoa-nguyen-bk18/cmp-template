package com.devindie.cmptemplate.billing.impl

import com.devindie.cmptemplate.billing.api.BillingCustomerInfo
import com.devindie.cmptemplate.billing.api.BillingError
import com.devindie.cmptemplate.billing.api.BillingOfferings
import com.devindie.cmptemplate.billing.api.BillingPurchase
import com.devindie.cmptemplate.billing.api.BillingResult
import com.devindie.cmptemplate.billing.api.provider.BillingProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BillingClientImplTest {
    @Test
    fun isEntitled_reflectsCachedCustomerInfo() =
        runTest {
            val provider =
                FakeBillingProvider(
                    customerInfo = BillingCustomerInfo(setOf("premium"), emptyMap()),
                )
            val client = BillingClientImpl(provider)
            client.initialize()
            assertTrue(client.isEntitled("premium"))
            assertFalse(client.isEntitled("pro"))
        }

    @Test
    fun purchase_delegatesToProvider() =
        runTest {
            val provider = FakeBillingProvider()
            val client = BillingClientImpl(provider)
            client.purchase("monthly")
            assertEquals("monthly", provider.lastPurchasePackageId)
        }

    @Test
    fun providerThrows_returnsUnknownFailure() =
        runTest {
            val provider =
                object : BillingProvider by FakeBillingProvider() {
                    override suspend fun restorePurchases(): BillingResult<BillingCustomerInfo> = error("network down")
                }
            val client = BillingClientImpl(provider)
            val result = client.restorePurchases() as BillingResult.Failure
            assertTrue(result.error is BillingError.Unknown)
        }
}

private class FakeBillingProvider(
    private val customerInfo: BillingCustomerInfo = BillingCustomerInfo.Empty,
) : BillingProvider {
    var lastPurchasePackageId: String? = null
    private val state = MutableStateFlow(customerInfo)

    override suspend fun initialize(): BillingResult<Unit> {
        state.value = customerInfo
        return BillingResult.Success(Unit)
    }

    override suspend fun getOfferings(): BillingResult<BillingOfferings> =
        BillingResult.Success(BillingOfferings(null, emptyMap()))

    override suspend fun purchase(packageId: String): BillingResult<BillingPurchase> {
        lastPurchasePackageId = packageId
        return BillingResult.Success(
            BillingPurchase(
                productId = "prod",
                transactionId = "tx",
                customerInfo = customerInfo,
            ),
        )
    }

    override suspend fun restorePurchases(): BillingResult<BillingCustomerInfo> = BillingResult.Success(customerInfo)

    override fun observeCustomerInfo(): Flow<BillingCustomerInfo> = state

    override suspend fun logIn(appUserId: String): BillingResult<BillingCustomerInfo> = BillingResult.Success(customerInfo)

    override suspend fun logOut(): BillingResult<BillingCustomerInfo> = BillingResult.Success(BillingCustomerInfo.Empty)
}
