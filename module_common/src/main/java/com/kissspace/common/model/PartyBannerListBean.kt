package com.kissspace.common.model

import kotlinx.serialization.Serializable

@Serializable
data class PartyBannerListBean(
    val chatRoomId: String,
    val desc: String,
    val jumpLink: String,
    val jumpType: Int,
    val nickName: String,
    val roomTitle: String,
    val schema: String,
    val sort: Int,
    val url: String,
    val userId: String
)