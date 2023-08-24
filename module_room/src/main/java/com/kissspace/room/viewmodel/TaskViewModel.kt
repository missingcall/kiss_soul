package com.kissspace.room.viewmodel

import com.didi.drouter.router.Result
import com.kissspace.common.base.BaseViewModel
import com.kissspace.network.net.Method
import com.kissspace.network.net.request
import com.kissspace.room.http.RoomApi

/**
 * @Author gaohangbo
 * @Date 2023/3/10 15:29.
 * @Describe 任务模块
 */
class TaskViewModel  : BaseViewModel() {
    fun shareChatRoom(block: ((Boolean) -> Unit)? = null) {
        request<Boolean>(RoomApi.API_SHARE_CHAT_ROOM, Method.POST, onSuccess = {
            block?.invoke(it)
        })
    }
}