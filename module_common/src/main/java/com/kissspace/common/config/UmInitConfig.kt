package com.kissspace.common.config

import android.content.Context
import com.umeng.commonsdk.UMConfigure
import com.umeng.socialize.PlatformConfig
import com.kissspace.common.flowbus.Event
import com.kissspace.common.flowbus.FlowBus

object UmInitConfig {
    fun initUM(context: Context) {
        UMConfigure.init(
            context,
            if (isReleaseServer) {
                ConstantsKey.UMENGKEY_RELEASE_KEY
            } else {
                ConstantsKey.UMENGKEY_DEBUG_KEY
            },
            "umeng",
            UMConfigure.DEVICE_TYPE_PHONE,
            ""
        )
        FlowBus.post(Event.InitOnKeyLoginEvent)
        //微信  appid,appkey
        PlatformConfig.setWeixin(ConstantsKey.WECHAT_APPID, ConstantsKey.WECHAT_APPSECRET)
        PlatformConfig.setWXFileProvider(context.packageName + ".FileProvider")

    }

}