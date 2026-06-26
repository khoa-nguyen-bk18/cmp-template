package com.devindie.cmptemplate.billing.impl

import com.devindie.cmptemplate.billing.api.BillingConfig
import com.devindie.cmptemplate.billing.api.provider.BillingProvider
import com.devindie.cmptemplate.billing.impl.provider.RevenueCatBillingProvider
import org.koin.core.scope.Scope

internal actual fun Scope.defaultBillingProvider(config: BillingConfig): BillingProvider = RevenueCatBillingProvider()
