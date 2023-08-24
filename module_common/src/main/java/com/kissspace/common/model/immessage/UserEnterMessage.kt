package com.kissspace.common.model.immessage

import java.util.Date

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/12/21 19:48
 * @Description: 用户进场消息
 *
 */
data class UserEnterMessage(
    var profilePath: String,
    var nickname: String,
    var consumeLevel: Int,//财富等级
    var charmLevel: Int,
    var carPath: String,
    var userId: String,
)