package com.kissspace.util

import android.app.Application
import android.content.Context
import androidx.startup.Initializer

class AppInitializer: Initializer<Unit> {

    override fun create(context: Context) {
        application = context as Application
        application.doOnActivityLifecycle(
            onActivityCreated = { activity, _ ->
                activityCache.add(activity)
            },
            onActivityDestroyed = { activity ->
                activityCache.remove(activity)
            }
        )
    }

    override fun dependencies() = emptyList<Class<Initializer<*>>>()
}
