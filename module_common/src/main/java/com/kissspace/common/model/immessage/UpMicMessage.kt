package com.kissspace.common.model.immessage

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/12/14 17:02
 * @Description: 上麦IM消息体
 */
class UpMicIMMessage(
    var microphoneNumber: Int,
    var nickname: String,
    var profilePath: String,
    var userId: String,
    var userRole: String,
    var waitingMicrophoneNumber: Int,
    var headWearSvga: String = "",
    var headWearIcon: String = ""
)