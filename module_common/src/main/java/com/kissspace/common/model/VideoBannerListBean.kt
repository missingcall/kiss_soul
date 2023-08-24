package com.kissspace.common.model

import kotlinx.serialization.Serializable

@Serializable
data class VideoBannerListBean(
    val chatRoomId: String,
    val desc: String,
    val integralGuessId: String = "",
    val jumpLink: String,
    val jumpType: Int,
    val leftBet: Int,
    val leftNum: Int,
    val rightBet: Int,
    val rightNum: Int,
    val schema: String,
    val sort: Int,
    val url: String,
    val nickName:String
)