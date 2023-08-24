package com.kissspace.util

import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ResourceUtils

fun getDrawable(@DrawableRes id: Int): Drawable = ResourceUtils.getDrawable(id)

inline fun @receiver:ColorRes Int.resToColor(): Int = ColorUtils.getColor(this)

