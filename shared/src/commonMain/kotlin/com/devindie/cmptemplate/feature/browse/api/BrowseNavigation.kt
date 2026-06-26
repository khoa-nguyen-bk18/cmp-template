package com.devindie.cmptemplate.feature.browse.api

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.devindie.cmptemplate.core.navigation.MainRoute
import com.devindie.cmptemplate.feature.browse.impl.BrowseScreen
import com.devindie.cmptemplate.feature.legal.api.LegalDocumentBottomSheetHost
import com.devindie.cmptemplate.feature.legal.api.LegalDocumentType

private const val LEGAL_TEST_PRIVACY_POLICY_URL = "https://example.com/privacy"
private const val LEGAL_TEST_TERMS_OF_SERVICE_URL = "https://example.com/terms"

fun NavGraphBuilder.browseDestination(onNavigateToCardDetail: (Long) -> Unit) {
    browseDestination(
        onNavigateToCardDetail = onNavigateToCardDetail,
        privacyPolicyUrl = LEGAL_TEST_PRIVACY_POLICY_URL,
        termsOfServiceUrl = LEGAL_TEST_TERMS_OF_SERVICE_URL,
    )
}

fun NavGraphBuilder.browseDestination(
    onNavigateToCardDetail: (Long) -> Unit,
    privacyPolicyUrl: String,
    termsOfServiceUrl: String,
) {
    composable<MainRoute.Browse> {
        var visibleLegalDocument by remember { mutableStateOf<LegalDocumentType?>(null) }

        BrowseScreen(
            modifier = Modifier.fillMaxSize(),
            onCardClick = { card -> onNavigateToCardDetail(card.id) },
            onPrivacyPolicyClick = { visibleLegalDocument = LegalDocumentType.PrivacyPolicy },
            onTermsOfServiceClick = { visibleLegalDocument = LegalDocumentType.TermsOfService },
        )

        LegalDocumentBottomSheetHost(
            visibleDocument = visibleLegalDocument,
            privacyPolicyUrl = privacyPolicyUrl,
            termsOfServiceUrl = termsOfServiceUrl,
            onDismiss = { visibleLegalDocument = null },
        )
    }
}
