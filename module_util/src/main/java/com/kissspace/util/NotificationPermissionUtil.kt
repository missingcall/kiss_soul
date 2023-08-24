package com.kissspace.util

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat

fun hasNotificationPermission(context: Context): Boolean =
    NotificationManagerCompat.from(context).areNotificationsEnabled()

fun requestNotificationPermission() {
    val requestPermissionLauncher =
        (topActivity as AppCompatActivity).registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
            } else {
            }
        }
    if (Build.VERSION.SDK_INT >= 33) {
        requestPermissionLauncher.launch("android.permission.POST_NOTIFICATIONS")
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