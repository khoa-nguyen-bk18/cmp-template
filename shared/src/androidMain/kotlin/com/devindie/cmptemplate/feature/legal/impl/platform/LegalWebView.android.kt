package com.devindie.cmptemplate.feature.legal.impl.platform

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
actual fun LegalWebView(
    url: String,
    reloadToken: Int,
    onPageStarted: () -> Unit,
    onPageFinished: () -> Unit,
    onError: () -> Unit,
    modifier: Modifier,
) {
    val context = LocalContext.current
    val webView =
        remember {
            WebView(context).apply {
                settings.javaScriptEnabled = true
                webViewClient =
                    object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            onPageStarted()
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            onPageFinished()
                        }

                        override fun onReceivedError(
                            view: WebView?,
                            request: WebResourceRequest?,
                            error: WebResourceError?,
                        ) {
                            if (request?.isForMainFrame == true) {
                                onError()
                            }
                        }
                    }
            }
        }

    LaunchedEffect(url, reloadToken) {
        if (reloadToken == 0) {
            webView.loadUrl(url)
        } else {
            webView.reload()
        }
    }

    AndroidView(
        factory = { webView },
        modifier = modifier,
    )
}
