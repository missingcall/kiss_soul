package com.kissspace.common.model

import kotlinx.serialization.Serializable

@Serializable
data class CommonGiftInfo(
    var giftId: String,
    var giftName: String,
    var price: String,
    var svg: String,
    var url: String,
    var giftNum: Int = 0
)