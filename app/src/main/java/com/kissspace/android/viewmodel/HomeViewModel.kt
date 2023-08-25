package com.kissspace.android.viewmodel

import com.kissspace.android.http.Api
import com.kissspace.common.base.BaseViewModel
import com.kissspace.common.config.AppConfigKey
import com.kissspace.common.config.CommonApi
import com.kissspace.common.http.getAppConfigByKey
import com.kissspace.common.model.HomeUserListBean
import com.kissspace.common.model.RoomScreenMessageModel
import com.kissspace.common.model.RoomTagListBean
import com.kissspace.common.model.VideoBannerListBean
import com.kissspace.network.net.Method
import com.kissspace.network.net.request
import com.kissspace.network.result.ResultState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow


class HomeViewModel : BaseViewModel() {

    private val _roomList = MutableSharedFlow<ResultState<List<HomeUserListBean>>>()
    val roomList = _roomList.asSharedFlow()

    private val _homeMessage = MutableSharedFlow<ResultState<RoomScreenMessageModel>>()
    val homeMessage = _homeMessage.asSharedFlow()

    fun getHomeUserList() {
        request(Api.API_POPULAR_USER_LIST, Method.GET, state = _roomList)
    }

    fun getCurrentMessage(){
        request(Api.API_HOME_QUERY_MESSAGE, Method.GET, state = _homeMessage)
    }
}