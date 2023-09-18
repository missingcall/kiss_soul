package com.kissspace.message.viewmodel

import androidx.databinding.ObservableField
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.scopeNetLife
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.ArrayUtils.add
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.TimeUtils
import com.drake.net.Get
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.Observer
import com.netease.nimlib.sdk.RequestCallbackWrapper
import com.netease.nimlib.sdk.msg.MsgService
import com.netease.nimlib.sdk.msg.MsgServiceObserve
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.msg.model.QueryDirectionEnum
import com.netease.nimlib.sdk.msg.model.RecentContact
import com.netease.nimlib.sdk.uinfo.UserService
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo
import com.kissspace.common.base.BaseViewModel
import com.kissspace.common.config.Constants
import com.kissspace.common.flowbus.Event
import com.kissspace.common.flowbus.FlowBus
import com.kissspace.common.model.ChatListModel
import com.kissspace.common.model.DynamicMessageNotice
import com.kissspace.common.model.ItemMessageMenu
import com.kissspace.common.model.LoveWallResponse
import com.kissspace.common.model.SystemMessageResponse
import com.kissspace.common.util.getFriendlyTimeSpanByNow
import com.kissspace.common.util.mmkv.MMKVProvider
import com.kissspace.common.util.parseUserExtension
import com.kissspace.message.http.MessageApi
import com.kissspace.module_message.R
import com.kissspace.network.net.Method
import com.kissspace.network.net.request
import com.kissspace.network.result.ResultState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

class MessageViewModel : BaseViewModel(), DefaultLifecycleObserver {
    //会话锚点，用作分页
    var anchor: RecentContact? = null

    //未读消息数
    val unReadCount = ObservableField(0)

    private val _getListDataEvent = MutableSharedFlow<ResultState<MutableList<Any>>>()
    val getListDataEvent = _getListDataEvent.asSharedFlow()


    private val _getRecentMsgListEvent = MutableSharedFlow<ResultState<List<ChatListModel>>>()
    val getRecentMsgListEvent = _getRecentMsgListEvent.asSharedFlow()

    private val _updateRecentEvent = MutableSharedFlow<ResultState<List<ChatListModel>>>()
    val updateRecentEvent = _updateRecentEvent.asSharedFlow()

    private val _bannerEvent = MutableSharedFlow<ResultState<LoveWallResponse>>()
    val bannerEvent = _bannerEvent.asSharedFlow()


    private val _systemMessageEvent = MutableSharedFlow<ResultState<SystemMessageResponse>>()
    val systemMessageEvent = _systemMessageEvent.asSharedFlow()


    private val _dynamicMessageCountEvent = MutableSharedFlow<ResultState<DynamicMessageNotice>>()
    val dynamicMessageCountEvent = _dynamicMessageCountEvent.asSharedFlow()

    private val recentContactObserver = Observer<List<RecentContact>> {
        if (it != null && it.isNotEmpty()) {
            parseData(it, true)
            FlowBus.post(Event.RefreshUnReadMsgCount)
            updateUnReadCount()
        }
    }

    fun getMessageMenu():MutableList<ItemMessageMenu>{
         val messageList : MutableList<ItemMessageMenu> = ArrayList()
        return messageList.apply {
            add(ItemMessageMenu("真爱墙", R.mipmap.message_menu_icon1))
            add(ItemMessageMenu("系统通知",R.mipmap.message_menu_icon2))
            add(ItemMessageMenu("互动消息",R.mipmap.message_menu_icon3))
            add(ItemMessageMenu("任务消息",R.mipmap.message_menu_icon4))
        }
    }


    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        NIMClient.getService(MsgServiceObserve::class.java)
            .observeRecentContact(recentContactObserver, true)

    }


    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        NIMClient.getService(MsgServiceObserve::class.java)
            .observeRecentContact(recentContactObserver, false)
    }


    fun requestSystemMessage() {
        val param = mutableMapOf<String, Any?>()
        param["pageNum"] = 1
        param["pageSize"] = 1
        param["os"] = "android"
        request(MessageApi.API_SYSTEM_MESSAGE, Method.GET, param, state = _systemMessageEvent)
    }

    fun requestBannerData() {
        val param = mutableMapOf<String, Any?>()
        param["recodeType"] = "002"
        param["pageNum"] = 1
        param["pageSize"] = 5
        request(MessageApi.API_LOVE_WALL_LIST, Method.GET, param, state = _bannerEvent)
    }


    /**
     * 查询历史会话列表
     */
    fun queryRecentMessage() {
        NIMClient.getService(MsgService::class.java)
            .queryRecentContacts(anchor, QueryDirectionEnum.QUERY_OLD, 20)
            .setCallback(object : RequestCallbackWrapper<List<RecentContact>>() {
                override fun onResult(
                    code: Int,
                    result: List<RecentContact>?,
                    exception: Throwable?
                ) {
                    if (!result.isNullOrEmpty()) {
                        parseData(result, false)
                    } else {
                        viewModelScope.launch {
                            _getRecentMsgListEvent.emit(ResultState.onAppSuccess(mutableListOf()))
                        }
                    }
                }
            })
    }

    fun requestDynamicMessageCount() {
        val param = mutableMapOf<String, Any?>()
        request(
            MessageApi.API_SYSTEM_MESSAGE_COUNT,
            Method.GET,
            param,
            state = _dynamicMessageCountEvent
        )
    }

    private fun parseData(list: List<RecentContact>, isUpdate: Boolean) {
        val chatList = mutableListOf<ChatListModel>()
        val accounts = list.map { it.contactId }
        anchor = list.last()
        NIMClient.getService(UserService::class.java).fetchUserInfo(accounts)
            .setCallback(object : RequestCallbackWrapper<List<NimUserInfo>>() {
                override fun onResult(
                    code: Int,
                    result: List<NimUserInfo>?,
                    exception: Throwable?
                ) {
                    if (result != null) {
                        list.forEachIndexed { index, recentContact ->
                            result[index].extension
                            val extension = parseUserExtension(result[index].extension)
                            val model = ChatListModel(
                                fromAccount = recentContact.contactId,
                                nickname = result[index].name,
                                avatar = result[index].avatar,
                                content = recentContact.getRecentContent(),
                                date = getFriendlyTimeSpanByNow(recentContact.time),
                                unReadCount = recentContact.unreadCount,
                                followRoomId = extension?.chatRoomId,
                                onlineState = extension?.onlineState
                            )
                            chatList.add(model)
                        }

                        viewModelScope.launch {
                            if (isUpdate) {
                                _updateRecentEvent.emit(ResultState.onAppSuccess(chatList))
                            } else {
                                _getRecentMsgListEvent.emit(ResultState.onAppSuccess(chatList))
                            }
                        }
                    }
                }
            })
    }

    fun updateUnReadCount() {
        unReadCount.set(NIMClient.getService(MsgService::class.java).totalUnreadCount + MMKVProvider.systemMessageUnReadCount)
    }

    fun unReadSystemMessage() {
        val param = mutableMapOf<String, Any?>()
        param["pageNum"] = 1
        param["pageSize"] = 1
        param["os"] = "android"
        request<SystemMessageResponse>(
            MessageApi.API_SYSTEM_MESSAGE,
            Method.GET,
            param,
            onSuccess = {
                MMKVProvider.systemMessageLastReadCount = it.total
                MMKVProvider.systemMessageUnReadCount = 0
                FlowBus.post(Event.RefreshUnReadMsgCount)
            })
    }

    /**
     * 获取会话最近消息文本
     */
    fun RecentContact.getRecentContent(): String? {
        return if (this.msgType == MsgTypeEnum.custom) {
            val msg = NIMClient.getService(MsgService::class.java)
                .queryLastMessage(this.contactId, SessionTypeEnum.P2P)
            if (msg != null && msg.attachStr != null) {
                val json = JSONObject(msg.attachStr)
                when (json.getString("type")) {
                    Constants.IMMessageType.MSG_CHAT_SEND_GIFT -> "[礼物]"
                    Constants.IMMessageType.MSG_CHAT_SEND_EMOJI -> "[表情]"
                    else -> null
                }
            } else {
                null
            }
        } else {
            this.content
        }
    }
}