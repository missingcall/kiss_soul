package com.kissspace.common.model.immessage

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/12/24 18:30
 * @Description: 修改房间消息实体类
 *
 */
data class UpdateRoomInfoMessage(
    var crId: String,
    var userId: String,
    var roomTitle: String,
    var roomAffiche: String,
    var roomIcon: String
)