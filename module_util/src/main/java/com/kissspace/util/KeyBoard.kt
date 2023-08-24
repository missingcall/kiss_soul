package com.kissspace.util

import android.app.Activity
import android.view.MotionEvent
import android.view.View
import androidx.fragment.app.Fragment
import com.blankj.utilcode.util.KeyboardUtils


fun showKeyboard() {
    KeyboardUtils.showSoftInput()
}

fun View.hideKeyboard() {
    KeyboardUtils.hideSoftInput(this)
}

fun Activity.hideKeyboard() {
    KeyboardUtils.hideSoftInput(this)
}

fun Fragment.hideKeyboard() {
    if (isAdded && !isDetached) {
        KeyboardUtils.hideSoftInput(this.requireActivity())
    }
}


