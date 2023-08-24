package com.kissspace.common.model.immessage

data class RoomStartPkMessage(
    var isPK: Boolean,
    var password: String,
    var pkTimeCountdown: Long,
    var topic: String,
    var userName: String,
    var host:String,
    var port:String,
)