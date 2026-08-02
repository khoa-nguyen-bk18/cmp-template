package com.devindie.cmptemplate.feature.auth.impl.phone

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.devindie.cmptemplate.core.ui.insets.appNavigationBarsPadding
import com.devindie.cmptemplate.core.ui.insets.appStatusBarsPadding
import com.devindie.cmptemplate.core.ui.theme.AppTheme
import com.devindie.cmptemplate.domain.model.auth.CountryDialCode
import com.devindie.cmptemplate.domain.model.auth.SupportedCountries
import kotlinx.collections.immutable.toImmutableList

private val AuthScreenBackground = Color(0xFFFFFFFF)
private val AuthSubtitleColor = Color(0xFF3E3E3E)
private val AuthFieldBorderColor = Color(0xFF676767)
private val AuthPlaceholderColor = Color(0xFFC5C5C5)
private val AuthDisabledButtonColor = Color(0xFFC5C5C5)

private val AuthFieldShape = RoundedCornerShape(4.dp)
private val AuthFieldHeight = 46.dp
private val AuthCountryFieldWidth = 86.dp

private val PhoneInputTextStyle =
    TextStyle(
        fontSize = 26.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.055.em,
        lineHeight = 31.2.sp,
    )

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PhoneNumberContent(
    state: PhoneNumberUiState,
    onCountryDropdownExpandedChange: (Boolean) -> Unit,
    onCountrySelected: (CountryDialCode) -> Unit,
    onLocalDigitsChange: (String) -> Unit,
    onNextClick: () -> Unit,
    onSendErrorShown: () -> Unit,
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val fieldColors =
        OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AuthFieldBorderColor,
            unfocusedBorderColor = AuthFieldBorderColor,
            disabledBorderColor = AuthFieldBorderColor,
            errorBorderColor = MaterialTheme.colorScheme.error,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            focusedContainerColor = AuthScreenBackground,
            unfocusedContainerColor = AuthScreenBackground,
            disabledContainerColor = AuthScreenBackground,
            cursorColor = MaterialTheme.colorScheme.onSurface,
        )

    LaunchedEffect(state.sendError) {
        state.sendError?.let { message ->
            snackbarHostState.showSnackbar(message)
            onSendErrorShown()
        }
    }

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(AuthScreenBackground)
                .appStatusBarsPadding()
                .appNavigationBarsPadding()
                .imePadding()
                .testTag("phone_number_screen"),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
        ) {
            IconButton(
                onClick = onBackClick,
                modifier =
                    Modifier
                        .padding(start = (8).dp, top = 8.dp)
                        .size(30.dp)
                        .testTag("phone_back_button"),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier.size(24.dp),
                )
            }

            Text(
                text = "Welcome! What's your mobile number?",
                style =
                    MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Medium,
                        color = Color.Black,
                    ),
                modifier = Modifier.padding(top = 8.dp),
            )
            Text(
                text = "With a valid number, you can access rides, deliveries, and our other services.",
                style =
                    MaterialTheme.typography.bodyMedium.copy(
                        lineHeight = 16.8.sp,
                        letterSpacing = 0.015.em,
                    ),
                color = AuthSubtitleColor,
                modifier = Modifier.padding(top = 17.dp),
            )

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 25.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ExposedDropdownMenuBox(
                    expanded = state.isCountryDropdownExpanded,
                    onExpandedChange = onCountryDropdownExpandedChange,
                    modifier =
                        Modifier
                            .width(AuthCountryFieldWidth)
                            .testTag("country_dropdown"),
                ) {
                    OutlinedTextField(
                        value = "${state.selectedCountry.flagEmoji} ${state.selectedCountry.dialCode}",
                        onValueChange = {},
                        readOnly = true,
                        modifier =
                            Modifier
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = AuthFieldHeight)
                                .height(AuthFieldHeight),
                        textStyle =
                            MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Normal,
                                fontSize = 16.sp,
                                lineHeight = 19.2.sp,
                            ),
                        singleLine = true,
                        shape = AuthFieldShape,
                        colors = fieldColors,
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
                            .defaultMinSize(minHeight = AuthFieldHeight)
                            .height(AuthFieldHeight)
                            .testTag("phone_input"),
                    textStyle = PhoneInputTextStyle,
                    placeholder = {
                        Text(
                            text = "87654321",
                            style = PhoneInputTextStyle,
                            color = AuthPlaceholderColor,
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    shape = AuthFieldShape,
                    colors = fieldColors,
                    isError = state.phoneError != null,
                )
            }

            state.phoneError?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onNextClick,
                enabled = state.isNextEnabled && !state.isLoading,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .padding(bottom = 16.dp)
                        .testTag("next_button"),
                shape = MaterialTheme.shapes.small,
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White,
                        disabledContainerColor = AuthDisabledButtonColor,
                        disabledContentColor = Color.White,
                    ),
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(20.dp),
                        strokeWidth = 2.dp,
                        color = Color.White,
                    )
                } else {
                    Text(
                        text = "Next",
                        style =
                            MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                            ),
                    )
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Preview
@Composable
private fun PhoneNumberContentPreview() {
    AppTheme {
        PhoneNumberContent(
            state =
                PhoneNumberUiState(
                    countries = SupportedCountries.all.toImmutableList(),
                    selectedCountry = SupportedCountries.default,
                    isNextEnabled = false,
                ),
            onCountryDropdownExpandedChange = {},
            onCountrySelected = {},
            onLocalDigitsChange = {},
            onNextClick = {},
            onSendErrorShown = {},
        )
    }
}
