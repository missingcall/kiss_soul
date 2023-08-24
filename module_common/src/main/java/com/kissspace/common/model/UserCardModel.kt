package com.kissspace.common.model

import kotlinx.serialization.Serializable

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/12/29 11:53
 * @Description: 用户名片信息
 *
 */
@Serializable
data class UserCardModel(
    val charmLevel: Int = 0,
    val consumeLevel: Int = 0,
    val displayId: String,
    val personalSignature: String = "这个人很懒～",
    var isFollow: Boolean,//是否关注
    var isForbiddenMike: Boolean = false,//是否禁麦
    var blackoutState: Boolean = false,//是否拉黑
    var isMuted: Boolean = false,//是否禁言
    val nickname: String,
    val profilePath: String,
    val sex: String,
    val userId: String,
    var isCheckedMic: Boolean = true,
    var accid: String,
    var userRole: String,
    var familyName: String = "",
    var medalList: MutableList<UserMedalBean> = mutableListOf()
)