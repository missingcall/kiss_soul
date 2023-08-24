package com.kissspace.room.viewmodel

import androidx.lifecycle.viewModelScope
import com.kissspace.common.base.BaseViewModel
import com.kissspace.common.config.CommonApi
import com.kissspace.common.model.PredictionListBean
import com.kissspace.common.model.PredictionRankingBean
import com.kissspace.common.model.UserInfoBean
import com.kissspace.network.net.Method
import com.kissspace.network.net.request
import com.kissspace.network.result.ResultState
import com.kissspace.room.http.RoomApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class PredictionViewModel : BaseViewModel() {
    private val _getUserInfoEvent = MutableSharedFlow<ResultState<UserInfoBean>>()
    val getUserInfoEvent = _getUserInfoEvent.asSharedFlow()

    private val _betEvent = MutableSharedFlow<ResultState<Int>>()
    val betEvent = _betEvent.asSharedFlow()

    private val _predictionListEvent = MutableSharedFlow<ResultState<List<PredictionListBean>>>()
    val predictionListEvent = _predictionListEvent.asSharedFlow()

    private val _stopBetEvent = MutableSharedFlow<ResultState<Int>>()
    val stopBetEvent = _stopBetEvent.asSharedFlow()

    private val _settleBetEvent = MutableSharedFlow<ResultState<Int>>()
    val settleBetEvent = _settleBetEvent.asSharedFlow()

    private val _rankingEvent = MutableSharedFlow<ResultState<PredictionRankingBean?>>()
    val rankingEvent = _rankingEvent.asSharedFlow()

    fun getUserInfo() {
        request(CommonApi.API_GET_USER_INFO, Method.GET, state = _getUserInfoEvent)
    }

    fun getPredictionList(crId: String) {
        val param = mutableMapOf<String, Any?>("chatRoomId" to crId)
        request(RoomApi.API_GET_PREDICTION_LIST, Method.GET, param, state = _predictionListEvent)
    }

    fun stopBet(id: String, position: Int) {
        val param = mutableMapOf<String, Any?>("integralGuessId" to id)
        request<Int>(RoomApi.API_STOP_PREDICTION, Method.POST, param, onSuccess = {
            viewModelScope.launch { _stopBetEvent.emit(ResultState.onAppSuccess(position)) }
        })
    }


    fun settleBet(id: String, option: String) {
        val param = mutableMapOf<String, Any?>("integralGuessId" to id, "option" to option)
        request(RoomApi.API_SETTLE_PREDICTION, Method.POST, param, state = _betEvent)
    }

    fun deletePrediction(id: String, onSuccess: () -> Unit) {
        val param = mutableMapOf<String, Any?>("integralGuessId" to id)
        request<Int>(RoomApi.API_DELETE_PREDICTION, Method.POST, param, onSuccess = {
            onSuccess()
        })
    }


    fun getRankingList(crId: String) {
        val param = mutableMapOf<String, Any?>("chatRoomId" to crId)
        request(RoomApi.API_PREDICTION_RANKING, Method.GET, param, state = _rankingEvent)
    }
}