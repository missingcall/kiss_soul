package com.kissspace.room.viewmodel

import androidx.databinding.ObservableField
import androidx.lifecycle.scopeNetLife
import androidx.lifecycle.viewModelScope
import com.kissspace.common.base.BaseViewModel
import com.kissspace.common.config.CommonApi
import com.kissspace.common.config.Constants
import com.kissspace.common.config.Constants.TypeFaceRecognition
import com.kissspace.common.model.GiftModel
import com.kissspace.common.model.GiftTabModel
import com.kissspace.common.model.PointsBoxResultBean
import com.kissspace.common.model.MicUserModel
import com.kissspace.common.model.PackGiftModel
import com.kissspace.common.model.UserInfoBean
import com.kissspace.common.model.UserProfileBean
import com.kissspace.common.model.immessage.GiftMessage
import com.kissspace.common.util.mmkv.MMKVProvider
import com.kissspace.common.util.setApplicationValue
import com.kissspace.network.net.Method
import com.kissspace.common.http.getUserInfo
import com.kissspace.network.net.asyncRequest
import com.kissspace.network.net.request
import com.kissspace.network.result.ResultState
import com.kissspace.network.result.emitSuccess
import com.kissspace.room.http.RoomApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class GiftViewModel : BaseViewModel() {
    //是否黑暗模式
    val isDarkMode = ObservableField(true)

    //是否全选背包礼物
    val isCheckedAllGift = ObservableField(false)

    val userInfo = ObservableField<UserInfoBean>()

    //是否全选用户
    val isCheckedAllUser = ObservableField(false)

    //礼物列表
    private val _giftListEvent = MutableSharedFlow<ResultState<MutableList<GiftModel>>>()
    val giftListEvent = _giftListEvent.asSharedFlow()

    //礼物tab
    private val _getTabListEvent = MutableSharedFlow<ResultState<MutableList<GiftTabModel>>>()
    val getTabListEvent = _getTabListEvent.asSharedFlow()

    //获取麦位用户列表事件
    private val _getOnMicUsersEvent = MutableSharedFlow<ResultState<MutableList<MicUserModel>>>()
    val getOnMicUsersEvent = _getOnMicUsersEvent.asSharedFlow()

    //开盲盒事件
    private val _openPointsBoxEvent = MutableSharedFlow<ResultState<PointsBoxResultBean>>()
    val openPointsBoxEvent = _openPointsBoxEvent.asSharedFlow()

    //送礼事件
    private val _sendGiftEvent = MutableSharedFlow<ResultState<GiftMessage>>()
    val sendGiftEvent = _sendGiftEvent.asSharedFlow()


    fun requestUserInfo() {
        getUserInfo(onSuccess = {
            userInfo.set(it)
        })
    }

    fun requestGiftList(tabId: String) {
        val params = mutableMapOf<String, Any?>()
        params["giftTabId"] = tabId
        if (tabId != Constants.GIFT_TAB_ID_PACKAGE) {
            request<MutableList<GiftModel>>(
                RoomApi.API_GET_GIFT_BY_ID,
                Method.GET,
                params,
                onSuccess = {
                    viewModelScope.launch { _giftListEvent.emitSuccess(it) }
                })
        } else {
            request<MutableList<PackGiftModel>>(
                RoomApi.API_GET_PACK_GIFT_LIST,
                Method.GET,
                onSuccess = {
                    val list = mutableListOf<GiftModel>()
                    it.forEach { gift ->
                        list.add(
                            GiftModel(
                                id = gift.giftId,
                                name = gift.giftName,
                                isPackGift = true,
                                price = gift.price,
                                url = gift.giftIcon,
                                num = gift.num
                            )
                        )
                    }
                    viewModelScope.launch { _giftListEvent.emitSuccess(list) }
                })
        }
    }

    fun getTabList() {
        request(RoomApi.API_GET_GIFT_TABS, Method.GET, state = _getTabListEvent)
    }

    /**
     *  #1.如果从个人名片点击送礼，收礼人只显示这个人，不跟随麦位变化，且默认选中此人
     *  #2.九麦房点击送礼图标，收礼人只显示麦位上的人（过滤自己）
     *  #3.潮播房点击送礼图标，收礼人显示房主和麦位上的人 （过滤自己），如果从个人名片点击，则同第一条
     *  #4.点击积分盲盒，收礼人只显示自己，切换到其他礼物恢复以上条件
     *  #5.普通礼物收礼人可以多选，背包礼物如果选择一个，收礼人可以多选，全选收礼人必须单选
     *
     *  获取收礼
     *  @param crId 房间id
     *  @param userId 收礼人ID
     *  @param isPointsBox 是否是积分盲盒
     *  @param from 送礼来源
     *
     *
     */
    fun switchUserList(
        crId: String, userId: String, isPointsBox: Boolean, from: Int
    ) {
        when (from) {
            Constants.GiftDialogFrom.FROM_PARTY -> {
                //九麦房送礼图标
                if (isPointsBox) {
                    requestSingleUser(MMKVProvider.userId)
                } else {
                    requestMicUser(crId)
                }
            }

            Constants.GiftDialogFrom.FROM_CHAOBO -> {
                if (isPointsBox) {
                    requestSingleUser(MMKVProvider.userId)
                } else {
                    requestAllUser(crId, userId)
                }
            }

            Constants.GiftDialogFrom.FROM_PROFILE, Constants.GiftDialogFrom.FROM_CHAT -> {
                if (isPointsBox) {
                    requestSingleUser(MMKVProvider.userId)
                } else {
                    requestSingleUser(userId)
                }
            }
        }
    }


    private fun requestSingleUser(userId: String) {
        if (userId.isNullOrEmpty()) {
            return
        }
        val params = mutableMapOf<String, Any?>("userId" to userId)
        request<UserProfileBean>(CommonApi.API_QUERY_USER_PROFILE, Method.GET, params, onSuccess = {
            val model = MicUserModel(
                wheatPositionId = it.userId,
                wheatPositionIdHeadPortrait = it.profilePath,
                wheatPositionIdName = it.nickname,
                onMicroPhoneNumber = -1,
                checked = false
            )
            viewModelScope.launch {
                _getOnMicUsersEvent.emit(ResultState.onAppSuccess(mutableListOf(model)))
            }
        })
    }

    private fun requestAllUser(crId: String, userId: String) {
        scopeNetLife {

            val userParam = mutableMapOf<String, Any?>("userId" to userId)
            val micParam = mutableMapOf<String, Any?>("chatRoomId" to crId)
            val userRequest = asyncRequest<UserProfileBean>(
                CommonApi.API_QUERY_USER_PROFILE, Method.GET, userParam
            )

            val micRequest = asyncRequest<MutableList<MicUserModel>>(
                RoomApi.API_GET_ON_MIC_USERS,
                Method.GET,
                micParam,
            )

            val userResult = userRequest.await()
            val micUserResult = micRequest.await()

            val result = mutableListOf<MicUserModel>()
            val model = MicUserModel(
                wheatPositionId = userResult.userId,
                wheatPositionIdHeadPortrait = userResult.profilePath,
                wheatPositionIdName = userResult.nickname,
                onMicroPhoneNumber = -2,
                checked = false
            )
            result.add(model)
            result.addAll(micUserResult)
            result.removeAll { t -> t.wheatPositionId == MMKVProvider.userId }

            viewModelScope.launch {
                _getOnMicUsersEvent.emit(ResultState.onAppSuccess(result))
            }
        }
    }

    private fun requestMicUser(crId: String) {
        val param = mutableMapOf<String, Any?>("chatRoomId" to crId)
        request<MutableList<MicUserModel>>(RoomApi.API_GET_ON_MIC_USERS,
            Method.GET,
            param,
            onSuccess = {
                it.removeAll { t -> t.wheatPositionId == MMKVProvider.userId }
                viewModelScope.launch { _getOnMicUsersEvent.emit(ResultState.onAppSuccess(it)) }
            })
    }

    fun sendGift(
        crId: String,
        count: Int,
        gifts: List<GiftModel>,
        users: List<MicUserModel>,
        isCheckedAllGift: Boolean
    ) {
        val giftSource =
            if (gifts[0].isPackGift) "002" else if (gifts[0].giftOrBox == "001") "001" else "003"

        if (gifts[0].giftOrBox == "002" && gifts[0].boxType == "002") {
            openIntegralBox(crId, gifts[0], count)
        } else {
            val targetUserIds = JSONArray()
            users.forEach {
                targetUserIds.put(it.wheatPositionId)
            }
            val giveGiftInfos = JSONArray()
            gifts.forEach {
                val json = JSONObject()
                json.put("giftId", it.id)
                json.put("giftNum", if (isCheckedAllGift) it.num else count)
                json.put("mysteryBoxId", it.id)
                giveGiftInfos.put(json)
            }
            val param = mutableMapOf<String, Any?>()
            if (!crId.isNullOrEmpty()) {
                param["chatRoomId"] = crId
            }
            param["sourceUserId"] = MMKVProvider.userId
            param["targetUserIds"] = targetUserIds
            param["giveGiftInfos"] = giveGiftInfos
            param["giftSource"] = giftSource
            setApplicationValue(
                type = TypeFaceRecognition,
                value = Constants.FaceRecognitionType.CONSUMPTION.type.toString()
            )
            request(RoomApi.API_SEND_GIFT, Method.POST, param, state = _sendGiftEvent)
        }
    }

    private fun openIntegralBox(crId: String?, gift: GiftModel, count: Int) {
        val param = mutableMapOf<String, Any?>()
        param["isFree"] = gift.freeNumber > 0
        param["mysteryBoxId"] = gift.id
        param["number"] = count
        param["chatRoomId"] = crId
        request(RoomApi.API_OPEN_INTEGRAL_BOX, Method.POST, param, state = _openPointsBoxEvent)
    }


}