package com.kissspace.util

import android.R
import android.app.Activity
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.ColorInt
import androidx.fragment.app.Fragment
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.BarUtils.getStatusBarHeight


val statusBarHeight: Int get() = getStatusBarHeight()

fun Activity.setStatusBarColor(@ColorInt color: Int, isStatusBlackText: Boolean = true) {
    setColor(this, color)
    if (isStatusBlackText) {
        setStatusBarBlackText()
    } else {
        setStatusBarWhiteText()
    }
}

fun Activity.immersiveStatusBar(isStatusBlackText: Boolean = true) {
    BarUtils.transparentStatusBar(this)
    if (isStatusBlackText) {
        setStatusBarBlackText()
    } else {
        setStatusBarWhiteText()
    }
}

fun Fragment.immersiveStatusBar(isStatusBlackText: Boolean = true) {
    BarUtils.transparentStatusBar(requireActivity())
    if (isStatusBlackText) {
        requireActivity().setStatusBarBlackText()
    } else {
        requireActivity().setStatusBarWhiteText()
    }
}


fun Activity.setStatusBarBlackText(): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val window = this.window
        val decorView = window.decorView
        decorView.systemUiVisibility =
            decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        return true
    }
    return false
}

fun Activity.setStatusBarWhiteText(): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val window = this.window
        val decorView = window.decorView
        decorView.systemUiVisibility =
            decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        return true
    }
    return false
}

/**
 * 设置状态栏背景颜色
 */
private fun setColor(activity: Activity, @ColorInt color: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        activity.window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        activity.window.statusBarColor = color
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        val systemContent = activity.findViewById<ViewGroup>(R.id.content)
        val statusBarView = View(activity)
        val lp = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            statusBarHeight
        )
        statusBarView.setBackgroundColor(color)
        systemContent.getChildAt(0).fitsSystemWindows = true
        systemContent.addView(statusBarView, 0, lp)
    }

}
