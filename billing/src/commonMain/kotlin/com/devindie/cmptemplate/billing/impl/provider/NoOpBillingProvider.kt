package com.devindie.cmptemplate.billing.impl.provider

import com.devindie.cmptemplate.billing.api.BillingCustomerInfo
import com.devindie.cmptemplate.billing.api.BillingError
import com.devindie.cmptemplate.billing.api.BillingOfferings
import com.devindie.cmptemplate.billing.api.BillingPurchase
import com.devindie.cmptemplate.billing.api.BillingResult
import com.devindie.cmptemplate.billing.api.provider.BillingProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal class NoOpBillingProvider : BillingProvider {
    override suspend fun initialize(): BillingResult<Unit> = BillingResult.Success(Unit)

    override suspend fun getOfferings(): BillingResult<BillingOfferings> =
        BillingResult.Success(
            BillingOfferings(
                current = null,
                all = emptyMap(),
            ),
        )

    override suspend fun purchase(packageId: String): BillingResult<BillingPurchase> =
        BillingResult.Failure(BillingError.NotConfigured)

    override suspend fun restorePurchases(): BillingResult<BillingCustomerInfo> =
        BillingResult.Failure(BillingError.NotConfigured)

    override fun observeCustomerInfo(): Flow<BillingCustomerInfo> = flowOf(BillingCustomerInfo.Empty)

    override suspend fun logIn(appUserId: String): BillingResult<BillingCustomerInfo> =
        BillingResult.Failure(BillingError.NotConfigured)

    override suspend fun logOut(): BillingResult<BillingCustomerInfo> =
        BillingResult.Failure(BillingError.NotConfigured)
}
