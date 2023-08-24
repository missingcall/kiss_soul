package com.kissspace.webview.init

import android.app.Application
import android.content.Context
import com.tencent.smtt.export.external.TbsCoreSettings
import com.tencent.smtt.sdk.QbSdk

object WebViewInitTask {
    fun init(application: Application) {
        initWebView(application)
    }

    fun prepareWebView(application: Application){
        WebViewCacheHolder.init(application)
    }

    private fun initWebView(context: Context) {
        QbSdk.setDownloadWithoutWifi(true)
        val map = mutableMapOf<String, Any>()
        map[TbsCoreSettings.TBS_SETTINGS_USE_PRIVATE_CLASSLOADER] = true
        map[TbsCoreSettings.TBS_SETTINGS_USE_SPEEDY_CLASSLOADER] = true
        map[TbsCoreSettings.TBS_SETTINGS_USE_DEXLOADER_SERVICE] = true
        QbSdk.initTbsSettings(map)
        val cb: QbSdk.PreInitCallback = object : QbSdk.PreInitCallback {
            override fun onViewInitFinished(arg0: Boolean) {
            }

            override fun onCoreInitFinished() {
            }
        }
        QbSdk.initX5Environment(context, cb)
    }
}