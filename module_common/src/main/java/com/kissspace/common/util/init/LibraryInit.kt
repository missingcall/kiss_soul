package com.kissspace.common.util.init

import android.app.Application
import android.content.Context
import androidx.startup.Initializer
import by.kirich1409.viewbindingdelegate.ViewBindingPropertyDelegate
import com.blankj.utilcode.util.ColorUtils
import com.didi.drouter.api.DRouter
import com.drake.brv.PageRefreshLayout
import com.drake.brv.utils.BRV
import com.drake.net.okhttp.setErrorHandler
import com.ishumei.smantifraud.SmAntiFraud
import com.kongzue.dialogx.DialogX
import com.netease.htprotect.HTProtect
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.SDKOptions
import com.netease.nimlib.sdk.auth.LoginInfo
import com.opensource.svgaplayer.SVGAParser
import com.safframework.http.interceptor.AndroidLoggingInterceptor
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.scwang.smart.refresh.header.MaterialHeader
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.tencent.bugly.crashreport.CrashReport
import com.tencent.mmkv.MMKV
import com.umeng.commonsdk.UMConfigure
import com.kissspace.common.config.ConstantsKey
import com.kissspace.common.config.UmInitConfig
import com.kissspace.common.config.isReleaseServer
import com.kissspace.common.ext.ViewClickDelay
import com.kissspace.common.http.error.GlobalErrorHandler
import com.kissspace.common.http.interceptor.HeaderInterceptor
import com.kissspace.common.provider.IWebViewProvider
import com.kissspace.common.util.isMainProcess
import com.kissspace.common.util.mmkv.MMKVProvider
import com.kissspace.module_common.BuildConfig
import com.kissspace.network.net.netInit
import com.kissspace.util.AppFileHelper
import com.kissspace.util.activityCache
import com.kissspace.util.application
import com.kissspace.util.doOnActivityLifecycle
import com.kissspace.util.getNimSdkPath
import com.kissspace.util.isNotEmpty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * @Author gaohangbo
 * @Date 2023/3/13 19:58.
 * @Describe
 */
class LibraryInit : Initializer<Unit> {
    override fun create(context: Context) {
        if (!isMainProcess(context)) {
            return
        }
        application = context as Application
        application.doOnActivityLifecycle(
            onActivityCreated = { activity, _ ->
                activityCache.add(activity)
            },
            onActivityDestroyed = { activity ->
                activityCache.remove(activity)
            }
        )
        MMKV.initialize(context)
        netInit(MMKVProvider.baseUrl) {
            writeTimeout((30 * 1000).toLong(), TimeUnit.MILLISECONDS)
            readTimeout((30 * 1000).toLong(), TimeUnit.MILLISECONDS)
            connectTimeout((15 * 1000).toLong(), TimeUnit.MILLISECONDS)
            addInterceptor(HeaderInterceptor())
            addInterceptor(AndroidLoggingInterceptor.build())
            setErrorHandler(GlobalErrorHandler())
        }
        initSmartRefreshLayout()
        preInitUmeng(context)
        AppFileHelper.initStoragePathInternal(context)
        initYidun(context)
        if (MMKVProvider.isAgreeProtocol) {
            //如果用户同意后，直接对sdk初始化
            NIMClient.init(context, getLoginInfo(), getNetEaseOptions(context))
            //初始化友盟
            UmInitConfig.initUM(context)
            initBuglySdk(context)
            CoroutineScope(Dispatchers.IO).launch {
                initShuMeiSdk(context)
                getSmDeviceId()
                DRouter.build(IWebViewProvider::class.java).getService().initWebView()
                withContext(Dispatchers.Main) {
                    //初始化webview在主线程
                    DRouter.build(IWebViewProvider::class.java).getService().prepareWebView()
                }
            }
        } else {
            //如果用户未同意，先调用config
            NIMClient.config(context, getLoginInfo(), getNetEaseOptions(context))
        }
        ViewBindingPropertyDelegate.strictMode = false
        CoroutineScope(Dispatchers.IO).launch {
            DialogX.init(context)
            SVGAParser.shareParser().init(context)
            BRV.clickThrottle = ViewClickDelay.SPACE_TIME
        }
    }

    private fun initYidun(context: Context) {
        HTProtect.init(context,ConstantsKey.NETEASE_RISK_MANAGEMENT,
            { p0, p1 -> },null)
    }


    private fun preInitUmeng(context: Context) {
        /**
         * 为满足工信部合规要求，请确保按照合规指南进行预初始化
         * https://developer.umeng.com/docs/119267/detail/182050
         */
        UMConfigure.preInit(
            context, if (isReleaseServer) {
                ConstantsKey.UMENGKEY_RELEASE_KEY
            } else {
                ConstantsKey.UMENGKEY_DEBUG_KEY
            }, "Umeng"
        )

    }

    private fun getNetEaseOptions(context: Context): SDKOptions {
        val options = SDKOptions()
        options.appKey =
            if (isReleaseServer) ConstantsKey.NETEASE_APP_KEY_RELEASE else ConstantsKey.NETEASE_APP_KEY_TEST
        options.useXLog = false
        //异步初始化sdk
        options.asyncInitSDK = true
        options.reducedIM = true
        options.preloadAttach = true
        options.disableAwake = true
        options.sdkStorageRootPath = getNimSdkPath(context)
        return options
    }

    private fun initSmartRefreshLayout() {
        SmartRefreshLayout.setDefaultRefreshHeaderCreator { context, _ ->
            MaterialHeader(context).setColorSchemeColors(ColorUtils.getColor(com.kissspace.module_common.R.color.color_6C74FB))
        }
        SmartRefreshLayout.setDefaultRefreshFooterCreator { context, _ ->
            ClassicsFooter(context).apply {
                setDrawableSize(20f)
                ClassicsFooter.REFRESH_FOOTER_NOTHING = "我也是有底线的"
            }
        }
        PageRefreshLayout.preloadIndex = 1
    }

    private fun getLoginInfo(): LoginInfo? {
        var loginInfo: LoginInfo? = null
        //获取之前登录成功保存的accid、token信息。
        val account: String = MMKVProvider.accId
        val netEaseToken: String = MMKVProvider.loginResult?.netEaseToken.orEmpty()
        if (account.isNotEmpty() && netEaseToken.isNotEmpty()) {
            //之前已经登录过，可以走自动登录。
            loginInfo = LoginInfo(account, netEaseToken)
        }
        return loginInfo
    }

    override fun dependencies() = emptyList<Class<Initializer<*>>>()
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
        if (com.kissspace.module_common.BuildConfig.DEBUG && !isReleaseServer) {
            CrashReport.initCrashReport(context, "3d1f5ff003", com.kissspace.module_common.BuildConfig.DEBUG)
            CrashReport.setUserId(MMKVProvider.userId)
        } else {
            CrashReport.initCrashReport(context, "cd01fd0ecc", com.kissspace.module_common.BuildConfig.DEBUG)
            CrashReport.setUserId(MMKVProvider.userId)
        }
    }

}