package com.devindie.cmptemplate.billing.impl

import com.devindie.cmptemplate.billing.api.BillingConfig
import com.devindie.cmptemplate.billing.api.provider.BillingProvider
import com.devindie.cmptemplate.billing.impl.provider.NoOpBillingProvider
import org.koin.core.scope.Scope

internal actual fun Scope.defaultBillingProvider(config: BillingConfig): BillingProvider = NoOpBillingProvider()
