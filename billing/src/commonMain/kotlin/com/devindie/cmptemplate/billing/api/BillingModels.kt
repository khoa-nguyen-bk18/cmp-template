package com.devindie.cmptemplate.billing.api

import kotlin.time.Instant

data class BillingOfferings(
    val current: BillingOffering?,
    val all: Map<String, BillingOffering>,
)

data class BillingOffering(
    val identifier: String,
    val packages: List<BillingPackage>,
)

data class BillingPackage(
    val identifier: String,
    val productId: String,
    val title: String,
    val description: String,
    val priceFormatted: String,
    val packageType: BillingPackageType,
)

enum class BillingPackageType {
    UNKNOWN,
    CUSTOM,
    LIFETIME,
    ANNUAL,
    SIX_MONTH,
    THREE_MONTH,
    TWO_MONTH,
    MONTHLY,
    WEEKLY,
}

data class BillingPurchase(
    val productId: String,
    val transactionId: String?,
    val customerInfo: BillingCustomerInfo,
)

data class BillingCustomerInfo(
    val activeEntitlements: Set<String>,
    val expirationByEntitlement: Map<String, Instant?>,
) {
    companion object {
        val Empty = BillingCustomerInfo(emptySet(), emptyMap())
    }
}

sealed interface BillingResult<out T> {
    data class Success<T>(
        val value: T,
    ) : BillingResult<T>

    data class Failure(
        val error: BillingError,
    ) : BillingResult<Nothing>
}

sealed interface BillingError {
    data object UserCancelled : BillingError

    data object NotConfigured : BillingError

    data class StoreError(
        val message: String,
        val code: Int? = null,
    ) : BillingError

    data class Unknown(
        val message: String,
    ) : BillingError
}
