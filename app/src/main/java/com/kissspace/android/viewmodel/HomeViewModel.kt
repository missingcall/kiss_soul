package com.kissspace.android.viewmodel

import com.kissspace.common.base.BaseViewModel
import com.kissspace.common.config.AppConfigKey
import com.kissspace.common.config.CommonApi
import com.kissspace.common.http.getAppConfigByKey
import com.kissspace.common.model.RoomTagListBean
import com.kissspace.common.model.VideoBannerListBean
import com.kissspace.network.net.Method
import com.kissspace.network.net.request
import com.kissspace.network.result.ResultState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow


class HomeViewModel : BaseViewModel() {
    private val _tagList = MutableSharedFlow<ResultState<MutableList<RoomTagListBean>>>()
    val tagList = _tagList.asSharedFlow()

    private val _getBannerEvent = MutableSharedFlow<ResultState<MutableList<VideoBannerListBean>>>()
    val getBannerEvent = _getBannerEvent.asSharedFlow()

    //是否显示首充
    val isShowFirstRecharge = MutableStateFlow<Boolean>(true)

    fun getRoomTagList() {
        getAppConfigByKey(AppConfigKey.KEY_CHAO_BO_TAG_LIST, _tagList)
    }

    fun getHomeBanner() {
        request(CommonApi.API_HOME_BANNER_CHAOBO, Method.GET, state = _getBannerEvent)
    }

}