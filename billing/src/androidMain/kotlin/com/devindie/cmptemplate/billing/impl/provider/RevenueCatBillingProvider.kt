package com.devindie.cmptemplate.billing.impl.provider

import com.devindie.cmptemplate.billing.api.BillingCustomerInfo
import com.devindie.cmptemplate.billing.api.BillingError
import com.devindie.cmptemplate.billing.api.BillingPurchase
import com.devindie.cmptemplate.billing.api.BillingResult
import com.devindie.cmptemplate.billing.api.provider.BillingProvider
import com.devindie.cmptemplate.billing.impl.mapper.toBillingCustomerInfo
import com.devindie.cmptemplate.billing.impl.mapper.toBillingOfferings
import com.devindie.cmptemplate.billing.impl.mapper.toBillingPurchase
import com.devindie.cmptemplate.billing.impl.mapper.toBillingStoreError
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.models.CacheFetchPolicy
import com.revenuecat.purchases.kmp.models.CustomerInfo
import com.revenuecat.purchases.kmp.models.Package
import com.revenuecat.purchases.kmp.PurchasesDelegate
import com.revenuecat.purchases.kmp.models.PurchasesError
import com.revenuecat.purchases.kmp.models.StoreProduct
import com.revenuecat.purchases.kmp.models.StoreTransaction
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

internal class RevenueCatBillingProvider : BillingProvider {
    private val purchases: Purchases
        get() = Purchases.sharedInstance

    override suspend fun initialize(): BillingResult<Unit> =
        when (val result = fetchCustomerInfo()) {
            is BillingResult.Success -> BillingResult.Success(Unit)
            is BillingResult.Failure -> result
        }

    override suspend fun getOfferings(): BillingResult<com.devindie.cmptemplate.billing.api.BillingOfferings> =
        suspendCancellableCoroutine { continuation ->
            purchases.getOfferings(
                onError = { error ->
                    continuation.resume(BillingResult.Failure(error.toBillingStoreError()))
                },
                onSuccess = { offerings ->
                    continuation.resume(BillingResult.Success(offerings.toBillingOfferings()))
                },
            )
        }

    override suspend fun purchase(packageId: String): BillingResult<BillingPurchase> {
        val revenueCatPackage =
            findRevenueCatPackage(packageId)
                ?: return BillingResult.Failure(
                    BillingError.StoreError("Package not found: $packageId"),
                )
        return suspendCancellableCoroutine { continuation ->
            purchases.purchase(
                packageToPurchase = revenueCatPackage,
                onError = { error, userCancelled ->
                    continuation.resume(
                        if (userCancelled) {
                            BillingResult.Failure(BillingError.UserCancelled)
                        } else {
                            BillingResult.Failure(error.toBillingStoreError())
                        },
                    )
                },
                onSuccess = { transaction, customerInfo ->
                    continuation.resume(
                        BillingResult.Success(transaction.toBillingPurchase(customerInfo)),
                    )
                },
            )
        }
    }

    override suspend fun restorePurchases(): BillingResult<BillingCustomerInfo> =
        suspendCancellableCoroutine { continuation ->
            purchases.restorePurchases(
                onError = { error ->
                    continuation.resume(BillingResult.Failure(error.toBillingStoreError()))
                },
                onSuccess = { customerInfo ->
                    continuation.resume(BillingResult.Success(customerInfo.toBillingCustomerInfo()))
                },
            )
        }

    override fun observeCustomerInfo(): Flow<BillingCustomerInfo> =
        callbackFlow {
            val delegate =
                object : PurchasesDelegate {
                    override fun onCustomerInfoUpdated(customerInfo: CustomerInfo) {
                        trySend(customerInfo.toBillingCustomerInfo())
                    }

                    override fun onPurchasePromoProduct(
                        product: StoreProduct,
                        startPurchase: (
                            onError: (error: PurchasesError, userCancelled: Boolean) -> Unit,
                            onSuccess: (storeTransaction: StoreTransaction, customerInfo: CustomerInfo) -> Unit,
                        ) -> Unit,
                    ) = Unit
                }
            purchases.delegate = delegate
            purchases.getCustomerInfo(
                fetchPolicy = CacheFetchPolicy.default(),
                onError = { /* initial emission skipped on error */ },
                onSuccess = { customerInfo ->
                    trySend(customerInfo.toBillingCustomerInfo())
                },
            )
            awaitClose {
                if (purchases.delegate == delegate) {
                    purchases.delegate = null
                }
            }
        }

    override suspend fun logIn(appUserId: String): BillingResult<BillingCustomerInfo> =
        suspendCancellableCoroutine { continuation ->
            purchases.logIn(
                newAppUserID = appUserId,
                onError = { error ->
                    continuation.resume(BillingResult.Failure(error.toBillingStoreError()))
                },
                onSuccess = { customerInfo, _ ->
                    continuation.resume(BillingResult.Success(customerInfo.toBillingCustomerInfo()))
                },
            )
        }

    override suspend fun logOut(): BillingResult<BillingCustomerInfo> =
        suspendCancellableCoroutine { continuation ->
            purchases.logOut(
                onError = { error ->
                    continuation.resume(BillingResult.Failure(error.toBillingStoreError()))
                },
                onSuccess = { customerInfo ->
                    continuation.resume(BillingResult.Success(customerInfo.toBillingCustomerInfo()))
                },
            )
        }

    private suspend fun fetchCustomerInfo(): BillingResult<CustomerInfo> =
        suspendCancellableCoroutine { continuation ->
            purchases.getCustomerInfo(
                fetchPolicy = CacheFetchPolicy.default(),
                onError = { error ->
                    continuation.resume(BillingResult.Failure(error.toBillingStoreError()))
                },
                onSuccess = { customerInfo ->
                    continuation.resume(BillingResult.Success(customerInfo))
                },
            )
        }

    private suspend fun findRevenueCatPackage(packageId: String): Package? =
        suspendCancellableCoroutine { continuation ->
            purchases.getOfferings(
                onError = { continuation.resume(null) },
                onSuccess = { offerings ->
                    continuation.resume(
                        offerings.all.values
                            .flatMap { it.availablePackages }
                            .firstOrNull { it.identifier == packageId },
                    )
                },
            )
        }
}
