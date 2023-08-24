package com.kissspace.common.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/12/12
 * @Description: 登录返回实体
 *
 */
@Parcelize
@kotlinx.serialization.Serializable
data class LoginResultBean(
    var token: String,
    var refreshToken: String,
    var netEaseToken: String,
    var tokenHead: String,
    var expiresIn: Long,
    var newUser: Boolean,
    var information: Boolean,
    var accId: String
) : Parcelable
