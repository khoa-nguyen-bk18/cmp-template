package com.devindie.cmptemplate.billing.api

import kotlinx.coroutines.flow.Flow

interface BillingClient {
    suspend fun initialize(): BillingResult<Unit>

    suspend fun getOfferings(): BillingResult<BillingOfferings>

    suspend fun purchase(packageId: String): BillingResult<BillingPurchase>

    suspend fun restorePurchases(): BillingResult<BillingCustomerInfo>

    fun isEntitled(entitlementId: String): Boolean

    fun observeCustomerInfo(): Flow<BillingCustomerInfo>

    suspend fun logIn(appUserId: String): BillingResult<BillingCustomerInfo>

    suspend fun logOut(): BillingResult<BillingCustomerInfo>
}
