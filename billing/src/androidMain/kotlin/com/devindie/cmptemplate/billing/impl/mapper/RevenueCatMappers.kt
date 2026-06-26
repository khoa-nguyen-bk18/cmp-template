package com.devindie.cmptemplate.billing.impl.mapper

import com.devindie.cmptemplate.billing.api.BillingCustomerInfo
import com.devindie.cmptemplate.billing.api.BillingOffering
import com.devindie.cmptemplate.billing.api.BillingOfferings
import com.devindie.cmptemplate.billing.api.BillingPackage
import com.devindie.cmptemplate.billing.api.BillingPackageType
import com.devindie.cmptemplate.billing.api.BillingPurchase
import com.revenuecat.purchases.kmp.models.CustomerInfo
import com.revenuecat.purchases.kmp.models.Offering
import com.revenuecat.purchases.kmp.models.Offerings
import com.revenuecat.purchases.kmp.models.Package
import com.revenuecat.purchases.kmp.models.PackageType
import com.revenuecat.purchases.kmp.models.PurchasesError
import com.revenuecat.purchases.kmp.models.StoreTransaction
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
internal fun CustomerInfo.toBillingCustomerInfo(): BillingCustomerInfo {
    val active = entitlements.active.keys.toSet()
    val expirations =
        entitlements.active.mapValues { (_, info) ->
            info.expirationDateMillis?.let(Instant::fromEpochMilliseconds)
        }
    return BillingCustomerInfo(
        activeEntitlements = active,
        expirationByEntitlement = expirations,
    )
}

internal fun Offerings.toBillingOfferings(): BillingOfferings =
    BillingOfferings(
        current = current?.toBillingOffering(),
        all = all.mapValues { (_, offering) -> offering.toBillingOffering() },
    )

private fun Offering.toBillingOffering(): BillingOffering =
    BillingOffering(
        identifier = identifier,
        packages = availablePackages.map { it.toBillingPackage() },
    )

private fun Package.toBillingPackage(): BillingPackage =
    BillingPackage(
        identifier = identifier,
        productId = storeProduct.id,
        title = storeProduct.title,
        description = storeProduct.localizedDescription.orEmpty(),
        priceFormatted = storeProduct.price.formatted,
        packageType = packageType.toBillingPackageType(),
    )

private fun PackageType.toBillingPackageType(): BillingPackageType =
    when (this) {
        PackageType.UNKNOWN -> BillingPackageType.UNKNOWN
        PackageType.CUSTOM -> BillingPackageType.CUSTOM
        PackageType.LIFETIME -> BillingPackageType.LIFETIME
        PackageType.ANNUAL -> BillingPackageType.ANNUAL
        PackageType.SIX_MONTH -> BillingPackageType.SIX_MONTH
        PackageType.THREE_MONTH -> BillingPackageType.THREE_MONTH
        PackageType.TWO_MONTH -> BillingPackageType.TWO_MONTH
        PackageType.MONTHLY -> BillingPackageType.MONTHLY
        PackageType.WEEKLY -> BillingPackageType.WEEKLY
    }

@OptIn(ExperimentalTime::class)
internal fun StoreTransaction.toBillingPurchase(customerInfo: CustomerInfo): BillingPurchase =
    BillingPurchase(
        productId = productIds.firstOrNull().orEmpty(),
        transactionId = transactionId,
        customerInfo = customerInfo.toBillingCustomerInfo(),
    )

internal fun PurchasesError.toBillingStoreError(): com.devindie.cmptemplate.billing.api.BillingError.StoreError =
    com.devindie.cmptemplate.billing.api.BillingError.StoreError(
        message = message,
        code = code.code,
    )
