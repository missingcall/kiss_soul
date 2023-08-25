package com.kissspace.common.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class RoomScreenMessageModel(
    val costCoin: Int,
    val messageContent: String,
    val messageId: String,
    val nickname: String,
    val profilePath: String,
    val userId: String,
) : Parcelable