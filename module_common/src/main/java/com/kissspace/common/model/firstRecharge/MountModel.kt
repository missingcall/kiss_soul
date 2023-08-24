package com.kissspace.common.model.firstRecharge

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * @Author gaohangbo
 * @Date 2023/2/23 14:43.
 * @Describe 坐骑
 */
@Serializable
@Parcelize
data class MountModel(
    val icon: String,
    val name: String,
    //天数
    val carIndate:Int
): Parcelable