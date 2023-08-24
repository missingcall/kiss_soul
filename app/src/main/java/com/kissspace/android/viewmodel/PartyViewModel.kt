package com.kissspace.android.viewmodel

import com.kissspace.android.http.Api
import com.kissspace.common.base.BaseViewModel
import com.kissspace.common.config.AppConfigKey
import com.kissspace.common.config.CommonApi
import com.kissspace.common.http.getAppConfigByKey
import com.kissspace.common.model.PartyBannerListBean
import com.kissspace.common.model.RoomListBannerBean
import com.kissspace.common.model.RoomListResponse
import com.kissspace.common.model.RoomTagListBean
import com.kissspace.network.net.Method
import com.kissspace.network.net.request
import com.kissspace.network.result.ResultState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow

class PartyViewModel : BaseViewModel() {
    private val _tagListEvent = MutableSharedFlow<ResultState<List<RoomTagListBean>>>()
    val tagListEvent = _tagListEvent.asSharedFlow()

    private val _getBannerEvent = MutableSharedFlow<ResultState<MutableList<PartyBannerListBean>>>()
    val getBannerEvent = _getBannerEvent.asSharedFlow()


    private val _roomListEvent = MutableSharedFlow<ResultState<RoomListResponse>>()
    val roomListEvent = _roomListEvent.asSharedFlow()

    private val _randomRoomEvent = MutableSharedFlow<ResultState<String>>()
    val randomRoomEvent = _randomRoomEvent.asSharedFlow()

    private val _roomListBannerEvent =
        MutableSharedFlow<ResultState<MutableList<RoomListBannerBean>>>()
    val roomListBannerEvent = _roomListBannerEvent.asSharedFlow()

    //是否显示首充
    val isShowFirstRecharge = MutableStateFlow<Boolean>(true)

    fun getRoomListBanner() {
        getAppConfigByKey(AppConfigKey.KEY_PARTY_BANNER_ROOM_LIST, _roomListBannerEvent)
    }

    fun getRoomList(roomTagId: String, pageNum: Int, pageSize: Int) {
        val param = mutableMapOf<String, Any?>()
        param["roomTagId"] = roomTagId
        param["roomTagCategory"] = "002"
        param["pageNum"] = pageNum
        param["pageSize"] = pageSize
        request(Api.API_GET_ROOM_LIST, Method.POST, param, state = _roomListEvent)
    }


    fun getHomeBanner() {
        request(CommonApi.API_HOME_BANNER_PARTY, Method.GET, state = _getBannerEvent)
    }

    fun getPartyRandomRoom(id:String){
        if (id.isNotEmpty()){
            val param = mutableMapOf<String, Any?>()
            param["tagId"] = id
            request(CommonApi.API_GET_RANDOM_ROOM,Method.GET,param,state = _randomRoomEvent)
        }
    }



    fun getRoomTagList() {
        getAppConfigByKey(AppConfigKey.KEY_PARTY_TAG_LIST, _tagListEvent)
    }


}