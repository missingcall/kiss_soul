package com.kissspace.room.viewmodel

import androidx.databinding.ObservableField
import androidx.lifecycle.viewModelScope
import com.kissspace.common.base.BaseViewModel
import com.kissspace.common.config.CommonApi
import com.kissspace.common.config.Constants
import com.kissspace.common.config.Constants.TypeFaceRecognition
import com.kissspace.common.model.*
import com.kissspace.common.model.immessage.GiftMessage
import com.kissspace.common.util.mmkv.MMKVProvider
import com.kissspace.common.util.setApplicationValue
import com.kissspace.network.net.Method
import com.kissspace.network.net.request
import com.kissspace.network.result.ResultState
import com.kissspace.room.http.RoomApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.json.JSONArray

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/12/19 14:11
 * @Description: 礼物弹窗viewModel
 *
 */
class GiftDialogViewModel : BaseViewModel() {
    val userInfo = ObservableField<UserInfoBean>()

    //是否送给所有用户
    val isAllUserChecked = ObservableField(false)

    //是否选择
    val isAllGiftChecked = ObservableField(false)

    //获取礼物tab列表事件
    private val _getTabListEvent = MutableSharedFlow<ResultState<MutableList<GiftTabModel>>>()
    val getTabListEvent = _getTabListEvent.asSharedFlow()

    //获取麦位用户列表事件
    private val _getOnMicUsersEvent = MutableSharedFlow<ResultState<MutableList<MicUserModel>>>()
    val getOnMicUsersEvent = _getOnMicUsersEvent.asSharedFlow()

    private val _getTargetUserInfoEvent = MutableSharedFlow<ResultState<UserCardModel>>()
    val getTargetUserInfoEvent = _getTargetUserInfoEvent.asSharedFlow()

    //送礼事件
    private val _sendGiftEvent = MutableSharedFlow<ResultState<GiftMessage>>()
    val sendGiftEvent = _sendGiftEvent.asSharedFlow()

    //开盲盒事件
    private val _openIntegralBoxEvent =
        MutableSharedFlow<ResultState<PointsBoxResultBean>>()
    val openIntegralBoxEvent = _openIntegralBoxEvent.asSharedFlow()

    fun requestUserInfo() {
        request<UserInfoBean>(CommonApi.API_GET_USER_INFO, Method.GET, onSuccess = {
            userInfo.set(it)
        })
    }

    fun getTabList() {
        request(RoomApi.API_GET_GIFT_TABS, Method.GET, state = _getTabListEvent)
    }

    private fun getOnMicUsers(crId: String) {
        val param = mutableMapOf<String, Any?>("chatRoomId" to crId)
        request<MutableList<MicUserModel>>(RoomApi.API_GET_ON_MIC_USERS,
            Method.GET,
            param,
            onSuccess = {
                it.removeAll { t -> t.wheatPositionId == MMKVProvider.userId }
                viewModelScope.launch { _getOnMicUsersEvent.emit(ResultState.onAppSuccess(it)) }
            })
    }

    fun switchUserList(crId: String, targetUserIds: String) {
        if (targetUserIds.isEmpty() && crId.isNotEmpty()) {
            getOnMicUsers(crId)
        } else {
            getTargetUserInfo(crId, targetUserIds)
        }
    }

    private fun getTargetUserInfo(crId: String, userId: String) {
        val params = mutableMapOf<String, Any?>("userId" to userId, "chatRoomId" to crId)
        request<UserCardModel>(RoomApi.API_GET_USER_PROFILE_INFO, Method.GET, params, onSuccess = {
            val model = MicUserModel(
                wheatPositionId = it.userId,
                wheatPositionIdHeadPortrait = it.profilePath,
                onMicroPhoneNumber = -1,
                checked = true)
            viewModelScope.launch {
                _getOnMicUsersEvent.emit(ResultState.onAppSuccess(mutableListOf(model)))
            }
        })
    }

    fun sendGift(
        crId: String?,
        giftSource: String,
        count: Int,
        integralBox: NormalGiftModel?,
        targetUserIds: JSONArray,
        giveGiftInfos: JSONArray
    ) {
        if (integralBox != null) {
            val param = mutableMapOf<String, Any?>()
            param["isFree"] = integralBox.freeNumber > 0
            param["mysteryBoxId"] = integralBox.id
            param["number"] = count
            request(RoomApi.API_OPEN_INTEGRAL_BOX, Method.POST, param, state = _openIntegralBoxEvent)
        } else {
            val param = mutableMapOf<String, Any?>()
            param["chatRoomId"] = crId
            param["sourceUserId"] = MMKVProvider.userId
            param["targetUserIds"] = targetUserIds
            param["giveGiftInfos"] = giveGiftInfos
            param["giftSource"] = giftSource
            setApplicationValue(
                type= TypeFaceRecognition,
                value= Constants.FaceRecognitionType.CONSUMPTION.type.toString()
            )
            request(RoomApi.API_SEND_GIFT, Method.POST, param, state = _sendGiftEvent)
        }

    }
}

