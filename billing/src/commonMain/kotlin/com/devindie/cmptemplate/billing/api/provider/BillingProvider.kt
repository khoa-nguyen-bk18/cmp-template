package com.devindie.cmptemplate.billing.api.provider

import com.devindie.cmptemplate.billing.api.BillingCustomerInfo
import com.devindie.cmptemplate.billing.api.BillingOfferings
import com.devindie.cmptemplate.billing.api.BillingPurchase
import com.devindie.cmptemplate.billing.api.BillingResult
import kotlinx.coroutines.flow.Flow

interface BillingProvider {
    suspend fun initialize(): BillingResult<Unit>

    suspend fun getOfferings(): BillingResult<BillingOfferings>

    suspend fun purchase(packageId: String): BillingResult<BillingPurchase>

    suspend fun restorePurchases(): BillingResult<BillingCustomerInfo>

    fun observeCustomerInfo(): Flow<BillingCustomerInfo>

    suspend fun logIn(appUserId: String): BillingResult<BillingCustomerInfo>

    suspend fun logOut(): BillingResult<BillingCustomerInfo>
}
