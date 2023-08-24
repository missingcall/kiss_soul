package com.kissspace.util

import com.blankj.utilcode.util.LogUtils
import java.lang.Exception

fun logITag(tag: String, info: String) {
    if (isAppDebug) {
        LogUtils.i(tag, info)
    }
}

fun logI(info: String) {
    if (isAppDebug) {
        LogUtils.i(info)
    }
}

fun logVTag(tag: String, info: String) {
    if (isAppDebug) {
        LogUtils.v(tag, info)
    }
}

fun logV(info: String) {
    if (isAppDebug) {
        LogUtils.v(info)
    }

}

fun logDTag(tag: String, info: String) {
    if (isAppDebug) {
        LogUtils.d(tag, info)
    }
}

fun logD(info: String) {
    if (isAppDebug) {
        LogUtils.d(info)
    }
}

fun logWTag(tag: String, info: String) {
    if (isAppDebug) {
        LogUtils.w(tag, info)
    }

}

fun logW(info: String) {
    if (isAppDebug) {
        LogUtils.w(info)
    }
}

fun logETag(tag: String, info: String) {
    if (isAppDebug) {
        LogUtils.e(tag, info)
    }
}

fun logE(info: String?) {
    if (isAppDebug) {
        LogUtils.e(info)
    }
}

fun logE(e: Exception) {
    if (isAppDebug) {
        LogUtils.e(e.message)
    }
}

fun logE(throwable: Throwable) {
    if (isAppDebug) {
        LogUtils.e(throwable.message)
    }
}

fun logATag(tag: String, info: String) {
    LogUtils.a(tag, info)
}

fun logA(info: String) {
    LogUtils.a(info)
}