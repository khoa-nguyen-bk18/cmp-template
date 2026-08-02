package com.devindie.cmptemplate.feature.auth.impl.otp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devindie.cmptemplate.domain.usecase.auth.ResendOtpUseCase
import com.devindie.cmptemplate.domain.model.auth.VerifyOtpRequest
import com.devindie.cmptemplate.domain.usecase.auth.VerifyOtpUseCase
import com.devindie.cmptemplate.domain.usecase.onboarding.CompleteOnboardingUseCase
import com.devindie.cmptemplate.domain.usecase.user.SaveUserSessionUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OtpVerificationViewModel(
    maskedPhone: String,
    private var challengeId: String,
    resendCooldownSeconds: Int,
    private val verifyOtp: VerifyOtpUseCase,
    private val resendOtp: ResendOtpUseCase,
    private val saveUserSession: SaveUserSessionUseCase,
    private val completeOnboarding: CompleteOnboardingUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(OtpVerificationUiState(maskedPhone = maskedPhone))
    val uiState: StateFlow<OtpVerificationUiState> = _uiState.asStateFlow()

    private val _authComplete = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val authComplete: SharedFlow<Unit> = _authComplete.asSharedFlow()

    private var countdownJob: Job? = null

    init {
        startCountdown(resendCooldownSeconds)
    }

    fun onDigitsChange(digits: String) {
        val filtered = digits.filter { it.isDigit() }.take(OTP_LENGTH)
        _uiState.update { it.copy(digits = filtered, otpError = null) }
        if (filtered.length == OTP_LENGTH && !_uiState.value.isVerifying) {
            verify(filtered)
        }
    }

    fun onClearClick() {
        _uiState.update { it.copy(digits = "", otpError = null) }
    }

    fun onResendClick() {
        if (_uiState.value.resendSecondsRemaining > 0 || _uiState.value.isResending) return

        viewModelScope.launch {
            _uiState.update { it.copy(isResending = true) }
            resendOtp(challengeId)
                .onSuccess { challenge ->
                    challengeId = challenge.challengeId
                    _uiState.update {
                        it.copy(
                            digits = "",
                            otpError = null,
                            isResending = false,
                        )
                    }
                    startCountdown(RESEND_COOLDOWN_SECONDS)
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isResending = false,
                            otpError = error.message ?: "Unable to resend code",
                        )
                    }
                }
        }
    }

    private fun verify(code: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isVerifying = true, otpError = null) }
            verifyOtp(VerifyOtpRequest(challengeId = challengeId, code = code))
                .onSuccess { session ->
                    saveUserSession(session)
                    completeOnboarding()
                    _uiState.update { it.copy(isVerifying = false) }
                    _authComplete.emit(Unit)
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isVerifying = false,
                            otpError = error.message ?: "Invalid code",
                        )
                    }
                }
        }
    }

    private fun startCountdown(seconds: Int) {
        countdownJob?.cancel()
        countdownJob =
            viewModelScope.launch {
                var remaining = seconds
                while (remaining >= 0) {
                    _uiState.update { it.copy(resendSecondsRemaining = remaining) }
                    if (remaining == 0) break
                    delay(1_000L)
                    remaining--
                }
            }
    }

    companion object {
        private const val OTP_LENGTH = 6
        private const val RESEND_COOLDOWN_SECONDS = 30
    }
}
