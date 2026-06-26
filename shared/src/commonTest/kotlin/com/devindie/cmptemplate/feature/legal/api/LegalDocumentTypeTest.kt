package com.devindie.cmptemplate.feature.legal.api

import kotlin.test.Test
import kotlin.test.assertEquals

class LegalDocumentTypeTest {
    @Test
    fun privacyPolicy_isDistinctFromTermsOfService() {
        assertEquals(LegalDocumentType.PrivacyPolicy, LegalDocumentType.valueOf("PrivacyPolicy"))
    }

    @Test
    fun termsOfService_isDistinctFromPrivacyPolicy() {
        assertEquals(LegalDocumentType.TermsOfService, LegalDocumentType.valueOf("TermsOfService"))
    }
}
