package com.kissspace.common.model.immessage

import com.kissspace.common.model.UserMedalBean

data class WaterMessage(
    val charmLevel: Int,
    val chatRoomId: String,
    val consumeLevel: Int,
    val nickname: String,
    val userId: String,
    val gameName: String,
    val profilePath: String,
    val headwearUrl: String = "",
    val privilege: String,
    val medalList: MutableList<UserMedalBean>? = null,
    val waterGameGiftInfoDto: WaterGameGiftInfoBean,
    val messageKindList: List<String> = mutableListOf()
)

data class WaterGameGiftInfoBean(
    val amount: Int,
    val giftFlag: String,
    val giftId: String,
    val giftName: String,
    val giftType: Int,
    val num: Int,
    val orderBy: Int,
    val price: Int,
    val url: String
)