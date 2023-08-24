package com.kissspace.common.model.immessage

data class GiftNotifyMessage(
    val chatRoomId: String,
    val roomTitle: String,
    val giftId: String,
    val giftName: String,
    val number: Int,
    val sourceUserId: String,
    val sourceUserNickname: String,
    val floatTipUrl: String,
    val targetUserId: String,
    val targetUserNickname: String,
    val giftUrl: String,
)