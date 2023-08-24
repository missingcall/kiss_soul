package com.kissspace.common.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class PointsBoxResultBean(
    val number: Long,
    val totalPoints: Int,
    val url: String,
) : Parcelable