package com.kissspace

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.startup.AppInitializer
import com.drake.brv.utils.BRV
import com.github.gzuliyujiang.oaid.DeviceIdentifier
import com.kissspace.common.base.CustomNotificationObserver
import com.kissspace.common.config.Constants
import com.kissspace.common.flowbus.Event
import com.kissspace.common.flowbus.FlowBus
import com.kissspace.common.util.*
import com.kissspace.common.util.init.LibraryDelayInitSecond
import com.kissspace.common.util.mmkv.MMKVProvider
import com.kissspace.room.service.RoomNotificationService
import com.kissspace.util.application


class App : Application() {
    override fun onCreate() {
        super.onCreate()
        if (!isMainProcess(this)) {
            return
        }
        BRV.modelId = com.kissspace.android.BR.m
        createNotificationChannel()
        FlowBus.observerEvent<Event.InitApplicationTaskEvent>(lifecycleOwner = ProcessLifecycleOwner.get()) {
            AppInitializer.getInstance(this).initializeComponent(LibraryDelayInitSecond::class.java)
            CustomNotificationObserver.initCustomNotificationObserver()
        }
        copyAssetsToStorage(application, Constants.ASSERT_ANIM_FILE)
        if (MMKVProvider.isAgreeProtocol) {
            DeviceIdentifier.register(this)
            CustomNotificationObserver.initCustomNotificationObserver()
        }
    }


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 创建前台服务通知频道
            val channel =
                NotificationChannel(
                    RoomNotificationService.FOREGROUND_NOTIFICATION_CHANNEL_ID,
                    RoomNotificationService.FOREGROUND_NOTIFICATION_CHANNEL_NAME,
                    RoomNotificationService.FOREGROUND_NOTIFICATION_CHANNEL_IMPORTANCE
                )
            val notificationManager = getSystemService(NotificationManager::class.java)
            kotlin.runCatching {
                notificationManager.createNotificationChannel(channel)
            }
        }
    }
}

