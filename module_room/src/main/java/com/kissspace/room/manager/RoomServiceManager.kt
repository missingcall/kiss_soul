package com.kissspace.room.manager

import android.content.Intent
import android.os.Build
import com.blankj.utilcode.util.LogUtils
import com.kissspace.util.topActivity
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.Observer
import com.netease.nimlib.sdk.RequestCallback
import com.netease.nimlib.sdk.chatroom.ChatRoomMessageBuilder
import com.netease.nimlib.sdk.chatroom.ChatRoomService
import com.netease.nimlib.sdk.chatroom.ChatRoomServiceObserver
import com.netease.nimlib.sdk.chatroom.model.ChatRoomMessage
import com.netease.nimlib.sdk.chatroom.model.EnterChatRoomData
import com.netease.nimlib.sdk.chatroom.model.EnterChatRoomResultData
import com.kissspace.common.config.Constants
import com.kissspace.common.config.ConstantsKey
import com.kissspace.common.config.isReleaseServer
import com.kissspace.common.flowbus.Event
import com.kissspace.common.flowbus.FlowBus
import com.kissspace.common.model.RoomInfoBean
import com.kissspace.common.model.immessage.BanMicMessage
import com.kissspace.common.model.immessage.BaseAttachment
import com.kissspace.common.model.immessage.KickOutRoomMessage
import com.kissspace.common.model.immessage.UpMicIMMessage
import com.kissspace.common.model.immessage.UpdateRoomInfoMessage
import com.kissspace.common.model.immessage.UserEnterMessage
import com.kissspace.common.util.mmkv.MMKVProvider
import com.kissspace.common.util.parseCustomMessage
import com.kissspace.common.util.customToast
import com.kissspace.module_room.R
import com.kissspace.network.net.Method
import com.kissspace.common.http.getUserInfo
import com.kissspace.common.model.MicUserModel
import com.kissspace.common.model.RoomListBean
import com.kissspace.common.model.immessage.CharmValueMessage
import com.kissspace.common.model.immessage.RoomChatMessageModel
import com.kissspace.common.model.immessage.SwitchIncomeOpenMessage
import com.kissspace.network.net.request
import com.kissspace.room.http.RoomApi
import com.kissspace.room.service.RoomNotificationService
import com.kissspace.room.ui.activity.LiveAudioActivity
import com.kissspace.util.application
import com.kissspace.util.logE
import im.zego.zegoexpress.ZegoExpressEngine
import im.zego.zegoexpress.callback.IZegoEventHandler
import im.zego.zegoexpress.constants.ZegoANSMode
import im.zego.zegoexpress.constants.ZegoAudioConfigPreset
import im.zego.zegoexpress.constants.ZegoPublishChannel
import im.zego.zegoexpress.constants.ZegoPublisherState
import im.zego.zegoexpress.constants.ZegoScenario
import im.zego.zegoexpress.constants.ZegoUpdateType
import im.zego.zegoexpress.constants.ZegoVideoConfigPreset
import im.zego.zegoexpress.constants.ZegoVideoSourceType
import im.zego.zegoexpress.entity.ZegoAudioConfig
import im.zego.zegoexpress.entity.ZegoEngineConfig
import im.zego.zegoexpress.entity.ZegoEngineProfile
import im.zego.zegoexpress.entity.ZegoRoomConfig
import im.zego.zegoexpress.entity.ZegoStream
import im.zego.zegoexpress.entity.ZegoUser
import im.zego.zegoexpress.entity.ZegoVideoConfig
import org.json.JSONObject
import java.util.ArrayList
import java.util.HashMap


/**
 *
 * @Author: nicko
 * @CreateDate: 2022/12/14 14:11
 * @Description: 房间管理类
 *
 */
object RoomServiceManager {
    /**
     * 当前房间信息
     */
    var roomInfo: RoomInfoBean? = null

    /**
     * 当前视频streamId
     */
    var videoStreamId: String? = null


    /**
     * 我是否在排麦列表中
     */
    var isInMicQueue = false

    /**
     * 我是否在推流
     */
    var isPublishStream = false


    /**
     * 即构引擎
     */
    private var mZegoEngine: ZegoExpressEngine? = null

    /**
     * 第一次进房时间
     */
    var firstJoinRoomTime = System.currentTimeMillis()

    val historyMessage = mutableListOf<ChatRoomMessage>()

    val allMessageList = mutableListOf<RoomChatMessageModel>()

    val roomList = mutableListOf<RoomListBean>()

    /**
     * 即构事件回调
     */
    private val zegoEventHandler = object : IZegoEventHandler() {
        override fun onPublisherStateUpdate(
            streamID: String?, state: ZegoPublisherState?, errorCode: Int, extendedData: JSONObject?
        ) {
            super.onPublisherStateUpdate(streamID, state, errorCode, extendedData)

        }

        override fun onRoomStreamUpdate(
            roomID: String?,
            updateType: ZegoUpdateType?,
            streamList: ArrayList<ZegoStream>?,
            extendedData: JSONObject?
        ) {
            super.onRoomStreamUpdate(roomID, updateType, streamList, extendedData)
            if (updateType == ZegoUpdateType.DELETE && streamList?.get(0)?.streamID == RoomServiceManager.roomInfo?.crId) {
                videoStreamId = null
            }
            if (updateType == ZegoUpdateType.ADD && streamList?.get(0)?.streamID == RoomServiceManager.roomInfo?.crId) {
                videoStreamId = streamList?.get(0)?.streamID
            }
            if (roomInfo?.roomTagCategory == Constants.ROOM_TYPE_PARTY) {
                if (updateType == ZegoUpdateType.ADD) {
                    streamList?.forEach {
                        if (it.streamID != MMKVProvider.userId) {
                            startPlayStream(it.streamID)
                        }
                    }
                } else {
                    streamList?.forEach {
                        stopPlayingStream(it.streamID)
                    }
                }
            } else {
                streamList?.forEach { stream ->
                    if (updateType == ZegoUpdateType.ADD) {
                        if (stream.streamID != roomInfo?.crId) {
                            if (stream.streamID != MMKVProvider.userId) {
                                startPlayStream(stream.streamID)
                            }
                        }
                    } else {
                        stopPlayingStream(stream.streamID)
                    }
                }
            }
        }

        override fun onCapturedSoundLevelUpdate(soundLevel: Float) {
            super.onCapturedSoundLevelUpdate(soundLevel)
            FlowBus.post(Event.CapturedSoundLevelUpdateEvent(soundLevel))
        }

        override fun onRemoteSoundLevelUpdate(soundLevels: HashMap<String, Float>?) {
            super.onRemoteSoundLevelUpdate(soundLevels)
            FlowBus.post(Event.RemoteSoundLevelUpdateEvent(soundLevels))
        }
    }


    /**
     * 房间消息观察者
     */
    private val observer = Observer<List<ChatRoomMessage>> {
        it?.forEach { msg ->
            try {
                val json = JSONObject(msg.attachStr)
                val type = json.getString("type")
                val data = BaseAttachment(type, json.get("data"))
                observerCustomMessage(data, msg)
            } catch (e: java.lang.Exception) {
                LogUtils.e("消息格式错误！$it")
            }
        }
    }

    /**
     *
     * 初始化ZegoEngine
     * @param crId 房间业务id
     * @param zegoToken 即构room token
     */
    fun init(roomInfoBean: RoomInfoBean) {
        this.roomInfo = roomInfoBean
        if (mZegoEngine == null) {
            firstJoinRoomTime = System.currentTimeMillis()
            initZegoEngine()
            loginZegoRoom(roomInfoBean.crId, roomInfoBean.zegoTokcen)
            initMusic()
            loginNimRoom(roomInfoBean.neteaseChatId, true)
            NIMClient.getService(ChatRoomServiceObserver::class.java)
                .observeReceiveMessage(observer, true)
            checkNimStatus()
        }
    }

    private fun initZegoEngine() {
        runCatching {
            val context = application
            val profile = ZegoEngineProfile().apply {
                appID =
                    if (isReleaseServer) ConstantsKey.ZEGO_APP_ID_RELEASE else ConstantsKey.ZEGO_APP_ID_TEST
                scenario = ZegoScenario.HIGH_QUALITY_CHATROOM
                application = context
            }
            mZegoEngine = ZegoExpressEngine.createEngine(profile, zegoEventHandler)
            val audioConfig = ZegoAudioConfig(ZegoAudioConfigPreset.HIGH_QUALITY_STEREO)
            audioConfig.bitrate = 64
            val videoConfig = ZegoVideoConfig(ZegoVideoConfigPreset.PRESET_1080P)
            val engineConfig = ZegoEngineConfig()
            engineConfig.advancedConfig = hashMapOf<String, String>(
                "prep_high_pass_filter" to "true", "enable_music_detection" to "true"
            )
            ZegoExpressEngine.setEngineConfig(engineConfig)
            mZegoEngine?.videoConfig = videoConfig
            mZegoEngine?.audioConfig = audioConfig
            //开启回声消除
            mZegoEngine?.enableHeadphoneAEC(true)
            mZegoEngine?.enableAGC(true)
            //开启ANS
            mZegoEngine?.enableANS(true)
            //场景化AI降噪
            mZegoEngine?.setANSMode(ZegoANSMode.MEDIUM)
            //开启音浪监听
            mZegoEngine?.startSoundLevelMonitor(2000)
        }.onFailure {
            LogUtils.e("zego初始化失败")
        }
    }


    /**
     * 登录即构room
     * @param crId 房间ID
     * @param token token
     */
    private fun loginZegoRoom(crId: String, token: String) {
        val user = ZegoUser(MMKVProvider.userId)
        val roomConfig = ZegoRoomConfig()
        roomConfig.token = token
        roomConfig.isUserStatusNotify = true
        mZegoEngine?.loginRoom(
            crId, user, roomConfig
        ) { errorCode, _ ->
            if (errorCode == 0) {
                val intent = Intent(topActivity, RoomNotificationService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    topActivity.startForegroundService(intent)
                } else {
                    topActivity.startService(intent)
                }
            }
        }
    }


    /**
     * 登录云信房间
     * @param nimRoomId 云信房间id
     */
    private fun loginNimRoom(nimRoomId: String, isSendMessage: Boolean) {
        val enterRoomData = EnterChatRoomData(nimRoomId)
        NIMClient.getService(ChatRoomService::class.java).enterChatRoomEx(enterRoomData, 5)
            .setCallback(object : RequestCallback<EnterChatRoomResultData> {
                override fun onSuccess(param: EnterChatRoomResultData?) {
                    if (isSendMessage) {
                        sendUserJoinMessage(nimRoomId)
                    }
                }

                override fun onFailed(code: Int) {
                }

                override fun onException(exception: Throwable) {

                }
            })
    }

    /**
     * 初始化点歌
     * 只有房间为派对房时初始化
     */
    private fun initMusic() {
        if (roomInfo?.roomTagCategory == Constants.ROOM_TYPE_PARTY) {
            RoomMusicManager.instance.init(mZegoEngine)
        }
    }


    /**
     * 处理房间消息
     */
    private fun observerCustomMessage(attachment: BaseAttachment, msg: ChatRoomMessage) {
        when (attachment.type) {
            Constants.IMMessageType.MSG_TYPE_UP_MIC,
            Constants.IMMessageType.MSG_TYPE_INVITE_MIC -> { //抱上麦消息
                if (topActivity !is LiveAudioActivity){
                    val data = parseCustomMessage<UpMicIMMessage>(attachment.data)
                    //先遍历列表剔除之前的麦位信息
                    val oldModel =
                        roomInfo?.wheatPositionList?.find { t -> t.wheatPositionId == data.userId }
                    if (oldModel != null) {
                        clearMicInfo(oldModel)
                    }
                    val model = roomInfo?.wheatPositionList?.get(data.microphoneNumber)
                    model?.wheatPositionIdName = data.nickname
                    model?.wheatPositionId = data.userId
                    model?.userRole = data.userRole
                    model?.headWearIcon = data.headWearIcon
                    model?.headWearSvga = data.headWearSvga
                    model?.wheatPositionIdHeadPortrait = data.profilePath
                }
            }

            Constants.IMMessageType.MSG_TYPE_KICK_MIC -> {
                if (topActivity !is LiveAudioActivity) {
                    val data = parseCustomMessage<UpMicIMMessage>(attachment.data)
                    val model =
                        roomInfo?.wheatPositionList?.find { it.wheatPositionId == data.userId }
                    if (model != null) {
                        clearMicInfo(model)
                    }
                    if (data.userId == MMKVProvider.userId) {
                        customToast("您已被抱下麦")
                        stopPublishStream()
                    }
                }
            }

            Constants.IMMessageType.MSG_TYPE_QUIT_MIC -> {//抱下麦
                if (topActivity !is LiveAudioActivity) {
                    val data = parseCustomMessage<UpMicIMMessage>(attachment.data)
                    val model =
                        roomInfo?.wheatPositionList?.find { it.wheatPositionId == data.userId }
                    if (model != null) {
                        clearMicInfo(model)
                    }
                    if (data.userId == MMKVProvider.userId) {
                        stopPublishStream()
                    }
                }
            }

            Constants.IMMessageType.MSG_UPDATE_ROOM_INFO -> {//修改房间信息
                val data = parseCustomMessage<UpdateRoomInfoMessage>(attachment.data)
                roomInfo?.roomIcon = data.roomIcon
                roomInfo?.roomAffiche = data.roomAffiche
                roomInfo?.roomTitle = data.roomTitle
                FlowBus.post(Event.RefreshRoomFloatingCoverEvent(data.roomIcon))
            }

            Constants.IMMessageType.MSG_SWITCH_INCOME_OPEN -> { //开启或关闭魅力值
                val data = parseCustomMessage<SwitchIncomeOpenMessage>(attachment.data)
                roomInfo?.charmOnOff = data.charmOnOff
                roomInfo?.wheatPositionList?.forEach {
                    it.isShowIncome = data.charmOnOff
                }
            }

            Constants.IMMessageType.MSG_UPDATE_CHARM_VALUE -> { //更新魅力值
                val data = parseCustomMessage<CharmValueMessage>(attachment.data)
                data.microphoneMessageList.forEachIndexed { index, item ->
                    roomInfo?.wheatPositionList?.get(index)?.wheatPositionIdCharmValue =
                        item.charmValue
                }
            }

            Constants.IMMessageType.MSG_BAN_MIC -> {
                val data = parseCustomMessage<BanMicMessage>(attachment.data)
                if (data.userId == MMKVProvider.userId) {
                    if (data.isMike) {
                        customToast("您被禁麦10分钟")
                        stopPublishStream()
                    } else {
                        customToast("您已被解除禁麦")
                    }
                }
            }

            Constants.IMMessageType.MSG_KICK_OUT_USER -> {
                //踢出房间
                val data = parseCustomMessage<KickOutRoomMessage>(attachment.data)
                if (data.userId == MMKVProvider.userId) {
                    customToast(R.string.room_toast_kick_out_user)
                    release()
                }
            }

            Constants.IMMessageType.MSG_CHAT_CLEAR_MESSAGE -> {
                historyMessage.clear()
            }

            Constants.IMMessageType.MSG_TYPE_GIFT, Constants.IMMessageType.MSG_TYPE_USER_IN, Constants.IMMessageType.MSG_EMOJI, Constants.IMMessageType.MSG_TYPE_ROOM_CHAT_CONTENT -> {
                addHistoryMessage(msg)
            }

            Constants.IMMessageType.MSG_CHAT_WATER -> {
                if (MMKVProvider.room_water_info_show) {
                    addHistoryMessage(msg)
                }
            }
        }
    }

    private fun clearMicInfo(model: MicUserModel) {
        model.wheatPositionId = ""
        model.wheatPositionIdName = ""
        model.wheatPositionIdHeadPortrait = ""
        model.headWearSvga = null
        model.headWearIcon = null
        model.userRole = ""
        roomInfo?.wheatPositionList?.set(model.onMicroPhoneNumber, model)
    }

    @Synchronized
    fun addHistoryMessage(msg: ChatRoomMessage) {
        if (historyMessage.size == 50) {
            historyMessage.removeFirst()
        }
        historyMessage.add(msg)
    }


    /**
     * 发送房间消息
     */
    fun sendUserJoinMessage(nimRoomId: String) {
        getUserInfo(onSuccess = {
            val model = UserEnterMessage(
                profilePath = it.profilePath,
                nickname = it.nickname,
                consumeLevel = it.consumeLevel,
                charmLevel = it.charmLevel,
                carPath = it.userCarUrl,
                userId = it.userId
            )
            val attr = BaseAttachment(Constants.IMMessageType.MSG_TYPE_USER_IN, model)
            val message = ChatRoomMessageBuilder.createChatRoomCustomMessage(nimRoomId, attr)
            NIMClient.getService(ChatRoomService::class.java).sendMessage(message, false)
                .setCallback(object : RequestCallback<Void> {
                    override fun onSuccess(param: Void?) {
                        addHistoryMessage(message)
                        FlowBus.post(Event.InsertUserInMessage(model))
                    }

                    override fun onFailed(code: Int) {
                    }

                    override fun onException(exception: Throwable?) {
                    }
                })
        })
    }

    /**
     * 设置音量
     * @param volume 音量
     */
    fun setVolume(volume: Int) {
        mZegoEngine?.setAllPlayStreamVolume(volume)
    }


    /**
     * 停止推流
     */
    fun stopPublishStream() {
        isPublishStream = false
        mZegoEngine?.stopPublishingStream()
    }


    /**
     * 麦克风静音
     */
    fun muteMicrophone(isMute: Boolean) {
        mZegoEngine?.muteMicrophone(isMute)
    }


    /**
     * 静音视频流
     */
    fun mutePlayStreamVideo(streamId: String) {
        mZegoEngine?.mutePlayStreamVideo(streamId, true)
    }

    /**
     * 判断麦克风是否静音
     */
    fun isMicrophoneMuted(): Boolean = mZegoEngine?.isMicrophoneMuted!!

    /**
     * 开始推流
     * @param streamId 流ID
     * @param isPushCDN 是否转推CDN
     */
    fun startPublishStream(streamId: String, isPushCDN: Boolean) {
        isPublishStream = true
        mZegoEngine?.startPublishingStream(streamId)
        if (isPushCDN) {
            mZegoEngine?.addPublishCdnUrl(streamId, roomInfo?.zeGoPushUrl, null)
        }
    }

    /**
     * 开始拉流 九麦房使用
     * @param streamId 流ID
     */
    fun startPlayStream(streamId: String?) {
        mZegoEngine?.startPlayingStream(streamId)
    }


    /**
     * 开始屏幕共享
     */
    fun startScreenCapture() {
        mZegoEngine?.enableCamera(true)
        mZegoEngine?.setVideoSource(
            ZegoVideoSourceType.SCREEN_CAPTURE, ZegoPublishChannel.MAIN
        )
//        mZegoEngine?.setAudioSource(ZegoAudioSourceType.DEFAULT)
        val audioConfig = ZegoAudioConfig(ZegoAudioConfigPreset.HIGH_QUALITY_STEREO)
        val videoConfig = ZegoVideoConfig(ZegoVideoConfigPreset.PRESET_1080P)
        mZegoEngine?.videoConfig = videoConfig
        mZegoEngine?.audioConfig = audioConfig
        mZegoEngine?.startScreenCapture()
    }

    /**
     * 停止屏幕共享
     */
    fun stopScreenCapture() {
        mZegoEngine?.stopScreenCapture()
    }

    /**
     * 停止拉流
     * @param streamId 流id
     */
    fun stopPlayingStream(streamId: String) {
        mZegoEngine?.stopPlayingStream(streamId)
    }


    private fun exitChatRoom(crId: String?, block: () -> Unit) {
        val param = mutableMapOf<String, Any?>()
        param["chatRoomId"] = crId
        param["userId"] = MMKVProvider.userId
        request<Int>(RoomApi.API_EXIT_ROOM, Method.POST, param, onSuccess = {
            block()
        }, onError = {
            block()
        })
    }


    /**
     * 轮询检查登录状态，如果掉线就手动登录
     */
    private fun checkNimStatus() {

    }


    private fun stopForegroundNotificationService() {
        topActivity.stopService(Intent(topActivity, RoomNotificationService::class.java))
    }


    /**
     * 退出房间，同步执行 保证退出登录的时候不会因为
     */
    fun release(block: (() -> Unit)? = null) {
        if (roomInfo != null) {
            exitChatRoom(roomInfo?.crId) {
                historyMessage.clear()
                allMessageList.clear()
                firstJoinRoomTime = 0L
                isPublishStream = false
                isInMicQueue = false
                videoStreamId = null
                RoomMQTTManager.release()
                FlowBus.post(Event.CloseRoomFloating)
                stopForegroundNotificationService()
                NIMClient.getService(ChatRoomService::class.java)
                    .exitChatRoom(roomInfo?.neteaseChatId)
                mZegoEngine?.logoutRoom()
                NIMClient.getService(ChatRoomServiceObserver::class.java)
                    .observeReceiveMessage(observer, false)
                ZegoExpressEngine.destroyEngine(null)
                mZegoEngine = null
                roomInfo = null
                RoomMusicManager.instance.release()
                block?.invoke()
            }
        } else {
            block?.invoke()
        }
    }

}