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
@Parcelize
data class RoomGameConfig(
    val game_name: String = "",
    val game_icon: String = "",
    val game_url: String = ""
) : Parcelable