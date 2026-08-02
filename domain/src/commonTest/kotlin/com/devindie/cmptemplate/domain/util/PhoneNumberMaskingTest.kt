package com.devindie.cmptemplate.domain.util

import com.devindie.cmptemplate.domain.model.auth.PhoneNumber
import kotlin.test.Test
import kotlin.test.assertEquals

class PhoneNumberMaskingTest {
    @Test
    fun maskPhoneNumber_sg_formatsWithMiddleObfuscation() {
        val masked = maskPhoneNumber(PhoneNumber(e164 = "+6587654321"))

        assertEquals("+65 •••• 4321", masked)
    }

    @Test
    fun maskPhoneNumber_my_usesMatchingDialCode() {
        val masked = maskPhoneNumber(PhoneNumber(e164 = "+60123456789"))

        assertEquals("+60 •••• 6789", masked)
    }
}
