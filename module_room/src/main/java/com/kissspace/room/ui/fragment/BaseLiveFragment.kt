package com.kissspace.room.ui.fragment

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.angcyo.tablayout.DslTabLayout
import com.didi.drouter.api.DRouter
import com.drake.brv.utils.*
import com.kissspace.util.dp
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.Observer
import com.netease.nimlib.sdk.RequestCallback
import com.netease.nimlib.sdk.StatusCode
import com.netease.nimlib.sdk.chatroom.ChatRoomMessageBuilder
import com.netease.nimlib.sdk.chatroom.ChatRoomService
import com.netease.nimlib.sdk.chatroom.ChatRoomServiceObserver
import com.netease.nimlib.sdk.chatroom.model.ChatRoomMessage
import com.netease.nimlib.sdk.chatroom.model.ChatRoomStatusChangeData
import com.netease.nimlib.sdk.msg.MsgService
import com.netease.nimlib.sdk.msg.MsgServiceObserve
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum
import com.netease.nimlib.sdk.msg.model.BroadcastMessage
import com.netease.nimlib.sdk.msg.model.NIMAntiSpamOption
import com.netease.nimlib.sdk.msg.model.RecentContact
import com.kissspace.common.base.BaseFragment
import com.kissspace.common.config.AppConfigKey
import com.kissspace.common.config.CommonApi
import com.kissspace.common.config.Constants
import com.kissspace.common.config.Constants.IMMessageType.MSG_REFRESH_TREE
import com.kissspace.common.ext.*
import com.kissspace.common.flowbus.Event
import com.kissspace.common.flowbus.FlowBus
import com.kissspace.common.http.getAppConfigByKey
import com.kissspace.common.http.getWechatShareUrl
import com.kissspace.common.model.*
import com.kissspace.common.model.immessage.*
import com.kissspace.common.provider.IRoomProvider
import com.kissspace.common.router.RouterPath
import com.kissspace.common.router.jump
import com.kissspace.common.util.*
import com.kissspace.common.util.mmkv.MMKVProvider
import com.kissspace.common.widget.CommonConfirmDialog
import com.kissspace.module_room.R
import com.kissspace.network.net.Method
import com.kissspace.network.net.request
import com.kissspace.room.adapter.RoomChatRecyclerAdapter
import com.kissspace.room.config.RoomSettingClickType
import com.kissspace.room.http.RoomApi
import com.kissspace.room.interfaces.RoomActionCallBack
import com.kissspace.room.interfaces.RoomBroadCastCallBack
import com.kissspace.room.manager.RoomServiceManager
import com.kissspace.room.ui.activity.BaseLiveActivity
import com.kissspace.room.ui.activity.LiveAudioActivity
import com.kissspace.room.widget.InviteUserUpMicDialog
import com.kissspace.room.widget.MicActionDialog
import com.kissspace.room.widget.RoomActionBarView
import com.kissspace.room.widget.RoomBlackListDialog
import com.kissspace.room.widget.RoomBroadcastDialog
import com.kissspace.room.widget.RoomBroadcastView
import com.kissspace.room.widget.RoomInfoDialog
import com.kissspace.room.widget.RoomMicrophoneLayout
import com.kissspace.room.widget.RoomMusicDialog
import com.kissspace.room.widget.RoomNoticePopup
import com.kissspace.room.widget.RoomPKTypeDialog
import com.kissspace.room.widget.RoomPasswordDialog
import com.kissspace.room.widget.RoomPredictionDialog
import com.kissspace.room.widget.RoomPredictionView
import com.kissspace.room.widget.RoomQuitPopup
import com.kissspace.room.widget.RoomSettingDialogV2
import com.kissspace.room.widget.RoomShareDialog
import com.kissspace.room.widget.RoomUserProfileDialog
import com.kissspace.room.widget.RoomVideoSettingDialog
import com.kissspace.room.widget.SettingManagerDialog
import com.kissspace.room.widget.TaskRewardListDialogFragment
import com.kissspace.util.loadImage
import com.kissspace.util.logE
import com.kissspace.util.orZero
import com.kissspace.util.resToColor
import com.kissspace.util.resToString
import com.zhpan.bannerview.BannerViewPager
import com.zhpan.bannerview.BaseBannerAdapter
import com.zhpan.bannerview.BaseViewHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.lang.Exception
import java.net.URL
import kotlin.text.isNotEmpty

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/12/15 22:13
 * @Description: 房间fragment基类 封装聊天列表和麦位列表等一些公共UI
 *
 */
abstract class BaseLiveFragment(layoutId: Int) : BaseFragment(layoutId),
    Observer<List<ChatRoomMessage>> {
    //聊天消息类型-普通
    private val CHAT_MSG_TYPE_NORMAL = 0

    //聊天消息类型-公告
    private val CHAT_MSG_TYPE_NOTICE = 1

    //聊天消息类型-进场消息
    private val CHAT_MSG_TYPE_USER_IN = 2

    //聊天消息类型-表情包
    private val CHAT_MSG_TYPE_USER_EMOJI = 3

//    //麦位用户列表
//    protected val mMicUserList = mutableListOf<MicUserModel>()

    //麦位操作响应事件
    private lateinit var mMicAction: MicActionDialog.ClickType.(MicUserModel) -> Unit

    //最近会话变更观察者
    private val recentContactObserver = Observer<List<RecentContact>> {
        updateUnReadCount()
    }

    //广播消息观察者
    private val broadcastObserver = Observer<BroadcastMessage> {
        updateUnReadCount()
    }


    private val onLineStateObserver = Observer<ChatRoomStatusChangeData> {
        if (it.status == StatusCode.NET_BROKEN) {
            customToast("网络断开，请检查网络")
        }
        if (it.status == StatusCode.LOGINED) {
            //重连成功 刷新麦位接口
            refreshMicUsers()
        }
    }

    //处理图片与语音消息接收，监听附件下载成功后再插入消息
    private val messageStatusObserver = Observer<ChatRoomMessage> {
        if (!isAdded) {
            return@Observer
        }
        if (it.yidunAntiSpamRes != null) {
            customToast("发送内容包含违禁内容，请重新发送")
            NIMClient.getService(MsgService::class.java).deleteChattingHistory(it)
            return@Observer
        }
        if (it.msgType == MsgTypeEnum.custom) {
            val attachment = it.attachment as BaseAttachment
            if (attachment.type == Constants.IMMessageType.MSG_TYPE_ROOM_CHAT_CONTENT && it.status == MsgStatusEnum.success) {
                val model = RoomChatMessageModel(
                    userId = getUserInfo()!!.userId,
                    profilePath = getUserInfo()!!.profilePath,
                    nickname = getUserInfo()!!.nickname,
                    content = it.content,
                    contentSpan = buildSpannedString { append(it.content) },
                    wealthLevel = getUserInfo()!!.consumeLevel,
                    charmLevel = getUserInfo()!!.charmLevel,
                    type = CHAT_MSG_TYPE_NORMAL,
                    privilege = getUserInfo()!!.privilege,
                    medalList = getUserInfo()!!.medalList,
                    headwearUrl = getUserInfo()!!.headwearUrl,
                    messageKindList = mutableListOf("001", "002"),
                    chatTabIndex = getChatTabIndex()
                )
                sendChatTask()
                RoomServiceManager.addHistoryMessage(it)
                insertChatModel(model)
            }
        }
    }


    private val onMicrophoneClick: (MicUserModel) -> Unit = {
        when {
            it.wheatPositionId.isNullOrEmpty() -> {
                //麦位空
                if (isNormalUser()) {
                    //普通用户直接上麦
                    if (it.lockWheat) customToast("该麦位已上锁", true)
                    else upMic(it.onMicroPhoneNumber)
                } else {
                    //否则弹出权限窗
                    val dialog = MicActionDialog()
                    dialog.initData(
                        it,
                        getRoomInfo()?.userRole,
                        getRoomInfo()!!.roomTagCategory,
                        mMicAction
                    )
                    dialog.show(childFragmentManager)
                }
            }

            it.wheatPositionId == MMKVProvider.userId -> {
                val dialog = MicActionDialog()
                dialog.initData(
                    it,
                    getRoomInfo()?.userRole,
                    getRoomInfo()!!.roomTagCategory,
                    mMicAction
                )
                dialog.show(childFragmentManager)
            }

            else -> {
                //别人在麦上
                if (isNormalUser()) {
                    //自己是普通用户直接弹出名片
                    RoomUserProfileDialog.newInstance(
                        it.wheatPositionId,
                        getRoomInfo()!!.userRole,
                        getRoomInfo()!!.crId
                    ).show(childFragmentManager)
                } else {
                    //否则弹出权限操作窗
                    val dialog = MicActionDialog()
                    dialog.initData(
                        it,
                        getRoomInfo()?.userRole,
                        getRoomInfo()!!.roomTagCategory,
                        mMicAction
                    )
                    dialog.show(childFragmentManager)
                }
            }
        }
    }


    override fun initView(savedInstanceState: Bundle?) {
        RoomServiceManager.init(getRoomInfo())
        getRoomInfo().wheatPositionList.forEach {
            it.isShowIncome = getRoomInfo().charmOnOff
        }
//        mMicUserList.addAll(getRoomInfo().wheatPositionList)
        //注册消息监听
        registerMessageObserver()
        //初始化actionBar
        initActionBar()
        //初始化广播
        initBroadCast()
        //初始化麦克风recyclerView
        getMicrophoneLayout().initRecyclerView(getRoomInfo().wheatPositionList, onMicrophoneClick)
        //初始话聊天列表
        initChatRecyclerView()
        //初始化麦位操作
        initMicActionEvents()
        //初始化一些请求
        initData()
    }

    private fun initBroadCast() {
        getBroadcastView().setCallBack(object : RoomBroadCastCallBack {
            override fun showBroadCast(costCoin:Int,start:Boolean) {
                RoomBroadcastDialog.newInstance(costCoin,start).show(childFragmentManager)
            }
        })
        if (getRoomInfo()?.largeScreenMessageResponse != null){
            getBroadcastView().initData(getRoomInfo()!!.largeScreenMessageResponse)
        }
    }

    private fun initData() {
        getChatTabLayout()?.setCurrentItem(0)
        //查询排麦列表
        requestMicQueueList()
        //查询最近的预言游戏
        requestRecentPrediction()
        //查询房间活动
        requestActivities()
        //查询聊天历史记录
        if (isOldRoom()) {
            requestHistoryMessage()
        }
    }


    /**
     *  获取排麦用户列表，将数量显示在排麦按钮上
     */
    private fun requestMicQueueList() {

        val params = mutableMapOf<String, Any?>("chatRoomId" to getRoomInfo().crId)
        request<List<MicQueueUserModel>>(RoomApi.API_GET_MIC_QUEUE_LIST,
            Method.GET,
            params,
            onSuccess = {
                updateMicQueueAmount(it.size)
            })
    }

    /**
     *  查询积分竞猜记录，将最近一条显示在右上角
     */
    private fun requestRecentPrediction() {
//        val param = mutableMapOf<String, Any?>("chatRoomId" to getRoomInfo().crId)
//        request<List<PredictionListBean>>(RoomApi.API_GET_PREDICTION_LIST,
//            Method.GET,
//            param,
//            onSuccess = {
//                val model = it.find { that -> that.state == "001" }
//                if (model != null) {
//                    val message = CreateBetMessage(
//                        model.integralGuessId,
//                        model.integralGuessTitle,
//                        model.validTime,
//                        model.leftBetAmount,
//                        model.rightBetAmount
//                    )
//                    showPredictionView(message)
//                } else {
//                    getPredictionView().visibility = View.INVISIBLE
//                }
//            })
    }


    private fun refreshMicUsers() {
        val param = mutableMapOf<String, Any?>()
        param["crId"] = getRoomInfo().crId
        param["userId"] = MMKVProvider.userId
        request<RoomInfoBean>(RoomApi.API_REFRESH_ROOM, Method.POST, param, onSuccess = {
            //todo 刷新麦位信息
//            getRoomInfo().wheatPositionList = it.wheatPositionList
//            getMicrophoneLayout().mutable.clear()
//            getMicrophoneLayout().addModels(it.wheatPositionList)
//            if (!isUserOnMic(MMKVProvider.userId)) {
//                RoomServiceManager.stopPublishStream()
//                updateMicStatus()
//            }
        })
    }


    /**
     * 查询房间活动
     */
    private fun requestActivities() {
        val api =
            if (getRoomInfo()?.roomTagCategory == Constants.ROOM_TYPE_PARTY) AppConfigKey.KEY_ROOM_PARTY_ACTIVITY else AppConfigKey.KEY_ROOM_CHAOBO_ACTIVITY
        getAppConfigByKey<List<RoomActivityBean>>(api) {
            if (it.isNullOrEmpty()) {
                getActivityBannerView().visibility = View.GONE
            } else {
                getActivityBannerView().visibility = View.VISIBLE
                getActivityBannerView().apply {
                    adapter = object : BaseBannerAdapter<Any>() {
                        override fun bindData(
                            holder: BaseViewHolder<Any>?, data: Any?, position: Int, pageSize: Int
                        ) {
                            holder?.findViewById<ImageView>(R.id.image)
                                ?.loadImage((data as RoomActivityBean)?.url)
                            holder?.findViewById<LinearLayout>(R.id.root)?.safeClick {
                                val model = data as RoomActivityBean
                                handleSchema(model.schema)
                            }
                        }

                        override fun getLayoutId(viewType: Int) =
                            R.layout.room_layout_acitivity_item

                    }
                    setIndicatorVisibility(View.VISIBLE)
                    setIndicatorSliderColor(
                        com.kissspace.module_common.R.color.color_1AFFFFFF.resToColor(),
                        com.kissspace.module_common.R.color.common_white.resToColor()
                    )
                    setIndicatorSliderWidth(5.dp.toInt())
                    setIndicatorMargin(0, 0, 0, 0)
                    registerLifecycleObserver(lifecycle)
                    refreshData(it.filter { item -> item.state == "001" && item.os == "001" })
                }.create()
            }
        }
    }


    private fun requestHistoryMessage() {
        //因为查询的是当前房间和系统房间的消息，所以要按照时间戳排序
        RoomServiceManager.historyMessage.sortBy { t -> t.time }
        //过滤掉消息时间小于第一次进房间时间的消息
        RoomServiceManager.historyMessage.removeAll { t -> t.time < RoomServiceManager.firstJoinRoomTime }
        parseHistoryMessage(RoomServiceManager.historyMessage)
    }


    private fun parseHistoryMessage(messages: MutableList<ChatRoomMessage>) {
        val messageList = mutableListOf<RoomChatMessageModel>()
        messages?.forEach {
            try {
                val json = JSONObject(it.attachStr)
                val type = json.getString("type")
                val data = BaseAttachment(type, json.get("data"))
                if (type == Constants.IMMessageType.MSG_EMOJI) {
                    val message = parseCustomMessage<RoomChatMessageModel>(data.data)
                    message.type = CHAT_MSG_TYPE_USER_EMOJI
                    message.messageKindList = mutableListOf("001", "002")
                    messageList.add(message)
                }
                if (type == Constants.IMMessageType.MSG_TYPE_ROOM_CHAT_CONTENT) {
                    val message = parseCustomMessage<RoomChatMessageModel>(data.data)
                    message.messageKindList = mutableListOf("001", "002")
                    message.contentSpan = buildSpannedString { append(message.content) }
                    messageList.add(message)
                }
                if (type == Constants.IMMessageType.MSG_TYPE_USER_IN) {
                    val data = parseCustomMessage<UserEnterMessage>(data.data)
                    messageList.add(parseUserJoinMessage(data))
                }
                if (type == Constants.IMMessageType.MSG_TYPE_GIFT) {
                    val data = parseCustomMessage<GiftMessage>(data.data)
                    messageList.addAll(
                        if (data.giftSource == "003") {
                            parseBoxGiftMessage(data)
                        } else {
                            parseGiftMessage(data)
                        }
                    )
                }
                if (type == Constants.IMMessageType.MSG_CHAT_WATER) {
                    val data = parseCustomMessage<WaterMessage>(data.data)
                    if (MMKVProvider.room_water_info_show) {
                        messageList.add(parseWaterMessage(data))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        insertChatModel(messageList, true)
    }


    private fun registerMessageObserver() {
        //注册IM消息监听
        NIMClient.getService(ChatRoomServiceObserver::class.java).observeReceiveMessage(this, true)
        //注册最近会话变更监听
        NIMClient.getService(MsgServiceObserve::class.java)
            .observeRecentContact(recentContactObserver, true)
        NIMClient.getService(MsgServiceObserve::class.java)
            .observeBroadcastMessage(broadcastObserver, true)
        NIMClient.getService(ChatRoomServiceObserver::class.java)
            .observeMsgStatus(messageStatusObserver, true)
        NIMClient.getService(ChatRoomServiceObserver::class.java)
            .observeOnlineStatus(onLineStateObserver, true)
    }

    private fun initActionBar() {
        getActionBarView().setMarginStatusBar()
        getActionBarView().initData(getRoomInfo())
        getActionBarView().setCallBack(object : RoomActionCallBack {
            override fun collect() {
                val param = mutableMapOf<String, Any?>()
                param["chatRoomId"] = getRoomInfo().crId
                param["type"] = "001"
                request<Int>(RoomApi.API_COLLECT_ROOM, Method.POST, param, onSuccess = {
                    getRoomInfo().collections = true
                    getActionBarView().hideCollect(getRoomInfo())
                })
            }

            override fun showNotice() {
                RoomNoticePopup(
                    requireContext(), getRoomInfo().roomAffiche
                ).showPopupWindow(getActionBarView().getNoticeView())
            }

            override fun share() {
                RoomShareDialog {
                    if (it == Constants.SharePlatform.WECHAT.type) {
                        //判断是否安装微信
                        getWechatShareUrl { url ->
                            ShareUtil.shareWechat(
                                getRoomInfo()?.roomTitle.orEmpty(),
                                "快来房间[${
                                    if (getRoomInfo()?.beautifulId.orEmpty()
                                            .isNotEmpty()
                                    ) getRoomInfo()?.beautifulId else getRoomInfo()?.showId
                                }]互动，和我一起语音吧",
                                url,
                                getRoomInfo()?.roomIcon.orEmpty(),
                                requireActivity()
                            )
                        }
                    } else if (it == Constants.SharePlatform.WECHAT_FRIEND.type) {
                        getWechatShareUrl { url ->
                            ShareUtil.shareWechatFriend(
                                getRoomInfo()?.roomTitle.orEmpty(),
                                url,
                                getRoomInfo()?.roomIcon.orEmpty(),
                                requireActivity()
                            )
                        }

                    }
                }.show(parentFragmentManager, "RoomShareDialog")
            }

            override fun quit() {
                //退出房间
                RoomQuitPopup(requireContext(), onQuit = {
                    (requireParentFragment() as LiveAudioFragment).exitRoom()
                }, onMinimize = {
                    (requireParentFragment().requireActivity() as LiveAudioActivity).handleBackPressed()
                }).showPopupWindow(getActionBarView().getQuitView())
            }

            override fun showProfile() {
                RoomUserProfileDialog.newInstance(
                    getRoomInfo()!!.houseOwnerId, getRoomInfo()!!.userRole, getRoomInfo()!!.crId
                ).show(parentFragmentManager)
            }

            override fun back() {
                (requireParentFragment().requireActivity() as LiveAudioActivity).handleBackPressed()
            }
        })
    }


    private fun getMicRecyclerLayoutNormalLayoutManager(): GridLayoutManager {
        return if (getRoomInfo()!!.roomTagCategory == Constants.ROOM_TYPE_PARTY) {
            GridLayoutManager(requireContext(), 4).apply {
                spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return if (position == 0) 4 else 1
                    }
                }
            }
        } else {
            GridLayoutManager(requireContext(), 1)
        }
    }

    private fun getMicRecyclerPKInfightingLayoutManager(): GridLayoutManager =
        GridLayoutManager(requireContext(), 2)


    private fun initChatRecyclerView() {
        //添加一条系统消息
        val systemMessage = RoomChatMessageModel(
            content = R.string.room_message_notice.resToString(),
            type = CHAT_MSG_TYPE_NOTICE,
            messageKindList = mutableListOf("001", "002")
        )
        if (!isOldRoom()) {
            RoomServiceManager.allMessageList.add(systemMessage)
        }
        val adapter = RoomChatRecyclerAdapter().apply {
            addType<RoomChatMessageModel> {
                when (type) {
                    CHAT_MSG_TYPE_NOTICE -> R.layout.room_chat_item_notice
                    CHAT_MSG_TYPE_USER_IN -> R.layout.room_chat_item_user_in
                    CHAT_MSG_TYPE_NORMAL -> R.layout.room_chat_item_normal
                    else -> R.layout.room_chat_item_emoji
                }
            }
            onClick(R.id.iv_avatar, R.id.tv_nickname) {
                val model = getModel<RoomChatMessageModel>()
                RoomUserProfileDialog.newInstance(
                    model.userId!!, getRoomInfo()!!.userRole, getRoomInfo()!!.crId
                ).show(childFragmentManager)
            }
        }
        getChatRecyclerView().linear().apply {
            setAdapter(adapter)
        }
        adapter.models = mutableListOf(systemMessage)
        getChatRecyclerView().addScrollOnBottomListener {
            showNewMessageButton(false)
        }
    }

    private fun initMicActionEvents() {
        mMicAction = {
            when (this) {
                MicActionDialog.ClickType.TYPE_QUIT -> {//下麦
                    quitMic(it.onMicroPhoneNumber)
                }

                MicActionDialog.ClickType.TYPE_UP_MIC -> {//上麦
                    upMic(it.onMicroPhoneNumber)
                }

                MicActionDialog.ClickType.TYPE_CLEAN_INCOME -> {//清魅力值
                    CommonConfirmDialog(
                        requireContext(), "是否清除此麦位的魅力值", "魅力值将重置为0"
                    ) {
                        if (this) {
                            clearIncome(it.onMicroPhoneNumber)
                        }
                    }.show()

                }

                MicActionDialog.ClickType.TYPE_INVITE -> {//抱上麦
                    InviteUserUpMicDialog.newInstance(getRoomInfo()!!.crId, it.onMicroPhoneNumber)
                        .show(childFragmentManager)
                }

                MicActionDialog.ClickType.TYPE_INFO_DETAIL -> {//看资料
                    RoomUserProfileDialog.newInstance(
                        it.wheatPositionId, getRoomInfo()!!.userRole, getRoomInfo()!!.crId
                    ).show(childFragmentManager)
                }

                MicActionDialog.ClickType.TYPE_LOCK_MIC -> {//锁麦
                    lockMic(true, false, it.onMicroPhoneNumber)
                }

                MicActionDialog.ClickType.TYPE_LOCK_ALL -> {//一键全锁
                    lockMic(true, true, it.onMicroPhoneNumber)
                }

                MicActionDialog.ClickType.TYPE_UNLOCK_MIC -> {//解锁麦
                    lockMic(false, false, it.onMicroPhoneNumber)
                }

                MicActionDialog.ClickType.TYPE_UNLOCK_ALL -> {//一键全开
                    lockMic(false, true, it.onMicroPhoneNumber)
                }

                MicActionDialog.ClickType.TYPE_KICK -> {//抱下麦
                    kickOut(it)
                }

                else -> {}
            }
        }
    }

    fun showRoomSettingDialog() {
        val canBet =
            getUserInfo()!!.userRightList.indexOf(Constants.UserPermission.PERMISSION_CREATE_PREDICTION) > -1
        RoomSettingDialogV2.newInstance(getRoomInfo(), canBet).setCallBack {
            when (it) {
                RoomSettingClickType.TYPE_GET_INTEGRAL -> {
                    TaskRewardListDialogFragment().show(
                        childFragmentManager, "TaskRewardListDialogFragment"
                    )
                }
                //举报
                RoomSettingClickType.TYPE_REPORT -> {
                    jump(
                        RouterPath.PATH_REPORT,
                        "reportType" to Constants.ReportType.ROOM.type,
                        "roomId" to getRoomInfo().crId,
                        "roomOwnerId" to getRoomInfo().houseOwnerId
                    )
                }

                RoomSettingClickType.TYPE_PREDICTION -> {
                    //积分竞猜
                    RoomPredictionDialog.newInstance(
                        getRoomInfo()!!.crId,
                        getRoomInfo()!!.userRole,
                        getRoomInfo()!!.roomTagCategory
                    ).show(childFragmentManager)
                }

                RoomSettingClickType.TYPE_PASSWORD -> {
                    //设置房间密码
                    val dialog = RoomPasswordDialog.newInstance(getRoomInfo()!!.crId)
                    dialog.show(childFragmentManager)
                }

                RoomSettingClickType.TYPE_SETTING_MANAGER -> {
                    //设置管理员
                    SettingManagerDialog.newInstance(getRoomInfo()!!.crId)
                        .show(childFragmentManager)
                }

                RoomSettingClickType.TYPE_ROOM_INFO -> {
                    //房间信息
                    RoomInfoDialog.newInstance(getRoomInfo()!!.crId).show(childFragmentManager)
                }

                RoomSettingClickType.TYPE_BACKGROUND -> {//设置房间背景
                    jump(
                        RouterPath.PATH_ROOM_SETTING_BACKGROUND,
                        "currentPath" to getRoomInfo().backgroundUrl,
                        "crId" to getRoomInfo().crId
                    )
                }

                RoomSettingClickType.TYPE_MUTE -> { //开启静音
                    RoomServiceManager.setVolume(if (MMKVProvider.room_mute) 0 else 100)
                }

                RoomSettingClickType.TYPE_INCOME -> {
                    //关闭魅力值
                    switchIncome()
                }

                RoomSettingClickType.TYPE_CLEAN_INCOME -> {
                    //清空魅力值
                    clearIncome(-1)
                }

                RoomSettingClickType.TYPE_VIDEO_DIRECTION -> {
                    //横竖屏幕切换
                    RoomVideoSettingDialog(getRoomInfo().crId, getRoomInfo().screenStatus).show(
                        childFragmentManager
                    )
                }

                RoomSettingClickType.TYPE_BACKGROUND_MUSIC -> {
                    val musicDialog = RoomMusicDialog()
                    musicDialog.show(childFragmentManager)
                }

                RoomSettingClickType.TYPE_CLEAR_SCREEN_MESSAGE -> {
                    CommonConfirmDialog(
                        requireContext(),
                        "温馨提示",
                        "清屏后会同步清空公屏内所有消息，\n请谨慎操作？",
                        negativeString = "取消"
                    ) {
                        if (this) {
                            sendClearMessage()
                        }
                    }.show()
                }

                RoomSettingClickType.TYPE_PK -> {
                    RoomPKTypeDialog.newInstance(getRoomInfo().crId).show(childFragmentManager)
                }

                RoomSettingClickType.TYPE_BLACKLIST -> {
                    RoomBlackListDialog(getRoomInfo().crId).show(childFragmentManager)
                }

                else -> {}
            }
        }.show(childFragmentManager)
    }

    private fun changeVideoDirection(state: String) {
        val param = mutableMapOf<String, Any?>()
        param["roomId"] = getRoomInfo()!!.crId
        param["screenStatus"] = state
        request<Int>(RoomApi.API_UPDATE_SCREEN_DIRECTION, Method.GET, param, onSuccess = {
            updateVideoDirection()
        })
    }

    /**
     *  接收聊天室消息
     *  @param type 消息类型
     *  @param message 消息内容
     */
    open fun observerCustomMessage(attachment: BaseAttachment) {
        if (!isAdded) {
            return
        }
        when (attachment.type) {
            Constants.IMMessageType.MSG_TYPE_UP_MIC -> { //上麦消息
                val data = parseCustomMessage<UpMicIMMessage>(attachment.data)
                //先遍历列表剔除之前的麦位信息
                val oldModel = getRoomInfo().wheatPositionList.find { t -> t.wheatPositionId == data.userId }
                if (oldModel != null) {
                    clearMicInfo(oldModel)
                }
                val model = getRoomInfo().wheatPositionList[data.microphoneNumber]
                model.wheatPositionIdName = data.nickname
                model.wheatPositionId = data.userId
                model.userRole = data.userRole
                model.headWearIcon = data.headWearIcon
                model.headWearSvga = data.headWearSvga
                model.wheatPositionIdHeadPortrait = data.profilePath
                model.notifyChange()
                getRoomInfo().wheatPositionList[data.microphoneNumber] = model
                if (data.userId == MMKVProvider.userId) {
                    updateMicStatus()
                }
            }

            Constants.IMMessageType.MSG_UPDATE_CHARM_VALUE -> { //更新魅力值
                val data = parseCustomMessage<CharmValueMessage>(attachment.data)
                data.microphoneMessageList.forEachIndexed { index, item ->
                    getRoomInfo().wheatPositionList[index].wheatPositionIdCharmValue = item.charmValue
                    getRoomInfo().wheatPositionList[index].notifyChange()
                }
            }

            Constants.IMMessageType.MSG_TYPE_QUIT_MIC -> { //下麦消息
                val data = parseCustomMessage<UpMicIMMessage>(attachment.data)
                val model = getRoomInfo().wheatPositionList.find { it.wheatPositionId == data.userId }
                if (model != null) {
                    clearMicInfo(model)
                }
                if (data.userId == MMKVProvider.userId) {
                    RoomServiceManager.stopPublishStream()
                    updateMicStatus()
                }
            }

            Constants.IMMessageType.MSG_TYPE_ROOM_CHAT_CONTENT -> { //文本消息
                val data = parseCustomMessage<RoomChatMessageModel>(attachment.data)
                data.messageKindList = mutableListOf("001", "002")
                data.contentSpan = buildSpannedString { append(data.content) }
                insertChatModel(data)

            }

            Constants.IMMessageType.MSG_EMOJI -> { //表情消息
                val data = parseCustomMessage<RoomChatMessageModel>(attachment.data)
                data.type = CHAT_MSG_TYPE_USER_EMOJI
                data.messageKindList = mutableListOf("001", "002")
                if (EmojiGameUtil.isEmojiGame(data.name)) {
                    data.emojiUrl = EmojiGameUtil.getEmojiPath(data.name, data.emojiGameIndex)
                    data.isEmojiLoop = false
                    data.isEmojiImage =
                        EmojiGameUtil.getEmojiImagePath(data.name, data.emojiGameIndex)
                }
                insertChatModel(data)
                playMicEmojiAnimation(data.userId, data.emojiUrl)
            }

            Constants.IMMessageType.MSG_TYPE_GIFT -> {//送礼消息
                val data = parseCustomMessage<GiftMessage>(attachment.data)
                doGiftAnimation(data)
                if (data.giftSource == "003") {
                    insertChatModel(parseBoxGiftMessage(data))
                } else {
                    insertChatModel(parseGiftMessage(data))
                }
                playGiftFlyAnimation(data)
            }

            Constants.IMMessageType.MSG_TYPE_LOCK_MIC, Constants.IMMessageType.MSG_TYPE_UNLOCK_MIC -> {//锁麦消息
                val data = parseCustomMessage<LockMicMessage>(attachment.data)
                getRoomInfo().wheatPositionList.forEach {
                    it.lockWheat = data.lockedMicrophone.contains(it.onMicroPhoneNumber)
                    it.notifyChange()
                }
            }

            Constants.IMMessageType.MSG_TYPE_USER_IN -> {//用户进场消息
                val data = parseCustomMessage<UserEnterMessage>(attachment.data)
                if (!data.carPath.isNullOrEmpty()) {
                    playCarAnimation(data)
                }
                insertChatModel(parseUserJoinMessage(data))
            }

            Constants.IMMessageType.MSG_TYPE_INVITE_MIC -> {//抱上麦消息
                val data = parseCustomMessage<UpMicIMMessage>(attachment.data)
                val model = getRoomInfo().wheatPositionList[data.microphoneNumber]
                model.wheatPositionIdName = data.nickname
                model.wheatPositionId = data.userId
                model.userRole = data.userRole
                model.headWearIcon = data.headWearIcon
                model.headWearSvga = data.headWearSvga
                model.wheatPositionIdHeadPortrait = data.profilePath
                model.notifyChange()
                getRoomInfo().wheatPositionList[data.microphoneNumber] = model
                if (data.userId == MMKVProvider.userId) {
                    RoomServiceManager.isInMicQueue = false
                    updateMicStatus()
                    CommonConfirmDialog(
                        requireContext(), "您已被抱上麦"
                    ) {
                        if (!this) {
                            quitMic(data.microphoneNumber)
                        }
                    }.show()
                }
            }

            Constants.IMMessageType.MSG_TYPE_KICK_MIC -> {//抱下麦消息
                val data = parseCustomMessage<UpMicIMMessage>(attachment.data)
                val model = getRoomInfo().wheatPositionList.find { it.wheatPositionId == data.userId }
                if (model != null) {
                    clearMicInfo(model)
                }
                if (data.userId == MMKVProvider.userId) {
                    customToast("您已被抱下麦", true)
                    RoomServiceManager.stopPublishStream()
                    updateMicStatus()
                }
            }

            Constants.IMMessageType.MSG_SETTING_MANAGER -> {//设置和取消管理消息
                val data = parseCustomMessage<SettingManagerMessage>(attachment.data)
                if (data.userId == MMKVProvider.userId) {
                    getRoomInfo()?.userRole =
                        if (data.isManager) Constants.ROOM_USER_TYPE_MANAGER else Constants.ROOM_USER_TYPE_NORMAL
                }
                getRoomInfo().wheatPositionList.forEach {
                    if (it.wheatPositionId == data.userId) {
                        it.userRole =
                            if (data.isManager) Constants.ROOM_USER_TYPE_MANAGER else Constants.ROOM_USER_TYPE_NORMAL
                    }
                }
                updateMicStatus()
            }

            Constants.IMMessageType.MSG_BAN_MIC -> {//禁麦消息
                val data = parseCustomMessage<BanMicMessage>(attachment.data)
                if (data.userId == MMKVProvider.userId && isUserOnMic(MMKVProvider.userId)) {
                    if (data.isMike) {
                        customToast("您被禁麦10分钟")
                        RoomServiceManager.stopPublishStream()
                        updateMicStatus()
                    } else {
                        customToast("您已被解除禁麦")
                    }

                }
            }

//            Constants.IMMessageType.MSG_PREDICTION_BET -> {//用户积分竞猜投注
//                val data = parseCustomMessage<PredictionBetMessage>(attachment.data)
//                getPredictionView().updateProgress(
//                    data.integralGuessId, data.leftBetAmount, data.rightBetAmount
//                )
//            }

            Constants.IMMessageType.MSG_CHAT_WATER -> {//浇水消息
                val data = parseCustomMessage<WaterMessage>(attachment.data)
                if (MMKVProvider.room_water_info_show) {
                    insertChatModel(parseWaterMessage(data))
                }
            }

            Constants.IMMessageType.MSG_PREDICTION_CREATE -> {//创建积分竞猜消息
                val data = parseCustomMessage<CreateBetMessage>(attachment.data)
                data.leftProgress = 0
                data.rightProgress = 0
                showPredictionView(data)
            }

            Constants.IMMessageType.MSG_PREDICTION_RESULT -> {//下注结果消息
                val data = parseCustomMessage<PredictionResultMessage>(attachment.data)
                requestRecentPrediction()
            }

            Constants.IMMessageType.MSG_DELETE_BET -> {//删除竞猜消息
                val data = parseCustomMessage<PredictionDeleteMessage>(attachment.data)
                requestRecentPrediction()
            }

            Constants.IMMessageType.MSG_USER_MIC_QUEUE -> {
                //用户排麦消息
                val data = parseCustomMessage<MicQueueMessage>(attachment.data)
                if (data.userId == MMKVProvider.userId) {
                    RoomServiceManager.isInMicQueue = true
                    updateMicStatus()
                }
                updateMicQueueAmount(data.waitingMicrophoneNumber)
            }

            Constants.IMMessageType.MSG_USER_CANCEL_MIC_QUEUE, Constants.IMMessageType.MSG_MANAGER_CANCEL_MIC_QUEUE -> {
                //取消排麦消息
                val data = parseCustomMessage<MicQueueMessage>(attachment.data)
                if (data.userId == MMKVProvider.userId) {
                    RoomServiceManager.isInMicQueue = false
                    updateMicStatus()
                    if (attachment.type == Constants.IMMessageType.MSG_MANAGER_CANCEL_MIC_QUEUE) {
                        customToast("您已被请出了队伍")
                    }
                }
                updateMicQueueAmount(data.waitingMicrophoneNumber)
            }

            Constants.IMMessageType.MSG_FINISH_BET -> {//结束竞猜
                val data = parseCustomMessage<FinishBetMessage>(attachment.data)
                requestRecentPrediction()
            }

            Constants.IMMessageType.MSG_KICK_OUT_USER -> {  //踢出房间
                val data = parseCustomMessage<KickOutRoomMessage>(attachment.data)
                if (data.userId == MMKVProvider.userId) {
                    customToast(R.string.room_toast_kick_out_user)
                    (activity as BaseLiveActivity).exitRoom()
                }
            }

            Constants.IMMessageType.MSG_UPDATE_ROOM_INFO -> {//更新房间信息
                val data = parseCustomMessage<UpdateRoomInfoMessage>(attachment.data)
                getRoomInfo().apply {
                    roomAffiche = data.roomAffiche
                    roomIcon = data.roomIcon
                    roomTitle = data.roomTitle
                }
                getActionBarView().initData(getRoomInfo())
            }

            Constants.IMMessageType.MSG_SYSTEM -> {//系统消息
                val data = parseCustomMessage<RoomSystemMessage>(attachment.data)
                if (data.os.contains("android") && (data.sex == MMKVProvider.sex || data.sex == "003") && (data.roomType == getRoomInfo()!!.roomTagCategory || data.roomType == "003")) {
                    val chatModel = RoomChatMessageModel(
                        content = data.content, type = CHAT_MSG_TYPE_NOTICE,
                        messageKindList = data.messageKindList
                    )
                    insertChatModel(chatModel)
                }
            }

            Constants.IMMessageType.MSG_UPDATE_VIDEO_DIRECTION -> {//设置横竖屏
                val data = parseCustomMessage<RoomVideoDirectionMessage>(attachment.data)
                getRoomInfo()?.screenStatus = data.screenStatus
                updateVideoDirection()
            }

            Constants.IMMessageType.MSG_SWITCH_INCOME_OPEN -> { //开启或关闭魅力值
                val data = parseCustomMessage<SwitchIncomeOpenMessage>(attachment.data)
                getRoomInfo().charmOnOff = data.charmOnOff
                customToast(if (data.charmOnOff == "001") "魅力值已开启" else "魅力值已关闭")
                getRoomInfo().wheatPositionList.forEach {
                    it.isShowIncome = data.charmOnOff
                    it.notifyChange()
                }
            }

            Constants.IMMessageType.MSG_CHAT_CLEAR_MESSAGE -> {//清除屏幕消息
                clearScreenMessage()
            }

            Constants.IMMessageType.MSG_GIFT_NOTIFY -> {
                val data = parseCustomMessage<GiftMessage>(attachment.data)
                if (data.sourceUserChatRoomId != getRoomInfo().crId) {
                    insertChatModel(parseGiftMessage(data))
                }
            }

            Constants.IMMessageType.MSG_GIFT_ANIMATION_NOTIFY -> {
                val data = parseCustomMessage<GiftAnimationMessage>(attachment.data)
                if (data.sourceUserChatRoomId != getRoomInfo().crId) {
                    playGiftAnimation(data.svg)
                }
            }

            Constants.IMMessageType.MSG_WATER_TREE_CHANGE -> {
                val data = parseCustomMessage<TreeChangeMessage>(attachment.data)
                changeTreeState(data.treeType)
            }

            Constants.IMMessageType.MSG_TYPE_ROOM_NEW_BROADCAST_MESSAGE -> {
                val data = parseCustomMessage<RoomScreenMessageModel>(attachment.data)
                getBroadcastView().initData(data)
            }
            Constants.IMMessageType.MSG_TYPE_ROOM_NEW_BROADCAST_END -> {
                getBroadcastView().initData(null)
            }

        }
    }

    private fun clearScreenMessage() {
        getChatRecyclerView().bindingAdapter.mutable.clear()
        getChatRecyclerView().addModels(
            mutableListOf(
                RoomChatMessageModel(
                    content = R.string.room_message_notice.resToString(),
                    type = CHAT_MSG_TYPE_NOTICE
                )
            )
        )
        RoomServiceManager.allMessageList.clear()
        RoomServiceManager.historyMessage.clear()
    }


    /**
     * 发送清屏自定义消息
     */
    private fun sendClearMessage() {
        val model = RoomChatMessageModel(type = 204)
        val attr = BaseAttachment(Constants.IMMessageType.MSG_CHAT_CLEAR_MESSAGE, model)
        val message = ChatRoomMessageBuilder.createChatRoomCustomMessage(
            getRoomInfo().neteaseChatId, attr
        )
        NIMClient.getService(ChatRoomService::class.java).sendMessage(message, false)
            .setCallback(object : RequestCallback<Void> {
                override fun onSuccess(param: Void?) {
                    clearScreenMessage()
                }

                override fun onFailed(code: Int) {
                    customToast(code.toString())
                }

                override fun onException(exception: Throwable?) {
                }

            })
    }


    /**
     * 发送文本消息
     * @param roomChatId 聊天室Id
     * @param content 发送内容
     * @param userInfoBean 发送人userinfo
     */
    fun sendTextMessage(
        roomChatId: String,
        content: String,
        userInfoBean: UserInfoBean,
    ) {
        val model = RoomChatMessageModel(
            userId = userInfoBean.userId,
            profilePath = userInfoBean.profilePath,
            nickname = userInfoBean.nickname,
            content = content,
            medalList = userInfoBean.medalList,
            contentSpan = buildSpannedString { append(content) },
            wealthLevel = userInfoBean.consumeLevel,
            charmLevel = userInfoBean.charmLevel,
            type = CHAT_MSG_TYPE_NORMAL,
            privilege = userInfoBean.privilege,
            headwearUrl = userInfoBean.headwearUrl
        )
        val attr = BaseAttachment(Constants.IMMessageType.MSG_TYPE_ROOM_CHAT_CONTENT, model)
        val message = ChatRoomMessageBuilder.createChatRoomCustomMessage(roomChatId, attr)
        message.content = content
        val antiSpamOption = NIMAntiSpamOption()
        val json = JSONObject()
        json.put("type", 1)
        json.put("data", content)
        antiSpamOption.content = json.toString()
        message.nimAntiSpamOption = antiSpamOption
        NIMClient.getService(ChatRoomService::class.java).sendMessage(message, false)
    }


    /**
     * 发送表情包消息
     * @param roomChatId 聊天室Id
     * @param url 表情包图片
     * @param userInfoBean 发送人userinfo
     */
    fun sendEmojiMessage(
        roomChatId: String,
        name: String,
        index: String,
        isEmojiLoop: Boolean,
        emojiGameImage: Int,
        url: String,
        userInfoBean: UserInfoBean,
    ) {
        val model = RoomChatMessageModel(
            userId = userInfoBean.userId,
            profilePath = userInfoBean.profilePath,
            nickname = userInfoBean.nickname,
            emojiUrl = url,
            wealthLevel = userInfoBean.consumeLevel,
            charmLevel = userInfoBean.charmLevel,
            type = CHAT_MSG_TYPE_USER_EMOJI,
            medalList = userInfoBean.medalList,
            privilege = userInfoBean.privilege,
            headwearUrl = userInfoBean.headwearUrl,
            name = name,
            emojiGameIndex = index,
            isEmojiLoop = isEmojiLoop,
            isEmojiImage = emojiGameImage,
            messageKindList = mutableListOf("001", "002"),
            chatTabIndex = getChatTabIndex()
        )

        val attr = BaseAttachment(Constants.IMMessageType.MSG_EMOJI, model)
        val message = ChatRoomMessageBuilder.createChatRoomCustomMessage(roomChatId, attr)
        NIMClient.getService(ChatRoomService::class.java).sendMessage(message, false)
            .setCallback(object : RequestCallback<Void> {
                override fun onSuccess(param: Void?) {
                    //发送成功插入聊天内容
                    sendChatTask()
                    insertChatModel(model)
                    playMicEmojiAnimation(model.userId, model.emojiUrl)
                    RoomServiceManager.addHistoryMessage(message)
                }

                override fun onFailed(code: Int) {
                }

                override fun onException(exception: Throwable?) {
                }

            })
    }

    private fun getChatTabIndex(): String {
        logE("当前index------${getChatTabLayout()?.currentItemIndex}")
        return when (getChatTabLayout()?.currentItemIndex.orZero()) {
            0 -> "001"
            1 -> "002"
            else -> "003"
        }
    }

    //向聊天列表插入聊天消息
    fun insertChatModel(model: RoomChatMessageModel, isHistory: Boolean = false) {
        if (!isHistory) {
            RoomServiceManager.allMessageList.add(model)
        }
        if (model.messageKindList.contains(getChatTabIndex())) {
            getChatRecyclerView().addModels(mutableListOf(model))
            //如果列表在底部或者是自己发送的消息，插入后列表自动滚动到底部，否则显示新消息按钮
            if (!getChatRecyclerView().canScrollVertically(1) || model.userId == MMKVProvider.userId) {
                getChatRecyclerView().scrollToBottom()
            } else {
                showNewMessageButton(true)
            }
        }
    }

    //向聊天列表插入聊天消息
    private fun insertChatModel(
        model: MutableList<RoomChatMessageModel>,
        isHistory: Boolean = false
    ) {
        model.forEach {
            if (!isHistory) {
                RoomServiceManager.allMessageList.add(it)
            }
            if (it.messageKindList.contains(getChatTabIndex())) {
                getChatRecyclerView().addModels(mutableListOf(it))
            }
        }
        //如果列表在底部或者是自己发送的消息，插入后列表自动滚动到底部，否则显示新消息按钮
        if (!getChatRecyclerView().canScrollVertically(1) || model[0].userId == MMKVProvider.userId) {
            getChatRecyclerView().scrollToBottom()
        } else {
            showNewMessageButton(true)
        }
    }


    //向聊天列表插入送礼消息
    private fun parseGiftMessage(model: GiftMessage): MutableList<RoomChatMessageModel> {
        val messageList = mutableListOf<RoomChatMessageModel>()
        val sourceUser = model.sourceUser
        model.targetUsers.forEach {
            val ssb = buildSpannedString {
                append("送给 ${it.nickName} ")
                color(com.kissspace.module_common.R.color.color_FEC238.resToColor()) {
                    it.giftInfos.forEach { gift ->
                        append("${gift.giftName}x${gift.total}")
                    }
                }
            }
            val message = RoomChatMessageModel(
                profilePath = sourceUser.profilePath,
                userId = sourceUser.userId,
                nickname = sourceUser.nickName,
                wealthLevel = sourceUser.consumeLevel,
                charmLevel = sourceUser.charmLevel,
                type = CHAT_MSG_TYPE_NORMAL,
                contentSpan = ssb,
                headwearUrl = sourceUser.headwearUrl,
                medalList = sourceUser.medalList,
                privilege = sourceUser.privilege,
                messageKindList = model.messageKindList
            )
            messageList.add(message)
        }
        return messageList
    }

    //解析盲盒礼物消息
    private fun parseBoxGiftMessage(
        model: GiftMessage,
    ): MutableList<RoomChatMessageModel> {
        val messageList = mutableListOf<RoomChatMessageModel>()
        val sourceUser = model.sourceUser
        model.targetUsers.forEach {
            val ssb = buildSpannedString {
                append("送给 ${it.nickName} ${it.mysteryBoxNumber}个${it.mysteryBoxName}，获得了总价值")
                color(com.kissspace.module_common.R.color.color_FEC238.resToColor()) {
                    append("${it.mysteryBoxGiftTotalValue}金币")
                }
                append("的礼物：")
                val gifts =
                    it.giftInfos.joinToString(separator = ",") { it.giftName + "x" + it.total }
                color(com.kissspace.module_common.R.color.color_D9853B.resToColor()) {
                    append(gifts)
                }
            }

            val message = RoomChatMessageModel(
                profilePath = sourceUser.profilePath,
                userId = sourceUser.userId,
                nickname = sourceUser.nickName,
                wealthLevel = sourceUser.consumeLevel,
                charmLevel = sourceUser.charmLevel,
                type = CHAT_MSG_TYPE_NORMAL,
                contentSpan = ssb,
                headwearUrl = sourceUser.headwearUrl,
                medalList = sourceUser.medalList,
                privilege = sourceUser.privilege,
                messageKindList = model.messageKindList
            )
            messageList.add(message)
        }
        return messageList
    }

    private fun parseWaterMessage(message: WaterMessage): RoomChatMessageModel {
        val ssb = buildSpannedString {
            append("在${message.gameName}中获得价值")
            color(com.kissspace.module_common.R.color.color_FEC238.resToColor()) {
                append("${message.waterGameGiftInfoDto.price}金币")
            }
            append("的礼物")
            color(com.kissspace.module_common.R.color.color_D9853B.resToColor()) {
                append(message.waterGameGiftInfoDto.giftName + "x" + message.waterGameGiftInfoDto.num)
            }
        }
        return RoomChatMessageModel(
            profilePath = message.profilePath,
            userId = message.userId,
            nickname = message.nickname,
            headwearUrl = message.headwearUrl,
            wealthLevel = message.consumeLevel,
            charmLevel = message.charmLevel,
            type = CHAT_MSG_TYPE_NORMAL,
            privilege = message.privilege,
            medalList = message.medalList,
            contentSpan = ssb,
            messageKindList = message.messageKindList
        )
    }

    private suspend fun buildGiftDrawable(infos: List<String>): List<Drawable> {
        return try {
            val list = mutableListOf<Drawable>()
            withContext(Dispatchers.IO) {
                infos.forEach {
                    if (it.isNotEmpty()) {
                        val drawable = Drawable.createFromStream(URL(it).openStream(), "image.jpg")
                        drawable?.let { d -> list.add(d) }
                    }
                }
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }


    //解析自定义消息
    override fun onEvent(t: List<ChatRoomMessage>?) {
        t?.forEach {
            try {
                val json = JSONObject(it.attachStr)
                logE("收到自定义消息${json}")
                val type = json.getString("type")
                if (type.equals(MSG_REFRESH_TREE)) {
                    FlowBus.post(Event.RefreshTree)
                } else {
                    val data = BaseAttachment(type, json.get("data"))
                    observerCustomMessage(data)
                }
            } catch (e: Exception) {
                logE("消息格式错误！${e.message}")
            }
        }
    }


    /**
     * 用户上麦
     * @param index 麦位序号
     */
    abstract fun upMic(index: Int)

    /**
     * 用户下麦
     * @param index 麦位序号
     * @param isKickOut 是否是被抱下麦的
     */
    abstract fun quitMic(index: Int)

    abstract fun getUserInfo(): UserInfoBean?

    //获取麦位控件
    abstract fun getMicrophoneLayout(): RoomMicrophoneLayout

    //获取列表列表
    abstract fun getChatRecyclerView(): RecyclerView

    //获取积分竞猜view
    abstract fun getPredictionView(): RoomPredictionView

    abstract fun getActivityBannerView(): BannerViewPager<Any>

    abstract fun getActionBarView(): RoomActionBarView

    abstract fun getBroadcastView(): RoomBroadcastView

    //显示新消息按钮
    abstract fun showNewMessageButton(isShow: Boolean)

    abstract fun updateMicQueueAmount(amount: Int)

    /**
     * 锁麦
     * @param isLock 锁麦还是解锁
     * @param isAll 是否操作全部
     * @param index 麦位下标
     */
    abstract fun lockMic(isLock: Boolean, isAll: Boolean, index: Int)

    /**
     * 更新麦克风状态
     */
    abstract fun updateMicStatus()

    /**
     * 抱下麦
     */
    abstract fun kickOut(micModel: MicUserModel)

    /**
     * 播放坐骑动画
     * @param url 动画资源地址
     */
    abstract fun playCarAnimation(message: UserEnterMessage)

    abstract fun playGiftAnimation(url: String)

    abstract fun changeTreeState(state: String)

    abstract fun updateUnReadCount()

    abstract fun playGiftFlyAnimation(message: GiftMessage)

    abstract fun updateVideoDirection()

    abstract fun getChatTabLayout(): DslTabLayout?

    /**
     * 是否是旧房间
     */
    abstract fun isOldRoom(): Boolean

    /**
     * 用户是否在麦上
     */
    fun isUserOnMic(userId: String?) = getRoomInfo().wheatPositionList.find { it.wheatPositionId == userId } != null

    /**
     * 麦上是否有用户
     */
    private fun isMicEmpty(): Boolean = getRoomInfo().wheatPositionList.isEmpty()

    /**
     * 是否是普通用户
     */
    fun isNormalUser() = getRoomInfo().userRole == Constants.ROOM_USER_TYPE_NORMAL

    /**
     * 是否房主
     */
    fun isAnchor() = getRoomInfo().userRole == Constants.ROOM_USER_TYPE_ANCHOR


    private fun showPredictionView(message: CreateBetMessage) {
//        getPredictionView().visibility = View.VISIBLE
//        getPredictionView().initData(message, lifecycleScope)
    }

    private fun switchIncome() {
        val param = mutableMapOf<String, Any?>()
        param["chatRoomId"] = getRoomInfo().crId
        param["charmOnOff"] = if (getRoomInfo().charmOnOff == "001") "002" else "001"
        request<Int>(RoomApi.API_SWITCH_INCOME, Method.GET, param)
    }

    private fun clearIncome(index: Int) {
        val param = mutableMapOf<String, Any?>()
        param["chatRoomId"] = getRoomInfo().crId
        param["microphoneNumber"] = index
        request<Int>(RoomApi.API_CLEAR_CHARM_VALUE, Method.POST, param, onSuccess = {
            customToast("操作成功")
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        //注销监听
        NIMClient.getService(ChatRoomServiceObserver::class.java).observeReceiveMessage(this, false)
        NIMClient.getService(MsgServiceObserve::class.java)
            .observeRecentContact(recentContactObserver, false)
        NIMClient.getService(MsgServiceObserve::class.java)
            .observeBroadcastMessage(broadcastObserver, false)
        NIMClient.getService(ChatRoomServiceObserver::class.java)
            .observeMsgStatus(messageStatusObserver, false)
        NIMClient.getService(ChatRoomServiceObserver::class.java)
            .observeOnlineStatus(onLineStateObserver, false)
    }

    fun getRoomInfo() = (requireParentFragment() as LiveAudioFragment).getRoomInfo()

    override fun createDataObserver() {
        super.createDataObserver()
        //刷新未读消息数量
        FlowBus.observerEvent<Event.RefreshUnReadMsgCount>(this) {
            updateUnReadCount()
        }
        FlowBus.observerEvent<Event.CapturedSoundLevelUpdateEvent>(this) {
            if (it.soundLevel > 0) {
                val model =
                    getRoomInfo().wheatPositionList.find { t -> t.wheatPositionId == MMKVProvider.userId }
                if (model != null) {
                    getMicrophoneLayout().playTalkAnimation(model)
                }
            }
        }

        FlowBus.observerEvent<Event.RemoteSoundLevelUpdateEvent>(this) {
            if (it.soundLevels.isNullOrEmpty()) {
                return@observerEvent
            }
            for ((streamId, level) in it.soundLevels!!) {
                if (level > 0) {
                    val model: MicUserModel? = if (streamId == getRoomInfo().crId) {
                        getRoomInfo().wheatPositionList.find { t -> t.wheatPositionId == getRoomInfo().houseOwnerId }
                    } else {
                        getRoomInfo().wheatPositionList.find { t -> t.wheatPositionId == streamId }
                    }
                    if (model != null) {
                        getMicrophoneLayout().playTalkAnimation(model)
                    }
                }
            }
        }

        FlowBus.observerEvent<Event.ShowLuckyGiftEvent>(this) {
            var from = 0
            var targetUserId = ""
            if (getRoomInfo().roomTagCategory == Constants.ROOM_TYPE_PARTY) {
                from = Constants.GiftDialogFrom.FROM_PARTY
                targetUserId = ""
            } else {
                targetUserId = getRoomInfo().houseOwnerId
                from = Constants.GiftDialogFrom.FROM_CHAOBO
            }
            val service = DRouter.build(IRoomProvider::class.java).getService()
            service.showGiftDialog(
                childFragmentManager,
                crId = getRoomInfo().crId,
                targetUserId = targetUserId,
                isOpenPointsBox = false,
                from = from,
                isChooseLucky = true
            )
        }
        FlowBus.observerEvent<Event.MsgSystemEvent>(this) {
            updateUnReadCount()
        }
    }

    private fun isAudioRoom() = getRoomInfo().roomTagCategory == Constants.ROOM_TYPE_PARTY


    /**
     * 麦位播放礼物动画
     */
    private fun playMicEmojiAnimation(userId: String?, url: String?) {
        val micModel = getRoomInfo().wheatPositionList.find { t -> t.wheatPositionId == userId }
        if (micModel != null && !url.isNullOrEmpty()) {
            getMicrophoneLayout().playMicEmojiAnimation(micModel, url)
        }
    }

    fun parseUserJoinMessage(message: UserEnterMessage): RoomChatMessageModel {
        val chatMessage = RoomChatMessageModel()
        chatMessage.userId = message.userId
        chatMessage.profilePath = message.profilePath
        chatMessage.charmLevel = message.charmLevel
        chatMessage.wealthLevel = message.consumeLevel
        chatMessage.nickname = message.nickname
        chatMessage.type = CHAT_MSG_TYPE_USER_IN
        chatMessage.messageKindList = mutableListOf("001", "002")
        return chatMessage
    }

    /**
     * 是否是同一个礼物送给多人
     */
    private fun isSameGift2User(message: GiftMessage): Boolean {
        if (message.targetUsers.size == 1) {
            return false
        }
        val giftInfo = mutableListOf<GiftInfo>()
        message.targetUsers.forEach {
            it.giftInfos.forEach { gift ->
                giftInfo.add(gift)
            }
        }
        return giftInfo.groupBy { it.giftId }.size == 1
    }


    /**
     * 播放送礼动画
     */
    private fun doGiftAnimation(message: GiftMessage) {
        if (!MMKVProvider.room_gift_effect_show) {
            return
        }
        if (isSameGift2User(message)) {
            val url = message.targetUsers[0].giftInfos[0].svg
            playGiftAnimation(url)
        } else {
            message.targetUsers.forEach { t ->
                t.giftInfos.forEach { gift ->
                    playGiftAnimation(gift.svg)
                }
            }
        }
    }

    fun clearMicInfo(model: MicUserModel) {
        model.wheatPositionId = ""
        model.wheatPositionIdName = ""
        model.wheatPositionIdHeadPortrait = ""
        model.headWearSvga = null
        model.headWearIcon = null
        model.userRole = ""
        model.notifyChange()
        getRoomInfo().wheatPositionList[model.onMicroPhoneNumber] = model
        getMicrophoneLayout().clearMicAnimation(model)
    }

    private fun sendChatTask() {
        if (Constants.isFinishChatTask) {
            return
        }
        val params = mutableMapOf<String, Any?>("sendType" to 2)
        request<Boolean>(CommonApi.API_TASK_SEND_MESSAGE, Method.POST, params, onSuccess = {
            Constants.isFinishChatTask = it
        })
    }


}