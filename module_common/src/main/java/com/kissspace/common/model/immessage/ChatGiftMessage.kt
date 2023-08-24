package com.kissspace.common.model.immessage


data class ChatGiftMessage(
    var giftName: String,
    var total: Int,
    var url: String,
    var svg: String?,
    var price: Int,
    var giftId: String
)