package com.devindie.cmptemplate.feature.auth.impl.phone

import com.devindie.cmptemplate.domain.model.auth.CountryDialCode
import com.devindie.cmptemplate.domain.model.auth.PhoneNumberInput
import com.devindie.cmptemplate.domain.model.auth.SupportedCountries
import com.devindie.cmptemplate.domain.usecase.auth.GetSupportedCountriesUseCase
import com.devindie.cmptemplate.domain.usecase.auth.SendOtpUseCase
import com.devindie.cmptemplate.domain.usecase.auth.ValidatePhoneNumberUseCase
import com.devindie.cmptemplate.domain.util.maskPhoneNumber
import com.devindie.cmptemplate.feature.auth.api.OtpVerificationRoute
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

class PhoneNumberViewModel(
    private val getSupportedCountries: GetSupportedCountriesUseCase,
    private val validatePhoneNumber: ValidatePhoneNumberUseCase,
    private val sendOtp: SendOtpUseCase,
) : ViewModel() {
    private val _uiState =
        MutableStateFlow(
            PhoneNumberUiState(
                countries = kotlinx.collections.immutable.persistentListOf(SupportedCountries.default),
                selectedCountry = SupportedCountries.default,
            ),
        )
    val uiState: StateFlow<PhoneNumberUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<PhoneNumberEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<PhoneNumberEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            val countries = getSupportedCountries()
            _uiState.update {
                it.copy(
                    countries = countries.toImmutableList(),
                    selectedCountry = countries.first(),
                )
            }
        }
    }

    fun onCountryDropdownExpandedChange(expanded: Boolean) {
        _uiState.update { it.copy(isCountryDropdownExpanded = expanded) }
    }

    fun onCountrySelected(country: CountryDialCode) {
        _uiState.update {
            it.copy(
                selectedCountry = country,
                isCountryDropdownExpanded = false,
                phoneError = null,
            )
        }
        revalidate()
    }

    fun onLocalDigitsChange(digits: String) {
        val filtered = digits.filter { it.isDigit() }
        _uiState.update { it.copy(localDigits = filtered, phoneError = null) }
        revalidate()
    }

    fun onNextClick() {
        val state = _uiState.value
        if (!state.isNextEnabled || state.isLoading) return

        viewModelScope.launch {
            val validation = validatePhoneNumber(currentInput())
            val phone =
                validation.getOrElse { error ->
                    _uiState.update {
                        it.copy(phoneError = error.message ?: "Invalid phone number")
                    }
                    return@launch
                }

            _uiState.update { it.copy(isLoading = true, sendError = null) }
            sendOtp(phone)
                .onSuccess { challenge ->
                    _uiState.update { it.copy(isLoading = false) }
                    _events.emit(
                        PhoneNumberEvent.NavigateToOtp(
                            OtpVerificationRoute(
                                challengeId = challenge.challengeId,
                                maskedPhone = maskPhoneNumber(phone),
                                resendAvailableAtEpochMs = challenge.resendAvailableAtEpochMs,
                            ),
                        ),
                    )
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            sendError = error.message ?: "Unable to send code",
                        )
                    }
                }
        }
    }

    fun onSendErrorShown() {
        _uiState.update { it.copy(sendError = null) }
    }

    private fun revalidate() {
        viewModelScope.launch {
            val result = validatePhoneNumber(currentInput())
            _uiState.update {
                it.copy(
                    isNextEnabled = result.isSuccess,
                    phoneError = null,
                )
            }
        }
    }

    private fun currentInput(): PhoneNumberInput =
        PhoneNumberInput(
            country = _uiState.value.selectedCountry,
            localDigits = _uiState.value.localDigits,
        )
}

sealed interface PhoneNumberEvent {
    data class NavigateToOtp(val route: OtpVerificationRoute) : PhoneNumberEvent
}
