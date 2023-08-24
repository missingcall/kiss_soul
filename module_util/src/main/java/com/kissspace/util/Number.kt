package com.kissspace.util

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.String

/**
 *  转换浮点数
 *  @param digit 保留的小数点位数
 *  @param mode 四舍五入模式
 */
fun Number.formatDecimal(digit: Int, mode: RoundingMode): String {
    return when (digit) {
        0 -> DecimalFormat("#").apply { roundingMode = mode }.format(this)
        1 -> DecimalFormat("0.0").apply { roundingMode = mode }.format(this)
        2 -> DecimalFormat("0.00").apply { roundingMode = mode }.format(this)
        3 -> DecimalFormat("0.000").apply { roundingMode = mode }.format(this)
        4 -> DecimalFormat("0.0000").apply { roundingMode = mode }.format(this)
        else -> DecimalFormat("#").apply { roundingMode = mode }.format(this)
    }
}


fun formatDoubleDown(value:Double): Double {
    return BigDecimal(value).setScale(0, RoundingMode.DOWN).toDouble()
}

//是否整除100
fun isMultiple(value:Double,count: Double): Boolean {
    return value%count!=0.0
}
