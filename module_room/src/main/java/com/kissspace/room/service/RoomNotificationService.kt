package com.kissspace.room.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.kissspace.room.manager.RoomServiceManager
import com.kissspace.room.ui.fragment.LiveAudioFragment

/**
 *
 * @Author: nicko
 * @CreateDate: 2023/3/22 15:08
 * @Description: 前台服务，音视频后台保活
 *
 */

class RoomNotificationService : Service() {
    companion object {
        const val FOREGROUND_NOTIFICATION_CHANNEL_ID = "foreground_channel_id"
        const val FOREGROUND_NOTIFICATION_CHANNEL_NAME = "前台服务通知"
        const val FOREGROUND_NOTIFICATION_CHANNEL_IMPORTANCE = NotificationManagerCompat.IMPORTANCE_LOW
        const val FOREGROUND_NOTIFICATION_PENDING_INTENT_REQUEST_CODE = 0x10
        const val FOREGROUND_NOTIFICATION_ID = 0xc000
    }

    private lateinit var mNotificationManager: NotificationManager

    override fun onCreate() {
        super.onCreate()
        mNotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        createNotificationForNormal()
    }

    override fun onBind(p0: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        mNotificationManager.cancel(FOREGROUND_NOTIFICATION_ID)
    }

    private fun createNotificationForNormal() {
        // 适配8.0及以上 创建渠道
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                FOREGROUND_NOTIFICATION_CHANNEL_ID,
                FOREGROUND_NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "正在房间中"
                setShowBadge(false) // 是否在桌面显示角标
            }
            mNotificationManager.createNotificationChannel(channel)
        }
        val notification = NotificationCompat.Builder(this, FOREGROUND_NOTIFICATION_CHANNEL_ID)
            .setSubText("正在房间中")
            .setSmallIcon(com.kissspace.module_common.R.mipmap.common_app_logo) // 小图标
            .setPriority(NotificationCompat.PRIORITY_DEFAULT) // 7.0 设置优先级
            .build()
        mNotificationManager.notify(FOREGROUND_NOTIFICATION_ID, notification)
        startForeground(FOREGROUND_NOTIFICATION_ID, notification)
    }

    private fun showNotification() {
        val pendingIntent = PendingIntent.getActivity(
            this,
            FOREGROUND_NOTIFICATION_PENDING_INTENT_REQUEST_CODE,
            parseIntent(),
            PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(this, FOREGROUND_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(com.kissspace.module_common.R.mipmap.common_app_logo)
            .setSubText("正在房间中").setContentText(RoomServiceManager.roomInfo?.roomTitle).build()
        mNotificationManager.notify(FOREGROUND_NOTIFICATION_ID, notification)
        startForeground(FOREGROUND_NOTIFICATION_ID, notification)
    }

    private fun parseIntent(): Intent {
        val intent = Intent(this, LiveAudioFragment::class.java)
        intent.putExtra("roomInfo", RoomServiceManager.roomInfo)
        intent.putExtra("needRefresh", true)
        return intent
    }
}