package com.kissspace.room.viewmodel

import android.text.Editable
import androidx.databinding.ObservableField
import com.kissspace.common.base.BaseViewModel
import com.kissspace.common.model.RoomSettingInfoModel
import com.kissspace.network.net.Method
import com.kissspace.network.net.request
import com.kissspace.network.result.ResultState
import com.kissspace.room.http.RoomApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class RoomInfoViewModel : BaseViewModel() {
    val roomInfo = ObservableField<RoomSettingInfoModel>()
    val nameTextLength = ObservableField(0)
    val noticeTextLength = ObservableField(0)
    val cover = ObservableField<String>()

    private val _submitEvent = MutableSharedFlow<ResultState<Int>>()
    val submitEvent = _submitEvent.asSharedFlow()

    fun requestRoomInfo(crId: String) {
        val param = mutableMapOf<String, Any?>("chatRoomId" to crId)
        request<RoomSettingInfoModel>(RoomApi.API_GET_ROOM_INFO, Method.GET, param, onSuccess = {
            roomInfo.set(it)
            nameTextLength.set(it.roomTitle.length)
            noticeTextLength.set(if (it.roomAffiche == null) 0 else it.roomAffiche!!.length)
            cover.set(it.roomIcon)
        })
    }

    fun onNameTextChange(editable: Editable) {
        val room = roomInfo.get()
        room?.roomTitle = editable.toString()
        roomInfo.set(room)
        nameTextLength.set(editable.toString().length)
    }

    fun onNoticeTextChange(editable: Editable) {
        val room = roomInfo.get()
        room?.roomAffiche = editable.toString()
        roomInfo.set(room)
        noticeTextLength.set(editable.toString().length)
    }

    fun submitInfo() {
        val param = mutableMapOf<String, Any?>()
        param["crId"] = roomInfo.get()!!.crId
        param["roomIcon"] = cover.get()
        param["roomTitle"] = roomInfo.get()?.roomTitle
        param["roomAffiche"] = roomInfo?.get()?.roomAffiche?.ifEmpty { "欢迎来到本房间" }
        request(RoomApi.API_UPDATE_ROOM_INFO, Method.POST, param, state = _submitEvent)
    }
}