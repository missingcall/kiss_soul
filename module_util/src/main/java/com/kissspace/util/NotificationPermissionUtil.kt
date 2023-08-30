package com.kissspace.util

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.FragmentActivity
import com.permissionx.guolindev.PermissionX

fun hasNotificationPermission(context: Context): Boolean =
    NotificationManagerCompat.from(context).areNotificationsEnabled()

fun requestNotificationPermission(context: FragmentActivity, block: ((Boolean) -> Unit)?=null) {
    if (Build.VERSION.SDK_INT >= 33) {
        PermissionX.init(context).permissions(Manifest.permission.POST_NOTIFICATIONS)
            .request { allGranted, _, _ ->
                if (allGranted) {
                    block?.invoke(true)
                } else {
                    block?.invoke(false)
                }
            }
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val intent = Intent()
        intent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, topActivity.packageName)
        topActivity.startActivity(intent)
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        val intent = Intent()
        intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
        intent.putExtra("app_package", topActivity.packageName)
        intent.putExtra("app_uid", topActivity.applicationInfo.uid)
        topActivity.startActivity(intent)
    }
}