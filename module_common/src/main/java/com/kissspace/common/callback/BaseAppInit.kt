package com.kissspace.common.callback

import android.app.Application
import android.content.res.Configuration

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/11/15
 * @Description:
 *
 */
abstract class BaseAppInit {

    abstract fun onCreate(application: Application)

    abstract fun onTerminate()

    abstract fun onLowMemory()

    abstract fun configurationChanged(configuration: Configuration)
}