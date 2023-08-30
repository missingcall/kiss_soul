package com.kissspace.common.util.init

import android.content.Context
import android.util.Log
import androidx.startup.Initializer
import com.didi.drouter.api.DRouter
import com.github.gzuliyujiang.oaid.DeviceIdentifier
import com.ishumei.smantifraud.SmAntiFraud
import com.kissspace.common.config.Constants
import com.tencent.bugly.crashreport.CrashReport
import com.kissspace.common.config.ConstantsKey
import com.kissspace.common.config.isReleaseServer
import com.kissspace.common.provider.IWebViewProvider
import com.kissspace.common.util.isMainProcess
import com.kissspace.common.util.mmkv.MMKVProvider
import com.kissspace.module_common.BuildConfig
import com.kissspace.util.application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @Author gaohangbo
 * @Date 2023/3/21 12:42.
 * @Describe
 */
class LibraryDelayInitSecond : Initializer<LibraryDelayInitSecond.Dependency> {
    override fun create(context: Context): Dependency {
        if (!isMainProcess(context)) {
            return Dependency()
        }
        // 初始化工作
        initBuglySdk(context)
        CoroutineScope(Dispatchers.IO).launch {
            initShuMeiSdk(context)
            getSmDeviceId()
            DRouter.build(IWebViewProvider::class.java).getService().initWebView()
            DeviceIdentifier.register(application)
            withContext(Dispatchers.Main) {
                //initDoKit()
                //初始化webview在主线程
                DRouter.build(IWebViewProvider::class.java).getService().prepareWebView()
            }
        }
        return Dependency()
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> {
        return mutableListOf(LibraryDelayInit::class.java)
    }

    class Dependency {

        init {
            initializer = true
        }

        companion object {
            var initializer: Boolean = false
        }
    }


    private fun initShuMeiSdk(context: Context) {
        val option: SmAntiFraud.SmOption = SmAntiFraud.SmOption()
        option.setAppId(
            if (isReleaseServer) {
                ConstantsKey.SM_APPID_RELEASE
            } else {
                ConstantsKey.SM_APPID_DEBUG
            }
        )
        option.setOrganization(ConstantsKey.SM_ORGANIZATION)
        option.setPublicKey(
            ConstantsKey.SM_PUBLICKEY
        )
        SmAntiFraud.create(context, option)
    }

    //采集数美的sdk
    private fun getSmDeviceId() {
        // 初始化
        if (MMKVProvider.sm_deviceId.isEmpty()) {
            val deviceId = SmAntiFraud.getDeviceId()
            MMKVProvider.sm_deviceId = deviceId
        }
    }

    private fun initBuglySdk(context: Context) {
        if (isReleaseServer) {
            CrashReport.initCrashReport(context, "29dc2117d0", false)
            CrashReport.setUserId(MMKVProvider.userId)
        } else {
            CrashReport.initCrashReport(context, "6dde796e05", true)
            CrashReport.setUserId(MMKVProvider.userId)
        }
    }
}