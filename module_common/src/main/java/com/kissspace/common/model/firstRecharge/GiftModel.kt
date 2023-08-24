package com.kissspace.common.model.firstRecharge

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * @Author gaohangbo
 * @Date 2023/2/7 14:24.
 * @Describe 礼物
 */
@Parcelize
@Serializable
data class GiftModel(
    val giftId: String,
    val giftImg: String,
    val giftName: String,
    val giftNum: Double
): Parcelable