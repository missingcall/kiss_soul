package com.kissspace.room.viewmodel

import android.text.Editable
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ObservableField
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.scopeNetLife
import com.drake.net.Get
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.RequestCallback
import com.netease.nimlib.sdk.chatroom.ChatRoomService
import com.netease.nimlib.sdk.chatroom.model.ChatRoomMessage
import com.netease.nimlib.sdk.msg.MsgService
import com.kissspace.common.base.BaseViewModel
import com.kissspace.common.config.AppConfigKey
import com.kissspace.common.config.CommonApi
import com.kissspace.common.config.Constants
import com.kissspace.common.flowbus.Event
import com.kissspace.common.flowbus.FlowBus
import com.kissspace.common.model.*
import com.kissspace.common.util.mmkv.MMKVProvider
import com.kissspace.common.util.customToast
import com.kissspace.common.util.showLoading
import com.kissspace.common.widget.InputRoomPwdDialog
import com.kissspace.network.net.Method
import com.kissspace.network.net.request
import com.kissspace.network.result.ResultState
import com.kissspace.room.http.RoomApi
import com.kissspace.room.manager.RoomServiceManager
import kotlinx.coroutines.flow.*
import org.json.JSONArray
import com.kissspace.util.logE
import com.kissspace.util.topActivity
import java.net.URLDecoder

class LiveViewModel : BaseViewModel(), DefaultLifecycleObserver {
    val unReadCount = ObservableField<Int>()
    val sendChatBtnEnable = ObservableField(false)

    fun onChatEditChange(editable: Editable) {
        sendChatBtnEnable.set(editable.toString().isNotEmpty())
    }


    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        updateUnReadCount()
    }

    fun updateUnReadCount() {
        val nimUnReadCount = NIMClient.getService(MsgService::class.java).totalUnreadCount
        val param = mutableMapOf<String, Any?>()
        param["pageNum"] = 1
        param["pageSize"] = 1
        param["os"] = "android"
        request<SystemMessageResponse>(
            CommonApi.API_SYSTEM_MESSAGE,
            Method.GET,
            param,
            onSuccess = {
                unReadCount.set(it.total - MMKVProvider.systemMessageLastReadCount + nimUnReadCount)
            }, onError = {
                unReadCount.set(nimUnReadCount)
            })
    }

    private val _getOnLineUsersEvent =
        MutableSharedFlow<ResultState<MutableList<RoomOnLineUserListBean>>>()
    val getOnLineUsersEvent = _getOnLineUsersEvent.asSharedFlow()

    //获取用户信息事件
    private val _getUserInfoEvent = MutableSharedFlow<ResultState<UserInfoBean>>()
    val getUserInfoEvent = _getUserInfoEvent.asSharedFlow()

    //浇水事件
    private val _openWaterEvent = MutableSharedFlow<String>()
    val openWaterEvent = _openWaterEvent.asSharedFlow()

    //上麦事件
    private val _upMicEvent = MutableSharedFlow<ResultState<MicUserModel>>()
    val upMicEvent = _upMicEvent.asSharedFlow()

    //下麦事件
    private val _quitMicEvent = MutableSharedFlow<ResultState<Int>>()
    val quitMicEvent = _quitMicEvent.asSharedFlow()

    //获取房间信息事件
    private val _getRoomInfoEvent = MutableSharedFlow<ResultState<RoomInfoBean>>()
    val getRoomInfoEvent = _getRoomInfoEvent.asSharedFlow()

    private val _refreshRoomInfoEvent = MutableSharedFlow<ResultState<RoomInfoBean>>()
    val refreshRoomInfoEvent = _refreshRoomInfoEvent.asSharedFlow()

    //设置房间密码事件
    private val _setPasswordEvent = MutableSharedFlow<ResultState<Int>>()
    val setPasswordEvent = _setPasswordEvent.asSharedFlow()

    //抱下麦事件
    private val _kickOutMicEvent = MutableSharedFlow<ResultState<Int>>()
    val kickOutMicEvent = _kickOutMicEvent.asSharedFlow()

    //检查是否可以开麦
    private val _checkCanOpenMicEvent = MutableSharedFlow<ResultState<UserCardModel>>()
    val checkCanOpenMicEvent = _checkCanOpenMicEvent.asSharedFlow()

    //检查是否可以聊天
    private val _checkCanChatEvent = MutableSharedFlow<ResultState<String>>()
    val checkCanChatEvent = _checkCanChatEvent.asSharedFlow()

    //检查是否可以聊天
    private val _roomRankUserEvent = MutableSharedFlow<ResultState<RoomRankBean>>()
    val roomRankUserEvent = _roomRankUserEvent.asSharedFlow()

    //获取聊天历史记录
    private val _getHistoryChatMessageEvent = MutableSharedFlow<List<ChatRoomMessage>>()
    val getHistoryChatMessageEvent = _getHistoryChatMessageEvent.asSharedFlow()


    private val _taskRewardListEvent = MutableSharedFlow<ResultState<List<TaskRewardModel>>>()
    val taskRewardListEvent = _taskRewardListEvent.asSharedFlow()

    private val _getTaskPointsListEvent = MutableSharedFlow<ResultState<Boolean>>()
    val getTaskPointsListEvent = _getTaskPointsListEvent.asSharedFlow()


    private fun createRoom(roomType: String) {
        val param = mutableMapOf<String, Any?>()
        param["roomTagCategory"] = roomType
        request(RoomApi.API_CREATE_ROOM, Method.POST, param, state = _getRoomInfoEvent)
    }

    private fun joinRoom(
        crId: String? = null,
        password: String? = null,
        roomType: String? = null,
        stochastic: String? = null,
    ) {
        val param = mutableMapOf<String, Any?>()
        param["crId"] = crId
        param["roomPwd"] = password
        param["roomType"] = roomType
        param["stochastic"] = stochastic
        request(RoomApi.API_JOIN_ROOM, Method.POST, param, state = _getRoomInfoEvent)
    }

    private fun refreshRoom(crId: String) {
        val param = mutableMapOf<String, Any?>()
        param["crId"] = crId
        param["userId"] = MMKVProvider.userId
        request(RoomApi.API_REFRESH_ROOM, Method.POST, param, state = _refreshRoomInfoEvent)
    }

    fun getOnLineUsers(crId: String) {
        val param = mutableMapOf<String, Any?>("chatRoomId" to crId)
        request(RoomApi.API_GET_ROOM_ONLINE_USER, Method.GET, param, state = _getOnLineUsersEvent)
    }

    fun getWaterGameUrl(crId: String?) {
        scopeNetLife {
            val gameUrl = Get<String>(CommonApi.API_GET_APP_CONFIG) {
                param("configKey", AppConfigKey.KEY_WATER_GAME_URL)
            }.await()
            val userInfo = Get<UserInfoBean>(CommonApi.API_GET_USER_INFO).await()
            val url = "${
                URLDecoder.decode(
                    gameUrl, "utf-8"
                )
            }?token=${MMKVProvider.loginResult?.token}&effect=${MMKVProvider.isShowWaterAnimation}&chatRoomId=${crId}"
            logE("waterUrl$url")
            _openWaterEvent.emit(url)
        }
    }

    fun checkCanOpenMic(crId: String) {
        val params =
            mutableMapOf<String, Any?>("userId" to MMKVProvider.userId, "chatRoomId" to crId)
        request(
            RoomApi.API_GET_USER_PROFILE_INFO, Method.GET, params, state = _checkCanOpenMicEvent
        )
    }

    fun checkCanChat(crId: String, onSuccess: () -> Unit) {
        val params =
            mutableMapOf<String, Any?>("userId" to MMKVProvider.userId, "chatRoomId" to crId)
        request<UserCardModel>(RoomApi.API_GET_USER_PROFILE_INFO, Method.GET, params, onSuccess = {
            if (!it.isMuted) {
                onSuccess()
            } else {
                customToast("您已被禁言")
            }
        }, onError = {
            customToast(it.message)
        })
    }

    /**
     *  主动上麦
     *  @param crId 房间id
     *  @param micIndex 要上的麦位下标 如果是点击左下角上麦可以不用传
     */
    fun upMic(crId: String, roomType: String, micIndex: Int? = null) {
        val param = mutableMapOf<String, Any?>()
        param["crId"] = crId
        param["microphoneNumber"] = micIndex
        param["roomTagCategory"] = roomType
        request(RoomApi.API_UP_MIC, Method.POST, param, state = _upMicEvent)
    }

    /**
     *  主动下麦
     *  @param crId 房间id
     *  @param micIndex 要下的麦位下标
     */
    fun quitMic(crId: String, micIndex: Int?, roomType: String) {
        val param = mutableMapOf<String, Any?>()
        param["crId"] = crId
        param["microphoneNumber"] = micIndex
        param["roomTagCategory"] = roomType
        request(RoomApi.API_QUIT_MIC, Method.POST, param, state = _quitMicEvent)
    }

    fun lockMic(isLockAll: Boolean, index: Int, crId: String?, roomType: String) {
        val param = mutableMapOf<String, Any?>()
        param["chatRoomId"] = crId
        param["microphoneNumber"] = if (isLockAll) -1 else index
        param["roomTagCategory"] = roomType
        request<Int>(RoomApi.API_LOCK_MIC, Method.POST, param, onSuccess = {
            customToast("操作成功")
        }, onError = {
            customToast(it.message)
        })
    }

    fun unLockMic(isLockAll: Boolean, index: Int, crId: String?, roomType: String) {
        val param = mutableMapOf<String, Any?>()
        param["chatRoomId"] = crId
        param["microphoneNumber"] = if (isLockAll) -1 else index
        param["roomTagCategory"] = roomType
        request<Int>(RoomApi.API_UNLOCK_MIC, Method.POST, param, onSuccess = {
            customToast("操作成功")
        })
    }

    fun getUserInfo() {
        request(CommonApi.API_GET_USER_INFO, Method.GET, state = _getUserInfoEvent)
    }


    fun kickOutMic(crId: String, userId: String, roomType: String, position: Int) {
        val param = mutableMapOf<String, Any?>()
        param["chatRoomId"] = crId
        param["userId"] = userId
        param["roomTagCategory"] = roomType
        param["microphoneNumber"] = position
        request(RoomApi.API_KICK_MIC, Method.POST, param, state = _kickOutMicEvent)
    }

//    fun isCollectRoom(crId: String) {
//        val param = mutableMapOf<String, Any?>()
//        param["chatRoomId"] = crId
//        param["userId"] = MMKVProvider.userId
//        request(RoomApi.API_IS_COLLECT_ROOM, Method.GET, param, state = _checkCollectEvent)
//    }
//
//    fun collectRoom(crId: String?) {
//        val param = mutableMapOf<String, Any?>()
//        param["chatRoomId"] = crId
//        param["type"] = "001"
//        request(RoomApi.API_COLLECT_ROOM, Method.POST, param, state = _collectEvent)
//    }

    //获取房间积分任务列表
    fun getChatRoomTaskRewardList() {
        val param = mutableMapOf<String, Any?>()
        request(
            RoomApi.API_GET_TASK_REWARD_LIST, Method.GET, param, state = _taskRewardListEvent
        )
    }


    fun getRoomRankList(crId: String, rankCycle: String, rankType: String) {
        val param = mutableMapOf<String, Any?>()
        param["chatRoomId"] = crId
        param["rankCycle"] = rankCycle
        param["rankType"] = rankType
        request(
            RoomApi.API_ROOM_RANK_USER, Method.GET, param, state = _roomRankUserEvent
        )
    }

    fun getHistoryChatMessage(crId: String) {
        val startTime = System.currentTimeMillis()
        NIMClient.getService(ChatRoomService::class.java).pullMessageHistory(crId, startTime, 20)
            .setCallback(object : RequestCallback<List<ChatRoomMessage>> {
                override fun onSuccess(param: List<ChatRoomMessage>?) {
                    param?.let {
                        FlowBus.post(Event.ChatRoomHistoryMessageEvent(it))
                    }
                }

                override fun onFailed(code: Int) {

                }

                override fun onException(exception: Throwable?) {

                }
            })
    }

    //领取所有积分
    fun getAllIntegralList(taskIdList: MutableList<String?>) {
        val param = mutableMapOf<String, Any?>()
        val taskIds = JSONArray(taskIdList)
        logE("taskIds$taskIds")
        param["taskIds"] = taskIds
        request(
            RoomApi.API_GET_ALL_INTEGRAL, Method.POST, param, state = _getTaskPointsListEvent
        )
    }


    fun handleRoomInfo(crId: String?, roomType: String?, stochastic: String?, userId: String?) {
        val roomInfo = RoomServiceManager.roomInfo
        when {
            stochastic == "001" -> {
                //随机进房间
                showLoading()
                if (roomInfo != null) {
                    RoomServiceManager.release()
                }
                joinRoom(roomType = roomType, stochastic = stochastic)
            }

            crId.isNullOrEmpty() && !roomType.isNullOrEmpty() || (roomInfo != null && roomInfo.roomTagCategory == roomType && roomInfo.houseOwnerId == MMKVProvider.userId && crId.isNullOrEmpty()) -> {
                //进自己房间 不需要校验任何信息
                showLoading()
                if (roomInfo == null) {
                    createRoom(roomType!!)
                } else if (roomInfo != null && roomInfo.roomTagCategory == roomType && roomInfo.houseOwnerId == MMKVProvider.userId) {
                    refreshRoom(crId = roomInfo!!.crId)
                } else {
                    RoomServiceManager.release()
                    createRoom(roomType = roomType!!)
                }
            }

            else -> {
                //进别人房间
                if (crId == roomInfo?.crId) {
                    //刷新房间，不用校验
                    showLoading()
                    refreshRoom(crId!!)
                } else {
                    RoomServiceManager.release()
                    checkRoom(crId!!)
                }
            }
        }
    }


    private fun checkRoom(crId: String) {
        checkRoomInfo(crId) {
            when {
                it.roomPwd.isNotEmpty() && it.role == Constants.ROOM_USER_TYPE_NORMAL -> {
                    //有密码
                    val dialog = InputRoomPwdDialog()
                    dialog.initData(it.roomPwd) { pwd ->
                        showLoading()
                        joinRoom(crId, pwd, it.roomTagCategory)
                    }
                    dialog.show((topActivity as AppCompatActivity).supportFragmentManager)
                }

                it.kickOutOfTheRoom -> {
                    customToast("您被踢出房间未满15分钟")
                }

                else -> {
                    showLoading()
                    joinRoom(crId, roomType = it.roomTagCategory)
                }
            }
        }
    }

    private fun checkRoomInfo(crId: String, onSuccess: (CheckRoomInfoModel) -> Unit) {
        val params = mutableMapOf<String, Any?>("chatRoomId" to crId)
        request<CheckRoomInfoModel>(CommonApi.API_QUERY_ROOM_PASSWORD,
            Method.GET,
            params,
            onSuccess = {
                onSuccess(it)
            },
            onError = { customToast(it.message) })
    }
}