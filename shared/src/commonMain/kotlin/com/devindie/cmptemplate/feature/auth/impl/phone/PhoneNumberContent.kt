package com.devindie.cmptemplate.feature.auth.impl.phone

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.devindie.cmptemplate.core.ui.insets.appNavigationBarsPadding
import com.devindie.cmptemplate.core.ui.insets.appStatusBarsPadding
import com.devindie.cmptemplate.domain.model.auth.CountryDialCode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PhoneNumberContent(
    state: PhoneNumberUiState,
    onCountryDropdownExpandedChange: (Boolean) -> Unit,
    onCountrySelected: (CountryDialCode) -> Unit,
    onLocalDigitsChange: (String) -> Unit,
    onNextClick: () -> Unit,
    onSendErrorShown: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.sendError) {
        state.sendError?.let { message ->
            snackbarHostState.showSnackbar(message)
            onSendErrorShown()
        }
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .appStatusBarsPadding()
                .appNavigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .testTag("phone_number_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Welcome! What's your mobile number?",
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = "With a valid number, you can access rides, deliveries, and our other services.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ExposedDropdownMenuBox(
                expanded = state.isCountryDropdownExpanded,
                onExpandedChange = onCountryDropdownExpandedChange,
                modifier = Modifier.testTag("country_dropdown"),
            ) {
                OutlinedTextField(
                    value = "${state.selectedCountry.flagEmoji} ${state.selectedCountry.dialCode}",
                    onValueChange = {},
                    readOnly = true,
                    modifier =
                        Modifier
                            .menuAnchor()
                            .height(56.dp),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = state.isCountryDropdownExpanded) },
                    shape = RoundedCornerShape(4.dp),
                )
                ExposedDropdownMenu(
                    expanded = state.isCountryDropdownExpanded,
                    onDismissRequest = { onCountryDropdownExpandedChange(false) },
                ) {
                    state.countries.forEach { country ->
                        DropdownMenuItem(
                            text = {
                                Text("${country.flagEmoji} ${country.displayName} (${country.dialCode})")
                            },
                            onClick = { onCountrySelected(country) },
                        )
                    }
                }
            }

            OutlinedTextField(
                value = state.localDigits,
                onValueChange = onLocalDigitsChange,
                modifier =
                    Modifier
                        .weight(1f)
                        .testTag("phone_input"),
                placeholder = { Text("87654321") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                shape = RoundedCornerShape(4.dp),
                isError = state.phoneError != null,
            )
        }

        state.phoneError?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        Button(
            onClick = onNextClick,
            enabled = state.isNextEnabled && !state.isLoading,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("next_button"),
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.height(20.dp),
                    strokeWidth = 2.dp,
                )
            } else {
                Text("Next")
            }
        }

        SnackbarHost(hostState = snackbarHostState)
    }
}
