package com.kissspace.common.model.immessage

data class MicQueueMessage(
    var operatorId: String,
    var userId: String,
    var waitingMicrophoneNumber: Int
)