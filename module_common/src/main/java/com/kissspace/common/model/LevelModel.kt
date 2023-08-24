package com.kissspace.common.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * @Author gaohangbo
 * @Date 2023/2/2 10:38.
 * @Describe 魅力等级 财富等级
 */
@Serializable
@Parcelize
data class LevelModel(
    val icon: String,
    val id: Int,
    val name: String,
    val num: Double
) : Parcelable