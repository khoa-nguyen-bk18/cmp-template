package com.devindie.cmptemplate.feature.legal.impl

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LegalDocumentBottomSheet(
    url: String,
    title: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(url) {
        sheetState.expand()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null,
        shape = RectangleShape,
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        LegalDocumentContent(
            url = url,
            title = title,
            onBack = onDismiss,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
