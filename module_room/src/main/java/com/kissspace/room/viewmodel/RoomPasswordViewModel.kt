package com.kissspace.room.viewmodel

import com.kissspace.common.base.BaseViewModel
import com.kissspace.network.net.Method
import com.kissspace.network.net.request
import com.kissspace.network.result.ResultState
import com.kissspace.room.http.RoomApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class RoomPasswordViewModel : BaseViewModel() {
    private val _setPasswordEvent = MutableSharedFlow<ResultState<Int>>()
    val setPasswordEvent = _setPasswordEvent.asSharedFlow()

    private val _closePasswordEvent = MutableSharedFlow<ResultState<Int>>()
    val closePasswordEvent = _closePasswordEvent.asSharedFlow()

    fun setPassword(crId: String, password: String) {
        val param = mutableMapOf<String, Any?>()
        param["crId"] = crId
        param["roomPwd"] = password
        request(RoomApi.API_SET_PASSWORD, Method.POST, param, state = _setPasswordEvent)
    }

    fun closePassword(crId: String) {
        val param = mutableMapOf<String, Any?>()
        param["chatRoomId"] = crId
        request(RoomApi.API_CLOSE_PASSWORD, Method.GET, param, state = _closePasswordEvent)
    }
}