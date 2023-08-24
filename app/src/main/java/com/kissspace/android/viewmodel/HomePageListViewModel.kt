package com.kissspace.android.viewmodel

import com.kissspace.android.http.Api
import com.kissspace.common.base.BaseViewModel
import com.kissspace.common.config.AppConfigKey
import com.kissspace.common.http.getAppConfigByKey
import com.kissspace.common.model.RoomListBannerBean
import com.kissspace.common.model.RoomListResponse
import com.kissspace.network.net.Method
import com.kissspace.network.net.request
import com.kissspace.network.result.ResultState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class HomePageListViewModel : BaseViewModel() {
    private val _roomList = MutableSharedFlow<ResultState<RoomListResponse>>()
    val roomList = _roomList.asSharedFlow()

    private val _roomListBannerEvent =
        MutableSharedFlow<ResultState<MutableList<RoomListBannerBean>>>()
    val roomListBannerEvent = _roomListBannerEvent.asSharedFlow()

    fun getRoomListBanner() {
        getAppConfigByKey(AppConfigKey.KEY_CHAOBO_BANNER_ROOM_LIST, _roomListBannerEvent)
    }

    fun getRoomList(roomTagId: String, pageNum: Int, pageSize: Int) {
        val param = mutableMapOf<String, Any?>()
        param["roomTagId"] = roomTagId
        param["roomTagCategory"] = "001"
        param["pageNum"] = pageNum
        param["pageSize"] = pageSize
        request(Api.API_GET_ROOM_LIST, Method.POST, param, state = _roomList)
    }
}