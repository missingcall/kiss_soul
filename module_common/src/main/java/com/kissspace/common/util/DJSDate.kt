package com.kissspace.common.util

import com.blankj.utilcode.util.ToastUtils
import com.kissspace.common.config.Constants
import com.kissspace.common.util.format.SafeSimpleDateFormat
import com.kissspace.common.util.format.DateFormat
import com.kissspace.common.util.format.DateFormat.MM_DD
import com.kissspace.common.util.format.DateFormat.YYYY_MM_DD
import com.kissspace.common.util.format.DateFormat.YYYY_MM_DD_HH_MM_SS
import com.kissspace.util.isNullOrZero
import com.kissspace.util.logE
import com.kissspace.util.orZero
import java.util.*
import kotlin.math.roundToInt

/**
 * formatXXX格式化成对应的字符串
 * toXXX转换成对应的类型
 * @author Loja
 * @created on 11/30/18.
 */
object DJSDate {
    /**
     * 当前时间
     */
    val now
        get() = System.currentTimeMillis()
    val formatNow
        get() = now.formatDate(YYYY_MM_DD_HH_MM_SS)

    /**
     * 今天，不包含时分秒
     */
    val today
        get() = Calendar.getInstance().apply {
            timeInMillis = now
            clearBelowHour()
        }.timeInMillis

    val tomorrow
        get() = Calendar.getInstance().apply {
            timeInMillis = now
            add(Calendar.DAY_OF_YEAR, 1)
            clearBelowHour()
        }.timeInMillis

    /**
     * 包含最后一天
     * 结束时间要大于等于开始时间
     * @param excludeBeginDay 是否包含第一天
     */
    fun getDays(
        beginTime: Long?,
        endTime: Long?,
        excludeBeginDay: Boolean = false
    ): Int = if (beginTime != null && endTime != null) {
        (if (beginTime <= endTime) ((endTime.toDayEnd() - beginTime.toDayBegin()).toDouble() / Constants.DAY_MILLIS).roundToInt() else 0).let {
            if (excludeBeginDay && it >= 1) it - 1 else it
        }
    } else 0

    /**
     * 如果beginTime和endTime相等，返回0
     */
    fun getDaysEqualZero(
        beginTime: Long?,
        endTime: Long?,
        excludeBeginDay: Boolean = false
    ): Int = if (beginTime != null && endTime != null) {
        (if (beginTime < endTime) ((endTime.toDayEnd() - beginTime.toDayBegin()).toDouble() / Constants.DAY_MILLIS).roundToInt() else 0).let {
            if (excludeBeginDay && it >= 1) it - 1 else it
        }
    } else 0

    /**
     * 从现在开始剩余天数
     */
    fun getDaysFromNow(
        beginTime: Long?,
        endTime: Long?,
        excludeBeginDay: Boolean = false
    ): Int {
        return if (beginTime != null && endTime != null) {
            (if (endTime >= beginTime) {
                if (beginTime > now) getDays(beginTime, endTime)
                else getDays(now, endTime)
            } else 0).let {
                if (excludeBeginDay && it >= 1) it - 1 else it
            }
        } else 0
    }

    fun age(
        age: Int?,
        months: Int?,
        showMonthAge: Int = 3
    ) = (if (age.orZero() > 0) "${age}岁" else "") +
            (if (age.orZero() < showMonthAge && months.orZero() > 0) "${months}个月" else "")

    fun formatMinutePeriod(
        beginMinutes: Int?,
        endMinutes: Int?,
        format: SafeSimpleDateFormat = DateFormat.HH_MM,
        split: String? = "-",
    ): String = formatMinutePeriod(beginMinutes?.toLong(), endMinutes?.toLong(), format, split)

    fun formatMinutePeriod(
        beginMinutes: Long?,
        endMinutes: Long?,
        format: SafeSimpleDateFormat = DateFormat.HH_MM,
        split: String? = "-",
    ): String = if (beginMinutes == null && endMinutes == null) ""
    else "${beginMinutes.minuteFormatDate(format)} $split ${endMinutes.minuteFormatDate(format)}"

    fun formatPeriod(
        beginTime: Long?,
        endTime: Long?,
        format: SafeSimpleDateFormat = DateFormat.YYYY_MM_DD_HH_MM_SLASH
    ): String = when {
        beginTime == null && endTime == null -> ""
        isSameDay(beginTime, endTime) -> "${beginTime.formatDate(format)} - ${
            endTime.formatDate(
                DateFormat.HH_MM
            )
        }"
        else -> "${beginTime.formatDate(format)} - ${endTime.formatDate(format)}"
    }

    /**
     * 2000/01/01-2000/12/12
     */
    fun formatPeriodDate(
        beginTime: Long?,
        endTime: Long?,
        format: SafeSimpleDateFormat = DateFormat.YYYY_MM_DD_SLASH
    ): String = "${beginTime.formatDate(format)} - ${endTime.formatDate(format)}"

    /**
     * 判定这两天是否为同一天
     */
    fun isSameDay(
        calendar1: Calendar,
        calendar2: Calendar
    ): Boolean = calendar1.get(Calendar.DAY_OF_YEAR) == calendar2.get(Calendar.DAY_OF_YEAR)
            && calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR)

    /**
     * 判定这两天是否为同一天
     */
    fun isSameDay(
        time1: Long?,
        time2: Long?
    ): Boolean = isSameDay(Calendar.getInstance().apply { timeInMillis = time1.orZero() },
        Calendar.getInstance().apply { timeInMillis = time2.orZero() })

    fun isSameDay(
        date1: Date,
        date2: Date
    ): Boolean = isSameDay(Calendar.getInstance().apply { time = date1 },
        Calendar.getInstance().apply { time = date2 })

    /**
     * end要比start晚
     * 返回这两天相隔了多少天
     */
    fun betweenDays(
        start: Calendar,
        end: Calendar
    ): Int {
        var result = end.get(Calendar.DAY_OF_YEAR) - start.get(Calendar.DAY_OF_YEAR)
        val startCopy = Calendar.getInstance().apply { timeInMillis = start.timeInMillis }
        while (startCopy.get(Calendar.YEAR) < end.get(Calendar.YEAR)) {
            result += startCopy.getActualMaximum(Calendar.DAY_OF_YEAR)
            startCopy.add(Calendar.YEAR, 1)
        }
        return if (result >= 0) result else -1
    }

    /**
     * 当前分钟数
     */
    fun getCurrentMinutes(calendar: Calendar? = Calendar.getInstance()): Int {
        return calendar?.get(Calendar.MINUTE).orZero() + calendar?.get(Calendar.HOUR_OF_DAY)
            .orZero() * 60
    }


    fun getTimeInMillis(year: Int, month: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month - 1)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.clearBelowHour()
        return calendar.timeInMillis
    }

    fun getTimeInMillis(year: Int, month: Int, day: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month - 1)
        calendar.set(Calendar.DAY_OF_MONTH, day)
        calendar.clearBelowHour()
        return calendar.timeInMillis
    }

    fun getHourTimeInMillis(hour: Int, minute: Int, second: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, second)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    /**
     * 当天时间的毫秒
     */
    fun getTimeHourMinutesInMillis(time: Long): Long {
        val selectCalendar = Calendar.getInstance().apply { timeInMillis = time }
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, selectCalendar.get(Calendar.YEAR))
        calendar.set(Calendar.MONTH, selectCalendar.get(Calendar.MONTH))
        calendar.set(Calendar.DAY_OF_MONTH, selectCalendar.get(Calendar.DAY_OF_MONTH))
        calendar.clearBelowHour()
        return selectCalendar.timeInMillis - calendar.timeInMillis
    }


    fun getTimeInMillis(dateString: String, format: SafeSimpleDateFormat): Long {
        return try {
            format.parse(dateString)?.time ?: 0
        } catch (e: Exception) {
            logE(e)
            0
        }
    }

    fun getDateRange(
        beginTime: Long,
        endTime: Long
    ): String {
        val format =
            if (beginTime.isThisYear && endTime.isThisYear) DateFormat.MM_DD_DOT else DateFormat.YYYY_MM_DD_DOT
        return if (isSameDay(beginTime, endTime)) {
            beginTime.formatDate(format)
        } else {
            "${beginTime.formatDate(format)}~${endTime.formatDate(format)}"
        }
    }

    fun getComposeTime(
        date: Long?,
        minute: Int?
    ): Long = date.orZero().clearBelowHour() + minute.orZero() * Constants.MINUTE_MILLIS
    
    fun checkBeforeToday(time: Long?): Boolean {
        if (time.orZero() > now.toDayEnd()) {
            ToastUtils.showShort("只能选择今天及以前的日期")
            return false
        }
        return true
    }

    fun formatTimeRange(start: Long, end: Long): String = when {
        start == now.toMonthBegin() && end == now.toDayEnd() -> "本月"
        start == now.addMonths(-1).toMonthBegin() && end == now.addMonths(-1).toMonthEnd() -> "上月"
        isSameDay(start, end) -> if (start.isThisYear && end.isThisYear) {
            start.formatDate(DateFormat.MM_DD_DOT)
        } else {
            start.formatDate(DateFormat.YYYY_MM_DD_DOT)
        }
        else -> {
            if (start.isThisYear && end.isThisYear) {
                "${start.formatDate(DateFormat.MM_DD_DOT)}~${
                    end.formatDate(
                        DateFormat.MM_DD_DOT
                    )
                }"
            } else {
                "${start.formatDate(DateFormat.YYYY_MM_DD_DOT)}~${
                    end.formatDate(
                        DateFormat.YYYY_MM_DD_DOT
                    )
                }"
            }
        }
    }


    fun plus(from: Long, year: Int = 0, month: Int = 0, day: Int = 0): Long {
        val calendar = Calendar.getInstance()
        with(calendar) {
            time = Date(from)
            add(Calendar.YEAR, year)
            add(Calendar.MONTH, month)
            add(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    /**
     * 取整，向上取半小时
     * interval：取整毫秒数
     */
    fun roundNumber(time: Long, interval: Long? = null): Long {
        val selectCalendar = Calendar.getInstance().apply { timeInMillis = time }
        selectCalendar.set(Calendar.SECOND, 0)
        selectCalendar.set(Calendar.MILLISECOND, 0)
        val minTemp = interval ?: 30 * 60 * 1000
        val resultTime = selectCalendar.timeInMillis + minTemp
        val remainder = resultTime % minTemp
        if (remainder != 0L) {
            selectCalendar.timeInMillis = resultTime - remainder
        }
        return selectCalendar.timeInMillis
    }

    /**
     * 获取[date]这个月的第一天
     */
    fun getTheFirstDayOfMonth(date: Date): Date {
        return Calendar.getInstance().apply {
            time = date
            set(Calendar.DAY_OF_MONTH, 1)
            clearBelowHour()
        }.time
    }

    /**
     * 获取这个月的最后一天
     */
    fun getTheLastDayOfMonth(date: Date): Date {
        val calendar = Calendar.getInstance().apply {
            time = date
            //加一个月
            add(Calendar.MONTH, 1)
            //跳到1号
            set(Calendar.DAY_OF_MONTH, 1)
            //1号0点
            clearBelowHour()
            //减1毫秒，跳到前一天的结束23:59:59.999
            add(Calendar.MILLISECOND, -1)
        }
        return calendar.time
    }

    /**
     * 判断[date1]和[date2]是不是同一个月
     */
    fun isSameMonth(date1: Long, date2: Long): Boolean {
        val c1 = Calendar.getInstance().apply { timeInMillis = date1 }
        val c2 = Calendar.getInstance().apply { timeInMillis = date2 }
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) && c1.get(Calendar.MONTH) == c2.get(
            Calendar.MONTH
        )
    }

    /**
     * 判断[date1]和[date2]是不是同一个月
     */
    fun isSameMonth(date1: Date, date2: Date): Boolean {
        val c1 = Calendar.getInstance().apply { time = date1 }
        val c2 = Calendar.getInstance().apply { time = date2 }
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) && c1.get(Calendar.MONTH) == c2.get(
            Calendar.MONTH
        )
    }

    /**
     * 判断[date1]和[date2]是不是同一周
     */
    fun isSameWeek(date1: Date, date2: Date): Boolean {
        val c1 = Calendar.getInstance().apply { time = date1 }
        val c2 = Calendar.getInstance().apply { time = date2 }
        return c1.timeInMillis.toWeekBegin() == c2.timeInMillis.toWeekBegin()
    }

    /**
     * 判断[date1]和[date2]是不是同一年
     */
    fun isSameYear(date1: Date?, date2: Date?): Boolean {
        if (date1 == null || date2 == null) return false
        val c1 = Calendar.getInstance().apply { time = date1 }
        val c2 = Calendar.getInstance().apply { time = date2 }
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)
    }



}

fun Long.minuteFormatDate(format: SafeSimpleDateFormat = DateFormat.HH_MM): String =
    (this * Constants.MINUTE_MILLIS + DJSDate.today).formatDate(format)

fun Long?.minuteFormatDate(format: SafeSimpleDateFormat = DateFormat.HH_MM): String =
    this?.minuteFormatDate(format).orEmpty()

fun Long.formatDate(format: SafeSimpleDateFormat = YYYY_MM_DD): String {
    try {
        return when {
            this == 0L || this == -1L -> ""
            else -> format.format(Date(this))
        }
    } catch (e: Exception) {
        logE(e)
    }
    return "无效时间"
}

fun Long?.formatDate(format: SafeSimpleDateFormat = YYYY_MM_DD): String = this?.formatDate(
    format
).orEmpty()

fun Long?.formatLogDate(): String = this?.formatDate(YYYY_MM_DD_HH_MM_SS).orEmpty()

fun Long?.formatDate(format: SafeSimpleDateFormat = YYYY_MM_DD, default: String): String =
    this?.formatDate(
        format
    ) ?: default


fun Long?.formatDateRange(end: Long?, format: SafeSimpleDateFormat = DateFormat.YYYY_MM_DD_DOT) =
    "${this.formatDate(format)}-${end.formatDate(format)}"

/**
 * 实岁
 */
fun Long.toAge() = DJSDate.now.let { now ->
    if (this > now || this == 0L || this == -1L) null
    else now.toYear() - this.toYear()
}

/**
 * 如果addDays为负数，则返回当天00:00:00点，当天开始
 */
fun Long.toAfterDayEnd(addDays: Int = 0): Long = Calendar.getInstance().let {
    it.timeInMillis = this
    it.clearBelowHour()
    if (addDays >= 0) {
        it.add(Calendar.DAY_OF_YEAR, addDays + 1)
        it.timeInMillis - Constants.SECOND_MILLIS
    } else it.timeInMillis
}

/**
 * 当前日期00:00:00点到结束日期23:59:59
 */
fun Long.toDayEnd(addDays: Int = 0): Long = if (this <= 0) this
else Calendar.getInstance().let {
    it.timeInMillis = this
    it.clearBelowHour()
    it.add(Calendar.DAY_OF_YEAR, addDays + 1)
    it.timeInMillis - Constants.SECOND_MILLIS
}

/**
 * 与第二天相差只有 1 millisecond； toDayEnd 相差 1000 millisecond。
 * */
fun Long.toDayEnd2(addDays: Int = 0): Long = if (this <= 0) this
else Calendar.getInstance().let {
    it.timeInMillis = this
    it.clearBelowHour()
    it.add(Calendar.DAY_OF_YEAR, addDays + 1)
    it.timeInMillis - 1
}

fun Long.toDayBegin(): Long = if (this <= 0) this
else Calendar.getInstance().let {
    it.timeInMillis = this
    it.clearBelowHour()
    it.timeInMillis
}

fun Long.toDayBegin2(addDays: Int = 0): Long = if (this <= 0) this
else Calendar.getInstance().let {
    it.timeInMillis = this
    it.add(Calendar.DATE, addDays)
    it.clearBelowHour()
    it.timeInMillis
}

fun Long.toWeekBegin(): Long = Calendar.getInstance().let {
    it.firstDayOfWeek = Calendar.MONDAY
    it.timeInMillis = this
    it.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
    it.clearBelowHour()
    it.timeInMillis
}

fun Long.toWeekEnd(addWeeks: Int = 0): Long = Calendar.getInstance().let {
    it.firstDayOfWeek = Calendar.MONDAY
    it.timeInMillis = this
    it.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
    it.clearBelowHour()
    it.add(Calendar.WEEK_OF_YEAR, addWeeks + 1)
    it.timeInMillis - Constants.SECOND_MILLIS
}

fun Long.toMonthBegin(): Long = Calendar.getInstance().let {
    it.timeInMillis = this
    it.set(Calendar.DAY_OF_MONTH, 1)
    it.clearBelowHour()
    it.timeInMillis
}

/**
 * 如果addMonths为负数，则返回当天00:00:00点
 */
fun Long.toAfterMonthEnd(addMonths: Int = 0): Long = Calendar.getInstance().let {
    it.timeInMillis = this
    it.clearBelowHour()
    if (addMonths >= 0) {
        it.set(Calendar.DAY_OF_MONTH, 1)
        it.add(Calendar.MONTH, addMonths + 1)
        it.timeInMillis - Constants.SECOND_MILLIS
    } else it.timeInMillis
}

fun Long.toMonthEnd(addMonths: Int = 0): Long = Calendar.getInstance().let {
    it.timeInMillis = this
    it.set(Calendar.DAY_OF_MONTH, 1)
    it.clearBelowHour()
    it.add(Calendar.MONTH, addMonths + 1)
    it.timeInMillis - Constants.SECOND_MILLIS
}

fun Long.addMonth(addMonths: Int = 1) = Calendar.getInstance().let {
    it.timeInMillis = this
    it.add(Calendar.MONTH, addMonths)
    it.timeInMillis
}

/**
 * 月份设置为month-1, month取值1-12
 */
fun Long.toMonth(month: Int): Long = Calendar.getInstance().let {
    it.timeInMillis = this
    it.set(Calendar.MONTH, month - 1)
    it.timeInMillis
}

/**
 * 添加addMonths个月，设置到前一天的23:59:59，
 */
fun Long.addMonthToYesterdayEnd(addMonths: Int = 1): Long = Calendar.getInstance().let {
    it.timeInMillis = this
    it.clearBelowHour()
    it.add(Calendar.MONTH, addMonths)
    it.timeInMillis - Constants.SECOND_MILLIS
}

fun Long.addDayToYesterdayEnd(addDays: Int = 1): Long = Calendar.getInstance().let {
    it.timeInMillis = this
    it.clearBelowHour()
    it.add(Calendar.DAY_OF_YEAR, addDays)
    it.timeInMillis - Constants.SECOND_MILLIS
}

fun Long.toYear() = Calendar.getInstance().let {
    it.timeInMillis = this
    it.get(Calendar.YEAR)
}

fun Long.toMonth() = Calendar.getInstance().let {
    it.timeInMillis = this
    it.get(Calendar.MONTH) + 1
}

fun Long.toDayOfMonth() = Calendar.getInstance().let {
    it.timeInMillis = this
    it.get(Calendar.DAY_OF_MONTH)
}

fun Long?.formatYear() = this?.toYear()?.toString().orEmpty()

fun Long?.formatMonth() = this?.toMonth()?.toString().orEmpty()

fun Long.toHour() = Calendar.getInstance().let {
    it.timeInMillis = this
    it.get(Calendar.HOUR_OF_DAY)
}

fun Long.toMinute() = Calendar.getInstance().let {
    it.timeInMillis = this
    it.get(Calendar.MINUTE)
}

fun Long?.toMinuteOfDay() = Calendar.getInstance().let {
    it.timeInMillis = this.orZero()
    it.get(Calendar.MINUTE) + it.get(Calendar.HOUR_OF_DAY) * 60
}

fun Long?.orNow() = this ?: DJSDate.now

/**
 * 从当前时间开始往后推算的周几week时间
 * @param week Calendar.MONDAY...Calendar.SUNDAY
 */
fun Long.toWeekTime(week: Int?) = Calendar.getInstance().let {
    if (this > 0) {
        it.timeInMillis = this
    }
    it.clearBelowHour()
    val startWeek = it.get(Calendar.DAY_OF_WEEK)
    var deta = (week ?: Calendar.MONDAY) - startWeek
    if (deta < 0) {
        deta += 7
    }
    it.add(Calendar.DAY_OF_WEEK, deta)
    it.timeInMillis
}

fun Long.toDayOfWeek() = Calendar.getInstance().let {
    it.timeInMillis = this
    it.get(Calendar.DAY_OF_WEEK)
}

fun Long.addYears(count: Int? = 1): Long = Calendar.getInstance().let {
    it.timeInMillis = this
    count?.let { count ->
        it.add(Calendar.YEAR, count)
    }
    it.timeInMillis
}

fun Long.addMonths(count: Int? = 1): Long = Calendar.getInstance().let {
    it.timeInMillis = this
    count?.let { count ->
        it.add(Calendar.MONTH, count)
    }
    it.timeInMillis
}

fun Long.addHours(count: Int? = 1): Long = Calendar.getInstance().let {
    it.timeInMillis = this
    count?.let { count ->
        it.add(Calendar.HOUR, count)
    }
    it.timeInMillis
}

fun Long.addMinute(count: Int? = 1): Long = Calendar.getInstance().let {
    it.timeInMillis = this
    count?.let { count ->
        it.add(Calendar.MINUTE, count)
    }
    it.timeInMillis
}

fun Long.addDays(count: Int = 1): Long = Calendar.getInstance().let {
    it.timeInMillis = this
    it.add(Calendar.DAY_OF_YEAR, count)
    it.timeInMillis
}

fun Long.addWeeks(count: Int = 1): Long = Calendar.getInstance().let {
    it.timeInMillis = this
    it.add(Calendar.WEEK_OF_YEAR, count)
    it.timeInMillis
}

/**
 * @param field Calendar.YEAR
 */
fun Long.addField(field:Int, count: Int = 1): Long = Calendar.getInstance().let {
    it.timeInMillis = this
    it.add(field, count)
    it.timeInMillis
}

fun Long.combineTime(
    hour: Int,
    minute: Int
) = Calendar.getInstance()
    .let {
        it.timeInMillis = this
        it.set(Calendar.HOUR_OF_DAY, hour)
        it.set(Calendar.MINUTE, minute)
        it.set(Calendar.SECOND, 0)
        it.set(Calendar.MILLISECOND, 0)
        it.timeInMillis
    }

fun Calendar.clearBelowHour() = apply {
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}

fun Long.clearBelowHour() = Calendar.getInstance().let {
    it.timeInMillis = this
    it.clearBelowHour()
    it.timeInMillis
}

fun Long.clearSeconds() = Calendar.getInstance().let {
    it.timeInMillis = this
    it.set(Calendar.SECOND, 0)
    it.set(Calendar.MILLISECOND, 0)
    it.timeInMillis
}

val Long.isToday: Boolean
    get() {
        val now = Calendar.getInstance()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = this
        return now.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                now.get(Calendar.MONTH) == calendar.get(Calendar.MONTH) &&
                now.get(Calendar.DAY_OF_MONTH) == calendar.get(Calendar.DAY_OF_MONTH)
    }

val Long.isYesterday: Boolean
    get() {
        val yesterday = Calendar.getInstance()
            .apply {
                add(Calendar.DAY_OF_MONTH, -1)
            }
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = this
        return yesterday.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                yesterday.get(Calendar.MONTH) == calendar.get(Calendar.MONTH) &&
                yesterday.get(Calendar.DAY_OF_MONTH) == calendar.get(Calendar.DAY_OF_MONTH)
    }

val Long.isTomorrow: Boolean
    get() {
        val tomorrow = Calendar.getInstance()
            .apply {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = this
        return tomorrow.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                tomorrow.get(Calendar.MONTH) == calendar.get(Calendar.MONTH) &&
                tomorrow.get(Calendar.DAY_OF_MONTH) == calendar.get(Calendar.DAY_OF_MONTH)
    }

val Long.isThisMonth: Boolean
    get() {
        val now = Calendar.getInstance()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = this
        return calendar.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                calendar.get(Calendar.MONTH) == now.get(Calendar.MONTH)
    }

val Long.isThisYear: Boolean
    get() {
        val now = Calendar.getInstance()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = this
        return calendar.get(Calendar.YEAR) == now.get(Calendar.YEAR)
    }

/**
 * 是否为未来的时间
 */
val Long.isFuture: Boolean
    get() = this > DJSDate.now
val Long.isPast: Boolean
    get() = this < DJSDate.now
val Long.isBeforeToday: Boolean
    get() = this < DJSDate.today
val Long.isAfterTomorrow: Boolean
    get() = this >= DJSDate.tomorrow

/**
 * 将 minute 转换为 xx:xx 的格式
 * */
val Long.formatDayTime: String
    get() {
        val hours = this / 60
        val minutes = this % 60
        return "%0d:%0d".format(hours, minutes)
    }

/**
 * 将 minute 转换为 xx:xx 的格式
 * */
val Int.formatDayTime: String
    get() {
        val hours = this / 60
        val minutes = this % 60
        return "%02d:%02d".format(hours, minutes)
    }

val Long.formatComfortTime: String
    get() {
        val past = Math.abs(DJSDate.now - this)
        return when {
            past < Constants.MINUTE_MILLIS && isPast -> "刚刚"
            past < Constants.HOUR_MILLIS && isPast -> "${past / Constants.MINUTE_MILLIS}分钟前"
            this.isToday -> "今天${formatDate(DateFormat.HH_MM)}"
            this.isYesterday -> "昨天${formatDate(DateFormat.HH_MM)}"
            this.isThisYear -> formatDate(DateFormat.MM_DD_HH_MM_CN)
            else -> formatDate(DateFormat.YYYY_MM_DD_HH_MM_CN)
        }
    }

val Long.formatComfortTimeV5: String
    get() {
        val past = Math.abs(DJSDate.now - this)
        return when {
            past < Constants.MINUTE_MILLIS && isPast -> "刚刚"
            past < Constants.HOUR_MILLIS && isPast -> "${past / Constants.MINUTE_MILLIS}分钟前"
            this.isToday -> "今天${formatDate(DateFormat.HH_MM)}"
            this.isYesterday -> "昨天${formatDate(DateFormat.HH_MM)}"
            this.isThisYear -> formatDate(DateFormat.MM_DD_HH_MM)
            else -> formatDate(DateFormat.YYYY_MM_DD_HH_MM)
        }
    }

val Long.formatSimpleComfortTime: String
    get() {
        return when {
            this.isToday -> "今天${formatDate(DateFormat.HH_MM)}"
            this.isYesterday -> "昨天${formatDate(DateFormat.HH_MM)}"
            this.isThisYear -> formatDate(DateFormat.MM_DD_HH_MM)
            else -> formatDate(DateFormat.YYYY_MM_DD_HH_MM)
        }
    }
val Long.formatSimpleComfortTime2: String
    get() {
        return when {
            this.isToday -> "今天${formatDate(DateFormat.HH_MM_SS)}"
            this.isYesterday -> "昨天${formatDate(DateFormat.HH_MM_SS)}"
            this.isThisYear -> formatDate(DateFormat.MM_DD_HH_MM_SS)
            else -> formatDate(DateFormat.YYYY_MM_DD_HH_MM_SS)
        }
    }

val Long.formatSimpleComfortDate: String
    get() {
        return when {
            this.isToday -> "今天"
            this.isYesterday -> "昨天"
            this.isThisYear -> formatDate(MM_DD)
            else -> formatDate(YYYY_MM_DD)
        }
    }

val Long.formatComfortYearMonth: String
    get() {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = this
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        return when {
            !isThisYear -> "${year}年${month}月"
            isThisMonth -> "本月"
            else -> "${month}月"
        }
    }

val Long.formatComfortTime2: String
    get() = when {
        this.isToday -> "今天${this.formatDate(DateFormat.HH_MM)}"
        this.isYesterday -> "昨天${this.formatDate(DateFormat.HH_MM)}"
        this.isTomorrow -> "明天${this.formatDate(DateFormat.HH_MM)}"
        this.isThisYear -> this.formatDate(DateFormat.MM_DD_EEE_HH_MM_CN)
        else -> this.formatDate(DateFormat.YYYY_MM_DD_EEE_HH_MM)
    }

val Long.formatExpireTime: String
    get() = DJSDate.getDays(DJSDate.now, this).let {
        when {
            it <= 0 -> "(已到期)"
            it < 30 -> "(${it}天后到期)"
            else -> ""
        }
    }

val Long.toDayId: Long
    get() = Calendar.getInstance().let {
        it.timeInMillis = this
        (it.get(Calendar.YEAR) * 1000 + it.get(Calendar.DAY_OF_YEAR)).toLong()
    }

val Long.toWeekId: Long
    get() = Calendar.getInstance().let {
        it.firstDayOfWeek = Calendar.MONDAY
        it.timeInMillis = this
        (it.get(Calendar.YEAR) * 1000 + it.get(Calendar.WEEK_OF_YEAR)).toLong()
    }

val Long.toMonthId: Long
    get() = Calendar.getInstance().let {
        it.timeInMillis = this
        (it.get(Calendar.YEAR) * 100 + it.get(Calendar.MONTH)).toLong()
    }

val Long.formatVideoDuration: String
    get() {
        val temp = this / 1000
        val hour = temp / 3600
        val min = temp % 3600 / 60
        val sec = temp % 60
        return if (hour == 0L) {
            String.format("%02d:%02d", min, sec)
        } else {
            String.format("%02d:%02d:%02d", hour, min, sec)
        }
    }

val Long.formatDurationMS: String
    get() {
        val minutes = this / Constants.MINUTE_MILLIS
        val seconds = (this % Constants.MINUTE_MILLIS) / Constants.SECOND_MILLIS
        return "${minutes}分${seconds}秒"
    }

val Long.formatDurationMS1: String
    get() {
        val minutes = this / 60
        val seconds = (this % 60)
        return "${minutes}:${seconds}"
    }

val Long.formatComfortMonth: String
    get() {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = this
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        return when {
            !isThisYear -> "${year}年${month}月"
            isThisMonth -> "本月"
            else -> "${month}月"
        }
    }

val Long.formatComfortDay: String
    get() {
        return when {
            isToday -> "今天"
            isYesterday -> "昨天"
            isThisYear -> formatDate(MM_DD)
            else -> formatDate(YYYY_MM_DD)
        }
    }

val Int.toServiceWeek
    get() = if (this == Calendar.SUNDAY) 7 else this - 1

fun Long.toCalendar() = Calendar.getInstance().apply {
    timeInMillis = this@toCalendar
}

fun Double?.ageToTime(): Long? = this?.roundToInt()?.let { age ->
    Calendar.getInstance()
        .let {
            it.timeInMillis = DJSDate.now
            if (age > 0) {
                it.clearBelowHour()
                it.set(Calendar.DAY_OF_YEAR, 1)
                it.add(Calendar.YEAR, -age)
                it.timeInMillis
            } else {
                null
            }
        }
}

fun Date.clearBelowHour() = Date(time.clearBelowHour())


fun getTimeInMillis(year: Int, month: Int, day: Int): Long {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.YEAR, year)
    calendar.set(Calendar.MONTH, month - 1)
    calendar.set(Calendar.DAY_OF_MONTH, day)
    calendar.clearBelowHour()
    return calendar.timeInMillis
}

fun Long.toDate() = Date(this)

fun getDate(year: Int, month: Int, day: Int): Date {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.YEAR, year)
    calendar.set(Calendar.MONTH, month - 1)
    calendar.set(Calendar.DAY_OF_MONTH, day)
    calendar.clearBelowHour()
    return calendar.time
}

fun Date.add(year: Int, month: Int): Date {
    val calendar = Calendar.getInstance().apply {
        time = this@add
        add(Calendar.YEAR, year)
        add(Calendar.MONTH, month)
    }
    return calendar.time
}

/**
 * 将时间格式化为时间间隔
 * [withHour]是否需要小时
 */
fun Int?.timeDurationFormat(withHour: Boolean = true) = this?.toLong().timeDurationFormat(withHour)
fun Long?.timeDurationFormat(withHour: Boolean = true): String {
    if (this == null || this == 0L) {
        return "0秒"
    }
    val hour = if (withHour) {
        this / 3600
    } else {
        0
    }
    val hourLeft = if (withHour) {
        this % 3600
    } else {
        this
    }
    val min = hourLeft / 60
    val sec = hourLeft % 60
    return "${hour}时${min}分${sec}秒"
}



fun Long?.toHHMMSS(withHour: Boolean = true): String {
    if (this == null || this == 0L) {
        return "0"
    }
    val hour = if (withHour) {
        this / 3600
    } else {
        0
    }
    val hourLeft = if (withHour) {
        this % 3600
    } else {
        this
    }
    val min = hourLeft / 60
    val sec = hourLeft % 60
    return if (withHour) {
        String.format("%02d:%02d:%02d", hour, min, sec)
    } else {
        String.format("%02d:%02d", min, sec)
    }
}

fun Int?.toCalendarWeekDay() = when (this) {
    1 -> Calendar.MONDAY
    2 -> Calendar.TUESDAY
    3 -> Calendar.WEDNESDAY
    4 -> Calendar.THURSDAY
    5 -> Calendar.FRIDAY
    6 -> Calendar.SATURDAY
    7 -> Calendar.SUNDAY
    else -> Calendar.MONDAY
}

// 将 Calendar 的常量周天转为 1~7 表示周一到周日
fun Int.toNormalWeekDay() = ((this + 5) % 7) + 1

/**
 * 清除这个日期的年月日信息
 */
fun Long.getTimeInDay(): Long {
    return Calendar.getInstance().apply {
        timeInMillis = this@getTimeInDay
        set(Calendar.YEAR, 1970)
        set(Calendar.MONTH, 0)
        set(Calendar.DAY_OF_MONTH, 1)
    }.timeInMillis
}

/**
 * [other] 比 this大几个月
 */
fun Long.betweenMonth(other: Long): Int {
    return if (this > other) {
        -(other.betweenMonth(this))
    } else {
        val currentCalendar = this.toCalendar()
        val otherCalendar = other.toCalendar()
        val yearCount = otherCalendar.get(Calendar.YEAR) - currentCalendar.get(Calendar.YEAR)
        val monthCount = otherCalendar.get(Calendar.MONTH) - currentCalendar.get(Calendar.MONTH)
        if (otherCalendar.get(Calendar.DAY_OF_MONTH) >= currentCalendar.get(Calendar.DAY_OF_MONTH)) {
            yearCount * 12 + monthCount
        } else {
            yearCount * 12 + monthCount - 1
        }
    }
}