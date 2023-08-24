package com.kissspace.webview.provider

import com.didi.drouter.annotation.Service
import com.didi.drouter.api.Extend
import com.kissspace.common.provider.IWebViewProvider
import com.kissspace.util.application
import com.kissspace.webview.init.WebViewInitTask


@Service(function = [IWebViewProvider::class], cache = Extend.Cache.SINGLETON)
 class WebViewService : IWebViewProvider {
    override fun initWebView() {
        WebViewInitTask.init(application)
    }

    override fun prepareWebView() {
        WebViewInitTask.prepareWebView(application)
    }


}


