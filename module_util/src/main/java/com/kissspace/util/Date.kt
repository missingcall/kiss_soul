package com.kissspace.util

import com.blankj.utilcode.util.TimeUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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

fun getAge(birthday:String):String{
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val birthDate = sdf.parse(birthday)

    val calendar = Calendar.getInstance()
    calendar.timeInMillis = System.currentTimeMillis()
    val currentDate = calendar.time

    calendar.time = birthDate
    val birthCalendar = Calendar.getInstance()
    birthCalendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR))
    birthCalendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH))
    birthCalendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH))
    val birthDateInMillis = birthCalendar.timeInMillis

    val ageInMillis = currentDate.time - birthDateInMillis
    val age = (ageInMillis / (1000L * 60 * 60 * 24 * 365)).toInt()
    return age.toString()
}