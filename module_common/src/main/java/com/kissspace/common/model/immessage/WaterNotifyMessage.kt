package com.kissspace.common.model.immessage

data class WaterNotifyMessage(
    val chatRoomId: String,
    val giftId: String,
    val giftName: String,
    val number: Int,
    val floatTipUrl: String,
    val giftUrl: String,
    val waterTreeNickname: String = "",
    val waterTreeUserId: String = "",
    val gameName: String = "",
    val gameType: String = "001"
)