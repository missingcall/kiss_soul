package com.kissspace.common.util

import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient


/**
 *@author: adan
 *@date: 2023/5/26
 *@Description:
 */
object WebViewUtil {


    fun init(paramWebView:WebView,paramJsCall: JsCall){
        val webSettings = paramWebView.settings
        paramWebView.requestFocusFromTouch()
        paramWebView.isHorizontalFadingEdgeEnabled = true
        paramWebView.isVerticalFadingEdgeEnabled = false
        paramWebView.isVerticalScrollBarEnabled = false
        webSettings.javaScriptEnabled = true
        paramWebView.addJavascriptInterface(paramJsCall, paramJsCall.getName())
        webSettings.allowFileAccessFromFileURLs = true
        webSettings.allowUniversalAccessFromFileURLs = true
        webSettings.javaScriptCanOpenWindowsAutomatically = true
        webSettings.builtInZoomControls = true
        webSettings.displayZoomControls = true
        webSettings.loadWithOverviewMode = true
        webSettings.pluginState = WebSettings.PluginState.ON
        webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH)
        webSettings.useWideViewPort = true
        webSettings.loadWithOverviewMode = true
        webSettings.setSupportZoom(false)
        webSettings.displayZoomControls = false
        webSettings.layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN
        webSettings.supportMultipleWindows()
        webSettings.setSupportMultipleWindows(true)
        webSettings.allowFileAccess = true
        webSettings.setNeedInitialFocus(true)
        webSettings.defaultTextEncodingName = "UTF-8"
        webSettings.cacheMode = WebSettings.LOAD_NO_CACHE
        paramWebView.webViewClient = CustomWebViewClient()
        paramWebView.webChromeClient = WebChromeClient()
    }

    fun loadAssets(paramWebView:WebView,path: String){
        paramWebView.loadUrl("file:///android_asset/$path")
    }


    class CustomWebViewClient : WebViewClient(){
        override fun shouldOverrideUrlLoading(
            view: WebView,
            param1String: String
        ): Boolean {
            view.loadUrl(param1String)
            return true
        }
    }

    interface JsCall{
        fun getName(): String

        @JavascriptInterface
        fun loadsuc()
    }

}