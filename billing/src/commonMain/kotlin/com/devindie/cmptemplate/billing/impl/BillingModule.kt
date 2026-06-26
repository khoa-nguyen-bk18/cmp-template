package com.devindie.cmptemplate.billing.impl

import com.devindie.cmptemplate.billing.api.BillingClient
import com.devindie.cmptemplate.billing.api.BillingConfig
import com.devindie.cmptemplate.billing.api.provider.BillingProvider
import com.devindie.cmptemplate.billing.impl.provider.NoOpBillingProvider
import org.koin.core.module.Module
import org.koin.core.scope.Scope
import org.koin.dsl.module

internal fun createBillingModule(config: BillingConfig): Module =
    module {
        single<BillingClient> {
            val provider: BillingProvider =
                if (!config.enabled) {
                    NoOpBillingProvider()
                } else {
                    config.provider ?: defaultBillingProvider(config)
                }
            BillingClientImpl(provider = provider)
        }
    }

internal expect fun Scope.defaultBillingProvider(config: BillingConfig): BillingProvider
