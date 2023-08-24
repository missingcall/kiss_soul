package com.kissspace.webview.init

import android.app.Application
import android.content.Context
import androidx.startup.Initializer
import com.kissspace.webview.jsbridge.BridgeUtil

class WebViewInit : Initializer<Unit> {
    override fun create(context: Context) {
        if (BridgeUtil.isMainProcess(context)) {
            WebViewInitTask.init(context as Application)
        }
    }

    override fun dependencies() = emptyList<Class<Initializer<*>>>()


}