package com.devindie.cmptemplate.billing

import com.devindie.cmptemplate.billing.api.BillingConfig
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.PurchasesConfiguration

/** Set API key here or via build settings for real RevenueCat smoke tests on iOS. */
internal object BillingIosConfig {
    const val ENABLED: Boolean = false
    const val API_KEY: String = ""
}

fun billingConfigForIos(): BillingConfig =
    BillingConfig(
        enabled = BillingIosConfig.ENABLED && BillingIosConfig.API_KEY.isNotBlank(),
        revenueCatApiKeyIos = BillingIosConfig.API_KEY,
    )

fun configureBillingPlatform() {
    if (BillingIosConfig.ENABLED && BillingIosConfig.API_KEY.isNotBlank()) {
        Purchases.configure(
            PurchasesConfiguration(apiKey = BillingIosConfig.API_KEY) {},
        )
    }
}
