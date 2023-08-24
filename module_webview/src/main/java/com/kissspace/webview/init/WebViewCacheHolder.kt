package com.kissspace.webview.init

import android.app.Application
import android.content.Context
import android.content.MutableContextWrapper
import android.os.Looper
import com.kissspace.webview.jsbridge.BridgeWebView
import java.util.*

object WebViewCacheHolder {
    private val webViewCacheStack = Stack<BridgeWebView>()
    private const val CACHED_WEB_VIEW_MAX_NUM = 4

    private  var application: Application?=null

    fun init(application: Application) {
        WebViewCacheHolder.application = application
        prepareWebView()
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
}