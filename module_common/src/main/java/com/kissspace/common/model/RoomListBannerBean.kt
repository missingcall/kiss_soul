package com.kissspace.common.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class RoomListBannerBean(
    var url: String,
    var schema: String,
    var state: String,
    var os: String,
    var jumpType: String,
    var chatRoomId: String,
) : Parcelable