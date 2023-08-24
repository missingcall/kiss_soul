package com.kissspace.common.util.format

import com.kissspace.common.util.formatNum
import java.math.RoundingMode
import java.text.DecimalFormat

/**
 * E=#
 * O=0
 * _=.
 * __=,
 * E__EEO_OO＝#,##0.00
 * @author Loja
 * @created on 10/23/18.
 */
object Format {
    //截去小数点
    val E = DecimalFormat("#").apply { roundingMode = RoundingMode.DOWN }
    val E_UP = DecimalFormat("#").apply { roundingMode = RoundingMode.HALF_UP }
    val O_O = DecimalFormat("0.0").apply { roundingMode = RoundingMode.HALF_UP }
    val O_O_DOWN = DecimalFormat("0.0").apply { roundingMode = RoundingMode.DOWN }
    //截去小数点
    val O_OO = DecimalFormat("0.00").apply { roundingMode = RoundingMode.DOWN }
    //保留小数点
    val O_OO_UP = DecimalFormat("0.00").apply { roundingMode = RoundingMode.UP }

    val O_EE = DecimalFormat("0.##").apply { roundingMode = RoundingMode.HALF_UP }
    val O_EE_NEGATIVE = DecimalFormat("0.##;-#").apply { roundingMode = RoundingMode.HALF_UP }
    val O_OOO = DecimalFormat("0.000").apply { roundingMode = RoundingMode.HALF_UP }
    val E__EEE = DecimalFormat("#,###").apply { roundingMode = RoundingMode.HALF_UP }
    val E__EEO_OO = DecimalFormat("#,##0.00").apply { roundingMode = RoundingMode.HALF_UP }
    val E__EEE_EE = DecimalFormat("#,###.##").apply { roundingMode = RoundingMode.HALF_UP }
    val OOOOO = DecimalFormat("00000").apply { roundingMode = RoundingMode.HALF_UP }
    val O_E = DecimalFormat("0.#").apply { roundingMode = RoundingMode.HALF_UP }

    val E_EE = DecimalFormat("#.##").apply { roundingMode = RoundingMode.DOWN }

    val E_EE_UP = DecimalFormat("#.##").apply { roundingMode = RoundingMode.HALF_UP }
}