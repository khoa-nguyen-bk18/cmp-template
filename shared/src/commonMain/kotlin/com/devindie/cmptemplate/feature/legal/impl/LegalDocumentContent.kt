package com.devindie.cmptemplate.feature.legal.impl

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.OpenInBrowser
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.devindie.cmptemplate.feature.legal.impl.platform.LegalWebView
import com.devindie.cmptemplate.feature.legal.impl.platform.rememberOpenUrlExternally

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LegalDocumentContent(
    url: String,
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var state by remember { mutableStateOf(LegalDocumentScreenUiState()) }
    val openUrlExternally = rememberOpenUrlExternally()

    fun openInExternalBrowser() {
        openUrlExternally(url)
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = ::openInExternalBrowser,
                        modifier = Modifier,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.OpenInBrowser,
                            contentDescription = "Open in browser",
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier =
            Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            LegalWebView(
                url = url,
                reloadToken = state.reloadToken,
                onPageStarted = {
                    state = state.copy(isLoading = true, hasError = false)
                },
                onPageFinished = {
                    state = state.copy(isLoading = false, hasError = false)
                },
                onError = {
                    state = state.copy(isLoading = false, hasError = true)
                },
                modifier = Modifier.fillMaxSize(),
            )

            if (state.isLoading && !state.hasError) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            if (state.hasError) {
                LegalDocumentErrorContent(
                    onRetryClick = {
                        state =
                            state.copy(
                                isLoading = true,
                                hasError = false,
                                reloadToken = state.reloadToken + 1,
                            )
                    },
                    onOpenInBrowserClick = ::openInExternalBrowser,
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        }
    }
}

@Composable
private fun LegalDocumentErrorContent(
    onRetryClick: () -> Unit,
    onOpenInBrowserClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Unable to load this page.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        Button(
            onClick = onRetryClick,
            modifier = Modifier.padding(top = 16.dp),
        ) {
            Text(text = "Retry")
        }
        TextButton(
            onClick = onOpenInBrowserClick,
            modifier = Modifier.padding(top = 8.dp),
        ) {
            Text(text = "Open in browser")
        }
    }
}
