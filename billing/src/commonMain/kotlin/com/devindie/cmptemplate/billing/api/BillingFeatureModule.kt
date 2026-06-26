package com.devindie.cmptemplate.billing.api

import com.devindie.cmptemplate.billing.impl.createBillingModule
import org.koin.core.module.Module

fun billingFeatureModule(config: BillingConfig): Module = createBillingModule(config)
