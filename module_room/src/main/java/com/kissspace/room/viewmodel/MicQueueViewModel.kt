package com.kissspace.room.viewmodel

import com.kissspace.common.base.BaseViewModel
import com.kissspace.common.config.Constants
import com.kissspace.common.model.MicQueueUserModel
import com.kissspace.common.util.customToast
import com.kissspace.network.net.Method
import com.kissspace.network.net.request
import com.kissspace.network.result.ResultState
import com.kissspace.room.http.RoomApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.json.JSONArray

class MicQueueViewModel : BaseViewModel() {
    private val _getQueueUsersEvent =
        MutableSharedFlow<ResultState<MutableList<MicQueueUserModel>>>()
    val getQueueUsersEvent = _getQueueUsersEvent.asSharedFlow()

    private val _inviteMicEvent = MutableSharedFlow<ResultState<Int>>()
    val inviteMicEvent = _inviteMicEvent.asSharedFlow()

    //取消排麦
    private val _cancelQueueEvent = MutableSharedFlow<ResultState<Int>>()
    val cancelQueueEvent = _cancelQueueEvent.asSharedFlow()

    //获取排麦用户列表
    fun getMicQueueUserList(crId: String) {
        val params = mutableMapOf<String, Any?>("chatRoomId" to crId)
        request(RoomApi.API_GET_MIC_QUEUE_LIST, Method.GET, params, state = _getQueueUsersEvent)
    }

    fun inviteMic(crId: String, userId: String, success: () -> Unit) {
        val param = mutableMapOf<String, Any?>()
        param["chatRoomId"] = crId
        param["userId"] = userId
        param["roomTagCategory"] = Constants.ROOM_TYPE_PARTY
        request<Int>(RoomApi.API_INVITE_MIC, Method.POST, param, onSuccess = {
            success.invoke()
        }, onError = {
            customToast(it.message)
        })
    }

    fun cancelQueue(crId: String, userId: JSONArray, success: () -> Unit) {
        val param = mutableMapOf<String, Any?>()
        param["chatRoomId"] = crId
        param["userIds"] = userId
        request<Int>(RoomApi.API_CANCEL_QUEUE, Method.POST, param, onSuccess = {
            success()
        }, onError = {
            customToast(it.message)
        })
    }

}