package com.kissspace.common.model

import kotlinx.serialization.Serializable

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/12/20 20:18
 * @Description: 房间可修改信息model
 *
 */
@Serializable
data class RoomSettingInfoModel(
    var crId: String,
    var userId: String,
    var roomTitle: String,
    var roomAffiche: String?,
    var roomIcon: String
)