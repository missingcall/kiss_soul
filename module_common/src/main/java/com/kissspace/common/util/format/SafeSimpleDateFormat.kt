package com.kissspace.common.util.format

import java.text.SimpleDateFormat
import java.util.*

/**
 * @author xzp
 * @created on 2019/7/29.
 */
class SafeSimpleDateFormat(private val pattern: String) {

    fun format(date: Date): String = getThreadLocalInstance(pattern).format(date)
    fun parse(dateString: String): Date = getThreadLocalInstance(pattern).parse(dateString)

    fun parseOrNull(dateString: String): Date? = try {
        getThreadLocalInstance(pattern).parse(dateString)
    } catch (e: Throwable) {
        null
    }

    companion object {
        private val threadLocalDateFormat = ThreadLocal<SimpleDateFormat>()
        private fun getThreadLocalInstance(pattern: String): SimpleDateFormat {
            var instance = threadLocalDateFormat.get()
            if (instance != null) {
                instance.applyPattern(pattern)
            } else {
                instance = SimpleDateFormat(pattern, Locale.CHINA)
                threadLocalDateFormat.set(instance)
            }
            return instance
        }
    }
}