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

class SettingManagerViewModel : BaseViewModel() {
    private val _getUsersEvent =
        MutableSharedFlow<ResultState<MutableList<RoomOnLineUserListBean>>>()
    val getUsersEvent = _getUsersEvent.asSharedFlow()

    private val _setManagerEvent = MutableSharedFlow<ResultState<Int>>()
    val setManagerEvent = _setManagerEvent.asSharedFlow()

    private val _cancelManagerEvent = MutableSharedFlow<ResultState<Int>>()
    val cancelManagerEvent = _cancelManagerEvent.asSharedFlow()

    fun getOnLineUsers(crId: String) {
        val param = mutableMapOf<String, Any?>("chatRoomId" to crId, "showAdmin" to "1")
        request(RoomApi.API_GET_ROOM_ONLINE_USER, Method.GET, param, state = _getUsersEvent)
    }

    fun setManager(crId: String, userId: String) {
        val param = mutableMapOf<String, Any?>()
        param["chatRoomId"] = crId
        param["userId"] = userId
        param["role"] = Constants.ROOM_USER_TYPE_MANAGER
        request(RoomApi.API_UPDATE_USER_ROLE, Method.POST, param, state = _setManagerEvent)
    }

    fun cancelManager(crId: String, userId: String) {
        val param = mutableMapOf<String, Any?>()
        param["chatRoomId"] = crId
        param["userId"] = userId
        request(RoomApi.API_CANCEL_MANAGER, Method.POST, param, state = _cancelManagerEvent)
    }
}