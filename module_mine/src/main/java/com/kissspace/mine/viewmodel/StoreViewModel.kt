package com.kissspace.mine.viewmodel

import com.kissspace.common.base.BaseViewModel
import com.kissspace.common.config.Constants
import com.kissspace.common.config.Constants.TypeFaceRecognition
import com.kissspace.common.model.GoodsListBean
import com.kissspace.common.util.mmkv.MMKVProvider
import com.kissspace.common.util.setApplicationValue
import com.kissspace.mine.http.MineApi
import com.kissspace.network.net.Method
import com.kissspace.network.net.request
import com.kissspace.network.result.ResultState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class StoreViewModel : BaseViewModel() {
    private val _getGoodsListEvent = MutableSharedFlow<ResultState<MutableList<GoodsListBean>>>()
    val getGoodsListEvent = _getGoodsListEvent.asSharedFlow()

    private val _buyEvent = MutableSharedFlow<ResultState<Boolean>>()
    val buyEvent = _buyEvent.asSharedFlow()


    fun getGoodsList(type: String) {
        val param = mutableMapOf<String, Any?>("commodityType" to type)
        param["commodityType"] = type
        //装扮商城
        param["position"] = "001"
        request(MineApi.API_STORE_GOODS_LIST, Method.GET, param, state = _getGoodsListEvent)
    }

    fun buy(id: String, type: String) {
        val param = mutableMapOf<String, Any?>()
        param["commodityInfoId"] = id
        param["userId"] = MMKVProvider.userId
        param["paymentMethod"] = type
        setApplicationValue(
            type = TypeFaceRecognition,
            value = Constants.FaceRecognitionType.CONSUMPTION.type.toString()
        )
        request(MineApi.API_BUY_DRESS_UP, Method.POST, param, state = _buyEvent)
    }
}