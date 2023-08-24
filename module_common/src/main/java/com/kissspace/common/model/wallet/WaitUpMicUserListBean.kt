package com.kissspace.common.model.wallet

/**
 *
 * @Author: nicko
 * @CreateDate: 2023/1/17 17:43
 * @Description: 排麦用户实体
 *
 */
@kotlinx.serialization.Serializable
data class WaitUpMicUserListBean(
    var userId: String,
    var displayId: String,
    var nickname: String,
    var sex: String,
    var profilePath: String,
    var bgPath: String
)