package com.devindie.cmptemplate.feature.legal.api

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.devindie.cmptemplate.core.ui.theme.AppTheme
import com.devindie.cmptemplate.feature.legal.impl.LegalDocumentBottomSheet

/**
 * Shows a legal document in a modal bottom sheet when [visibleDocument] is non-null.
 * URLs are required host inputs — not hardcoded inside the feature.
 */
@Composable
fun LegalDocumentBottomSheetHost(
    visibleDocument: LegalDocumentType?,
    privacyPolicyUrl: String,
    termsOfServiceUrl: String,
    onDismiss: () -> Unit,
) {
    val document = visibleDocument ?: return
    val url =
        when (document) {
            LegalDocumentType.PrivacyPolicy -> privacyPolicyUrl
            LegalDocumentType.TermsOfService -> termsOfServiceUrl
        }
    val title =
        when (document) {
            LegalDocumentType.PrivacyPolicy -> "Privacy Policy"
            LegalDocumentType.TermsOfService -> "Terms of Service"
        }

    LegalDocumentBottomSheet(
        url = url,
        title = title,
        onDismiss = onDismiss,
    )
}

@Preview
@Composable
private fun LegalDocumentBottomSheetPreview() {
    AppTheme {
        LegalDocumentBottomSheetHost(
            visibleDocument = LegalDocumentType.PrivacyPolicy,
            privacyPolicyUrl = "https://example.com/privacy",
            termsOfServiceUrl = "https://example.com/terms",
            onDismiss = {},
        )
    }
}
