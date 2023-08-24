package com.kissspace.util

import com.blankj.utilcode.util.TimeUtils

const val YYYY_MM_DD = "yyyy-MM-dd"

fun Long.millis2String(): String = TimeUtils.millis2String(this)

fun Long.millis2String(pattern: String): String = TimeUtils.millis2String(this, pattern)

fun Long.isToday(): Boolean = TimeUtils.isToday(this)

val Long.formatDurationMS1: String
    get() {
        val minutes = this / 60
        val seconds = (this % 60)
        return "${minutes}:${seconds}"
    }

