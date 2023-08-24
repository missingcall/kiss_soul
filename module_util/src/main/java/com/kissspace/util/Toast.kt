
@file:Suppress("unused")

package com.kissspace.util

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.annotation.StringRes
import com.blankj.utilcode.util.ToastUtils

fun toast(text: String?) {
    ToastUtils.showShort(text)
}

fun toast(text: CharSequence?) {
    ToastUtils.showShort(text)
}


fun toast(@StringRes resId: Int) {
    ToastUtils.showShort(resId)
}

fun toastLong(text: String) {
    ToastUtils.showLong(text)
}

fun toastLong(@StringRes resId: Int) {
    ToastUtils.showLong(resId)
}

fun customToast(view: View, gravity: Int = Gravity.CENTER) {
    ToastUtils.getDefaultMaker().setGravity(gravity, 0, 0).show(view)
}


