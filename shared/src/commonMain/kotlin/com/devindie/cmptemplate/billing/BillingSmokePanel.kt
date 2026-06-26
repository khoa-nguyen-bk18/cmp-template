package com.devindie.cmptemplate.billing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.devindie.cmptemplate.billing.api.BillingClient
import com.devindie.cmptemplate.billing.api.BillingError
import com.devindie.cmptemplate.billing.api.BillingResult
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun BillingSmokePanel(
    modifier: Modifier = Modifier,
    billing: BillingClient = koinInject(),
) {
    var status by remember { mutableStateOf("Tap a button to run a billing smoke check.") }
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Billing smoke test",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = status,
            style = MaterialTheme.typography.bodyMedium,
        )
        Button(
            onClick = {
                scope.launch {
                    status =
                        when (val result = billing.initialize()) {
                            is BillingResult.Success -> "initialize: OK"
                            is BillingResult.Failure -> "initialize: ${result.error.format()}"
                        }
                }
            },
        ) {
            Text("Initialize")
        }
        Button(
            onClick = {
                scope.launch {
                    status =
                        when (val result = billing.getOfferings()) {
                            is BillingResult.Success -> {
                                val count = result.value.all.size
                                val current = result.value.current?.identifier ?: "none"
                                "offerings: $count total, current=$current"
                            }
                            is BillingResult.Failure -> "offerings: ${result.error.format()}"
                        }
                }
            },
        ) {
            Text("Get offerings")
        }
        Button(
            onClick = {
                scope.launch {
                    status =
                        when (val result = billing.restorePurchases()) {
                            is BillingResult.Success -> {
                                val entitlements = result.value.activeEntitlements.joinToString()
                                "restore: entitlements=[$entitlements]"
                            }
                            is BillingResult.Failure -> "restore: ${result.error.format()}"
                        }
                }
            },
        ) {
            Text("Restore purchases")
        }
    }
}

private fun BillingError.format(): String =
    when (this) {
        BillingError.NotConfigured -> "not configured (billing disabled or Purchases not configured)"
        BillingError.UserCancelled -> "user cancelled"
        is BillingError.StoreError -> "store error: $message"
        is BillingError.Unknown -> "unknown: $message"
    }
