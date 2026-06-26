package com.devindie.cmptemplate.billing.impl

import com.devindie.cmptemplate.billing.api.BillingClient
import com.devindie.cmptemplate.billing.api.BillingCustomerInfo
import com.devindie.cmptemplate.billing.api.BillingError
import com.devindie.cmptemplate.billing.api.BillingOfferings
import com.devindie.cmptemplate.billing.api.BillingPurchase
import com.devindie.cmptemplate.billing.api.BillingResult
import com.devindie.cmptemplate.billing.api.provider.BillingProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach

internal class BillingClientImpl(
    private val provider: BillingProvider,
) : BillingClient {
    private var cachedCustomerInfo: BillingCustomerInfo = BillingCustomerInfo.Empty

    override suspend fun initialize(): BillingResult<Unit> =
        runSafely {
            provider.initialize().also { result ->
                if (result is BillingResult.Success) {
                    cachedCustomerInfo = provider.observeCustomerInfo().first()
                }
            }
        }

    override suspend fun getOfferings(): BillingResult<BillingOfferings> = runSafely { provider.getOfferings() }

    override suspend fun purchase(packageId: String): BillingResult<BillingPurchase> =
        runSafely {
            provider.purchase(packageId).also { result ->
                if (result is BillingResult.Success) {
                    cachedCustomerInfo = result.value.customerInfo
                }
            }
        }

    override suspend fun restorePurchases(): BillingResult<BillingCustomerInfo> =
        runSafely {
            provider.restorePurchases().also { result ->
                if (result is BillingResult.Success) {
                    cachedCustomerInfo = result.value
                }
            }
        }

    override fun isEntitled(entitlementId: String): Boolean = entitlementId in cachedCustomerInfo.activeEntitlements

    override fun observeCustomerInfo(): Flow<BillingCustomerInfo> =
        provider.observeCustomerInfo().onEach { cachedCustomerInfo = it }

    override suspend fun logIn(appUserId: String): BillingResult<BillingCustomerInfo> =
        runSafely {
            provider.logIn(appUserId).also { result ->
                if (result is BillingResult.Success) {
                    cachedCustomerInfo = result.value
                }
            }
        }

    override suspend fun logOut(): BillingResult<BillingCustomerInfo> =
        runSafely {
            provider.logOut().also { result ->
                if (result is BillingResult.Success) {
                    cachedCustomerInfo = result.value
                }
            }
        }

    private suspend inline fun <T> runSafely(
        crossinline block: suspend () -> BillingResult<T>,
    ): BillingResult<T> =
        try {
            block()
        } catch (@Suppress("TooGenericExceptionCaught") error: Exception) {
            BillingResult.Failure(BillingError.Unknown(error.message ?: "billing_error"))
        }
}
