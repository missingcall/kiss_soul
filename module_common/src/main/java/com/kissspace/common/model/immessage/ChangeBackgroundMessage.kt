package com.kissspace.common.model.immessage

data class ChangeBackgroundMessage(
    var chatRoomId: String,
    var staticImage: String = "",
    var dynamicImage: String? = null
)