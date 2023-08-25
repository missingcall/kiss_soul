package com.kissspace.webview.init

import android.app.Application
import android.content.Context
import android.content.MutableContextWrapper
import android.os.Looper
import android.webkit.WebView
import com.kissspace.webview.jsbridge.BridgeWebView
import java.util.*

object WebViewCacheHolder {
    private val webViewCacheStack = Stack<BridgeWebView>()
    private val webViewHomeCacheStack = Stack<WebView>()
    private const val CACHED_WEB_VIEW_MAX_NUM = 4

    private  var application: Application?=null

    fun init(application: Application) {
        WebViewCacheHolder.application = application
        prepareWebView()
        prepareHomeWebView()
    }

    fun prepareWebView() {
        if (webViewCacheStack.size < CACHED_WEB_VIEW_MAX_NUM) {
                Looper.myQueue().addIdleHandler {
                    if (webViewCacheStack.size < CACHED_WEB_VIEW_MAX_NUM) {
                        application?.let {
                            webViewCacheStack.push(createWebView(MutableContextWrapper(it)))
                        }
                    }
                    false
                }
        }
    }

    fun acquireWebViewInternal(context: Context): BridgeWebView {
        if (webViewCacheStack.isEmpty()) {
            return createWebView(context)
        }
        val webView = webViewCacheStack.pop()
        val contextWrapper = webView.context as MutableContextWrapper
        contextWrapper.baseContext = context
        return webView
    }

    private fun createWebView(context: Context): BridgeWebView {
        return BridgeWebView(context)
    }

    private fun prepareHomeWebView() {
        Looper.myQueue().addIdleHandler {
            application?.let {
                val createWebView = createHomeWebView(MutableContextWrapper(it))
                webViewHomeCacheStack.push(createWebView)
            }
            false
        }
    }

    fun acquireHomeWebViewInternal(context: Context): WebView {
        if (webViewHomeCacheStack.isEmpty()) {
            return createHomeWebView(context)
        }
        val webView = webViewHomeCacheStack.pop()
        val contextWrapper = webView.context as MutableContextWrapper
        contextWrapper.baseContext = context
        return webView
    }

    private fun createHomeWebView(context: Context): WebView {
        return WebView(context)
    }
}