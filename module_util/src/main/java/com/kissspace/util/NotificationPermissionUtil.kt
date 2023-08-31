package com.kissspace.util

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.permissionx.guolindev.PermissionX

fun hasNotificationPermission(context: Context): Boolean =
    NotificationManagerCompat.from(context).areNotificationsEnabled()

fun requestNotificationPermission(fragment: Fragment?=null,activity:FragmentActivity?=null,block: ((Boolean) -> Unit)?=null) {
    if (Build.VERSION.SDK_INT >= 33) {
        if (fragment != null) {
            PermissionX.init(fragment).permissions(Manifest.permission.POST_NOTIFICATIONS)
                .request { allGranted, _, _ ->
                    if (allGranted) {
                        block?.invoke(true)
                    } else {
                        block?.invoke(false)
                    }
                }
        }else{
            if (activity != null) {
                PermissionX.init(activity).permissions(Manifest.permission.POST_NOTIFICATIONS)
                    .request { allGranted, _, _ ->
                        if (allGranted) {
                            block?.invoke(true)
                        } else {
                            block?.invoke(false)
                        }
                    }
            }
        }
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val intent = Intent()
        intent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, topActivity?.packageName)
        topActivity?.startActivity(intent)
    } else {
        val intent = Intent()
        intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
        intent.putExtra("app_package", topActivity?.packageName)
        intent.putExtra("app_uid", topActivity?.applicationInfo?.uid)
        topActivity?.startActivity(intent)
    }
}