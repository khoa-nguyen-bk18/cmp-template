package com.devindie.cmptemplate.domain.validation

import com.devindie.cmptemplate.domain.model.auth.AuthError
import com.devindie.cmptemplate.domain.model.auth.PhoneNumberInput
import com.devindie.cmptemplate.domain.model.auth.SupportedCountries
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PhoneNumberValidatorTest {
    @Test
    fun sg_validNumber_returnsE164() {
        val result =
            PhoneNumberValidator.validate(
                PhoneNumberInput(SupportedCountries.default, "87654321"),
            )

        assertTrue(result.isSuccess)
        assertEquals("+6587654321", result.getOrThrow().e164)
    }

    @Test
    fun sg_invalidPrefix_fails() {
        val result =
            PhoneNumberValidator.validate(
                PhoneNumberInput(SupportedCountries.default, "77654321"),
            )

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is AuthError.InvalidPhoneNumber)
    }

    @Test
    fun sg_tooShort_fails() {
        val result =
            PhoneNumberValidator.validate(
                PhoneNumberInput(SupportedCountries.default, "8765432"),
            )

        assertTrue(result.isFailure)
    }

    @Test
    fun my_validNineDigits_succeeds() {
        val malaysia = SupportedCountries.all.first { it.isoCode == "MY" }
        val result = PhoneNumberValidator.validate(PhoneNumberInput(malaysia, "123456789"))

        assertTrue(result.isSuccess)
        assertEquals("+60123456789", result.getOrThrow().e164)
    }

    @Test
    fun th_validNumber_succeeds() {
        val thailand = SupportedCountries.all.first { it.isoCode == "TH" }
        val result = PhoneNumberValidator.validate(PhoneNumberInput(thailand, "812345678"))

        assertTrue(result.isSuccess)
    }

    @Test
    fun id_validNumber_succeeds() {
        val indonesia = SupportedCountries.all.first { it.isoCode == "ID" }
        val result = PhoneNumberValidator.validate(PhoneNumberInput(indonesia, "8123456789"))

        assertTrue(result.isSuccess)
    }

    @Test
    fun ph_validNumber_succeeds() {
        val philippines = SupportedCountries.all.first { it.isoCode == "PH" }
        val result = PhoneNumberValidator.validate(PhoneNumberInput(philippines, "9123456789"))

        assertTrue(result.isSuccess)
    }

    @Test
    fun vn_validNumber_succeeds() {
        val vietnam = SupportedCountries.all.first { it.isoCode == "VN" }
        val result = PhoneNumberValidator.validate(PhoneNumberInput(vietnam, "912345678"))

        assertTrue(result.isSuccess)
    }

    @Test
    fun emptyDigits_fails() {
        val result = PhoneNumberValidator.validate(PhoneNumberInput(SupportedCountries.default, ""))

        assertTrue(result.isFailure)
    }
}
