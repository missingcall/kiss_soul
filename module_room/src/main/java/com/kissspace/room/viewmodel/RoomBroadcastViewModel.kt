package com.kissspace.room.viewmodel

import com.kissspace.common.base.BaseViewModel
import com.kissspace.network.net.Method
import com.kissspace.network.net.request
import com.kissspace.network.result.ResultState
import com.kissspace.room.http.RoomApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class RoomBroadcastViewModel : BaseViewModel() {
    private val _sendMessageEvent = MutableSharedFlow<ResultState<Boolean>>()
    val sendMessageEvent = _sendMessageEvent.asSharedFlow()

    fun send(coin: String, messageContent: String) {
        val param = mutableMapOf<String, Any?>()
        param["costCoin"] = coin
        param["messageContent"] = messageContent
        request(RoomApi.API_SEND_BROADCAST_MESSAGE, Method.POST, param, state = _sendMessageEvent)
    }
}