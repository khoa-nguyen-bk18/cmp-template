package com.devindie.cmptemplate.feature.legal.impl.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCSignatureOverride
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.WebKit.WKNavigation
import platform.WebKit.WKNavigationDelegateProtocol
import platform.WebKit.WKWebView
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun LegalWebView(
    url: String,
    reloadToken: Int,
    onPageStarted: () -> Unit,
    onPageFinished: () -> Unit,
    onError: () -> Unit,
    modifier: Modifier,
) {
    val navigationDelegate =
        remember(onPageStarted, onPageFinished, onError) {
            LegalWebNavigationDelegate(
                onPageStarted = onPageStarted,
                onPageFinished = onPageFinished,
                onError = onError,
            )
        }

    val webView =
        remember(navigationDelegate) {
            WKWebView().apply {
                this.navigationDelegate = navigationDelegate
            }
        }

    LaunchedEffect(url, reloadToken) {
        val nsUrl = NSURL.URLWithString(url) ?: run {
            onError()
            return@LaunchedEffect
        }
        if (reloadToken == 0) {
            webView.loadRequest(NSURLRequest.requestWithURL(nsUrl))
        } else {
            webView.reload()
        }
    }

    UIKitView(
        factory = { webView },
        modifier = modifier,
    )
}

private class LegalWebNavigationDelegate(
    private val onPageStarted: () -> Unit,
    private val onPageFinished: () -> Unit,
    private val onError: () -> Unit,
) : NSObject(), WKNavigationDelegateProtocol {
    @ObjCSignatureOverride
    override fun webView(webView: WKWebView, didStartProvisionalNavigation: WKNavigation?) {
        onPageStarted()
    }

    @ObjCSignatureOverride
    override fun webView(webView: WKWebView, didFinishNavigation: WKNavigation?) {
        onPageFinished()
    }

    @ObjCSignatureOverride
    override fun webView(
        webView: WKWebView,
        didFailProvisionalNavigation: WKNavigation?,
        withError: platform.Foundation.NSError,
    ) {
        onError()
    }

    @ObjCSignatureOverride
    override fun webView(
        webView: WKWebView,
        didFailNavigation: WKNavigation?,
        withError: platform.Foundation.NSError,
    ) {
        onError()
    }
}
