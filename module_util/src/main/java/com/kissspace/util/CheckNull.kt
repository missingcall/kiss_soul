package com.kissspace.util

import android.text.Editable

fun <T> Collection<T>?.isNotEmpty(): Boolean = !isNullOrEmpty()

fun CharSequence?.isNotEmptyBlank(): Boolean = !isNullOrBlank()

fun CharSequence?.isNotBlank(): Boolean = !isNullOrBlank()

fun CharSequence?.isNotEmpty(): Boolean = !isNullOrEmpty()

fun String?.trimString(): String = this.toString().trim()

fun Editable?.trimString(): String = this.toString().trim()

fun Number?.isNullOrZero(): Boolean = (this == null || this == 0)

fun Int?.orZero(): Int = this ?: 0

fun Long?.orZero(): Long = this ?: 0

fun Double?.orZero(): Double = this ?: 0.0

fun Float?.orZero(): Float = this ?: 0f

fun Boolean?.orFalse(): Boolean = this ?: false

fun Boolean?.orTrue(): Boolean = this ?: true

fun String.notEmptyOrNull(): String? = if (this == "") null else this

inline fun String?.ifNullOrEmpty(defaultValue: String = ""): String {
    return if (this.isNullOrEmpty()) defaultValue else this
}