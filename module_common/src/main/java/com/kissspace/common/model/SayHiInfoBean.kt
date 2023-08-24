package com.kissspace.common.model

import kotlinx.serialization.Serializable

@Serializable
data class SayHiInfoBean(
    val examineMessageContent: String = "",
    val messageContent: String = "",
    val messageId: String,
    val messageTypeId: String,
    val userId: String
)