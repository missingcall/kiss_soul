package com.kissspace.common.model.config

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.Serializable

/**
 * @Author gaohangbo
 * @Date 2023/4/14 16:28.
 * @Describe
 */
@Serializable
data class WaterConfig(
    val water: String,
    val wish_pool: String,
    val card_game: String
)