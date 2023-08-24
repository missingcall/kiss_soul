package com.kissspace.common.model

import kotlinx.serialization.Serializable

@Serializable
data class CheckRoomInfoModel(
    var roomPwd: String = "",
    var roomTagCategory: String,
    var kickOutOfTheRoom: Boolean,
    var neteaseChatId: String,
    var kickOutOfTheRoomTime: Long = 0,
    var role:String
)