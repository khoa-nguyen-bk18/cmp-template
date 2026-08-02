package com.devindie.cmptemplate.feature.auth.api

import com.devindie.cmptemplate.feature.auth.impl.otp.OtpVerificationViewModel
import com.devindie.cmptemplate.feature.auth.impl.phone.PhoneNumberViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val authFeatureModule =
    module {
        viewModelOf(::PhoneNumberViewModel)
        viewModel { (route: OtpVerificationRoute) ->
            OtpVerificationViewModel(
                maskedPhone = route.maskedPhone,
                challengeId = route.challengeId,
                resendCooldownSeconds = RESEND_COOLDOWN_SECONDS,
                verifyOtp = get(),
                resendOtp = get(),
                saveUserSession = get(),
                completeOnboarding = get(),
            )
        }
    }

private const val RESEND_COOLDOWN_SECONDS: Int = 30
