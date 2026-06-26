package com.devindie.cmptemplate.billing.api

import com.devindie.cmptemplate.billing.api.provider.BillingProvider

data class BillingConfig(
    val enabled: Boolean = false,
    val revenueCatApiKeyAndroid: String = "",
    val revenueCatApiKeyIos: String = "",
    val provider: BillingProvider? = null,
)
