package com.kissspace.common.model

import androidx.databinding.BaseObservable
import com.netease.nimlib.sdk.msg.model.RecentContact

data class ChatListModel(
    var fromAccount: String? = null,
    var nickname: String? = null,
    var avatar: String? = null,
    var content: String? = null,
    var date: String? = null,
    var unReadCount: Int = 0,
    var followRoomId: String? = null,
    var onlineState: String? = "002"
) : BaseObservable()