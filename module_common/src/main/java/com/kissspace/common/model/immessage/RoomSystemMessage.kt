package com.kissspace.common.model.immessage

data class RoomSystemMessage(
    var os: List<String>,
    var pushLocation: String,
    var roomType: String,
    var sex: String,
    var title: String,
    var content: String,
    val messageKindList: List<String> = mutableListOf()
)