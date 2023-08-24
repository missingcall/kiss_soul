package com.kissspace.common.util.init

import android.content.Context
import androidx.startup.Initializer
import com.netease.nimlib.sdk.NIMClient
import com.kissspace.common.config.UmInitConfig
import com.kissspace.common.util.isMainProcess
import com.kissspace.util.logE

/**
 * @Author gaohangbo
 * @Date 2023/3/13 19:58.
 * @Describe
 */
class LibraryDelayInit : Initializer<LibraryDelayInit.Dependency> {
    override fun create(context: Context): Dependency {
        if (!isMainProcess(context)) {
            return Dependency()
        }
        // 初始化工作
        UmInitConfig.initUM(context)
        NIMClient.initSDK()
        return Dependency()
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> {
        return mutableListOf()
    }

    class Dependency {

    }


}