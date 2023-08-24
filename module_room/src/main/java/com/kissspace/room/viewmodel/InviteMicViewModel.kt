package com.kissspace.room.viewmodel

import com.kissspace.common.base.BaseViewModel
import com.kissspace.common.config.Constants
import com.kissspace.common.model.MicQueueUserModel
import com.kissspace.common.model.RoomOnLineUserListBean
import com.kissspace.network.net.Method
import com.kissspace.network.net.request
import com.kissspace.network.result.ResultState
import com.kissspace.room.http.RoomApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class InviteMicViewModel : BaseViewModel() {
    private val _getUsersEvent = MutableSharedFlow<ResultState<MutableList<RoomOnLineUserListBean>>>()
    val getUsersEvent = _getUsersEvent.asSharedFlow()

    private val _inviteMicEvent = MutableSharedFlow<ResultState<Int>>()
    val inviteMicEvent = _inviteMicEvent.asSharedFlow()

    fun getOnLineUsers(crId: String) {
        val param = mutableMapOf<String, Any?>("chatRoomId" to crId)
        request(RoomApi.API_GET_ROOM_ONLINE_USER, Method.GET, param, state = _getUsersEvent)
    }

    fun inviteMic(crId: String, userId: String, index: Int) {
        val param = mutableMapOf<String, Any?>()
        param["chatRoomId"] = crId
        param["userId"] = userId
        param["microphoneNumber"] = index
        param["roomTagCategory"] = Constants.ROOM_TYPE_PARTY
        request(RoomApi.API_INVITE_MIC, Method.POST, param, state = _inviteMicEvent)
    }

}