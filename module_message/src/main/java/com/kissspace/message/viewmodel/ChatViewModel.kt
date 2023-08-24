package com.kissspace.message.viewmodel

import android.text.TextUtils
import androidx.databinding.ObservableField
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.ToastUtils
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.RequestCallback
import com.netease.nimlib.sdk.RequestCallbackWrapper
import com.netease.nimlib.sdk.friend.FriendService
import com.netease.nimlib.sdk.msg.MessageBuilder
import com.netease.nimlib.sdk.msg.MsgService
import com.netease.nimlib.sdk.msg.attachment.AudioAttachment
import com.netease.nimlib.sdk.msg.attachment.ImageAttachment
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.msg.model.IMMessage
import com.netease.nimlib.sdk.msg.model.NIMAntiSpamOption
import com.netease.nimlib.sdk.msg.model.QueryDirectionEnum
import com.kissspace.common.base.BaseViewModel
import com.kissspace.common.config.CommonApi
import com.kissspace.common.config.Constants
import com.kissspace.common.http.getUserInfo
import com.kissspace.common.model.ChatEmojiListBean
import com.kissspace.common.model.ChatModel
import com.kissspace.common.model.UserInfoBean
import com.kissspace.common.model.UserProfileBean
import com.kissspace.common.model.immessage.BaseAttachment
import com.kissspace.common.model.immessage.ChatGiftMessage
import com.kissspace.common.util.EmojiGameUtil
import com.kissspace.common.util.mmkv.MMKVProvider
import com.kissspace.common.util.parseCustomMessage
import com.kissspace.common.util.customToast
import com.kissspace.network.net.Method
import com.kissspace.network.net.request
import com.kissspace.network.result.ResultState
import com.kissspace.util.fromJson
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File
import kotlin.math.ceil


class ChatViewModel : BaseViewModel() {
    //接收人信息
    val mineUserInfo = ObservableField<UserInfoBean>()

    //发送人信息
    val friendUserInfo = ObservableField<UserProfileBean>()


    val getChatListEvent = MutableSharedFlow<MutableList<ChatModel>>()
    private var lastMessage: IMMessage? = null

    private val _getEmojiListEvent = MutableSharedFlow<ResultState<List<ChatEmojiListBean>>>()
    val getEmojiListEvent = _getEmojiListEvent.asSharedFlow()

    private val _followUserEvent = MutableSharedFlow<ResultState<Int>>()
    val followUserEvent = _followUserEvent.asSharedFlow()

    private val _cancelFollowEvent = MutableSharedFlow<ResultState<Int>>()
    val cancelFollowEvent = _cancelFollowEvent.asSharedFlow()

    private val _banUserEvent = MutableSharedFlow<ResultState<String>>()
    val banUserEvent = _banUserEvent.asSharedFlow()

    fun requestUserInfo(account: String, userId: String) {
        getUserInfo(onSuccess = {
            mineUserInfo.set(it)
            val params = mutableMapOf<String, Any?>("userId" to userId)
            request<UserProfileBean>(
                CommonApi.API_QUERY_USER_PROFILE,
                Method.GET,
                params,
                onSuccess = { that ->
                    friendUserInfo.set(that)
                    queryMessage(account, true)
                })
        })
    }


    fun requestFriendInfo(userId: String) {
        val params = mutableMapOf<String, Any?>("userId" to userId)
        request<UserProfileBean>(CommonApi.API_QUERY_USER_PROFILE, Method.GET, params, onSuccess = {
            friendUserInfo.set(it)
        })
    }

    fun sendChatTask() {
        if (Constants.isFinishChatTask) {
            return
        }
        val params = mutableMapOf<String, Any?>("sendType" to 1)
        request<Boolean>(CommonApi.API_TASK_SEND_MESSAGE, Method.POST, params, onSuccess = {
            Constants.isFinishChatTask = it
        })
    }

    private fun followUser(userId: String?) {
        val param = mutableMapOf<String, Any?>("userId" to userId)
        request<Int>(CommonApi.API_FOLLOW_USER, Method.POST, param, onSuccess = {
            val user = friendUserInfo.get()
            user?.attention = true
            friendUserInfo.set(user)
            friendUserInfo.notifyChange()
        })
    }

    private fun cancelFollow(userId: String?) {
        val param = mutableMapOf<String, Any?>("userId" to userId)
        request<Int>(CommonApi.API_CANCEL_FOLLOW_USER, Method.GET, param, onSuccess = {
            val user = friendUserInfo.get()
            user?.attention = false
            friendUserInfo.set(user)
            friendUserInfo.notifyChange()
        })
    }

    fun requestEmojiList() {
        request(
            CommonApi.API_QUERY_EMOJI_LIST, Method.GET, state = _getEmojiListEvent
        )
    }

    fun follow() {
        val user = friendUserInfo.get()
        if (user?.attention == true) {
            cancelFollow(user?.userId)
        } else {
            followUser(user?.userId)
        }
    }

    fun sendTextMessage(
        account: String, content: String, isShowTime: Int, block: (ChatModel) -> Unit
    ) {
        if (checkBlack()) {
            return
        }
        if (content.length > 50) {
            customToast("最多输入50个字符")
            return
        }
        val message = MessageBuilder.createTextMessage(
            account, SessionTypeEnum.P2P, content
        )
        message.nimAntiSpamOption = NIMAntiSpamOption()
        val remote = mutableMapOf<String, Any?>()
        remote["fromUserId"] = mineUserInfo.get()?.userId
        remote["fromNickname"] = mineUserInfo.get()?.nickname
        remote["fromProfilePath"] = mineUserInfo.get()?.profilePath
        remote["targetUserId"] = friendUserInfo.get()?.userId
        remote["targetNickname"] = friendUserInfo.get()?.nickname
        remote["targetProfilePath"] = friendUserInfo.get()?.profilePath
        remote["isShowTime"] = isShowTime
        message.remoteExtension = remote
        NIMClient.getService(MsgService::class.java).sendMessage(message, false)
            .setCallback(object : RequestCallback<Void?> {
                override fun onSuccess(param: Void?) {
                    val model = ChatModel(
                        avatar = mineUserInfo.get()?.profilePath,
                        nickname = mineUserInfo.get()?.nickname,
                        content = content,
                        isReceive = false,
                        isShowTime = isShowTime,
                        messageType = Constants.ChatMessageType.TYPE_CONTENT,
                        timestamp = System.currentTimeMillis(),
                        imMessage = message
                    )
                    block(model)
                }

                override fun onFailed(code: Int) {
                    if (code == 7101) {
                        customToast("您已被该用户拉黑，将无法与该用户进行私聊", true)
                    }
                }

                override fun onException(exception: Throwable?) {
                    ToastUtils.showLong(exception?.message)
                }
            })
    }


    fun sendPictureMessage(
        account: String, file: File, isShowTime: Int
    ) {
        if (checkBlack()) {
            return
        }
        val message = MessageBuilder.createImageMessage(
            account, SessionTypeEnum.P2P, file, file.name
        )
        message.nimAntiSpamOption = NIMAntiSpamOption()
        val remote = mutableMapOf<String, Any?>()
        remote["fromUserId"] = mineUserInfo.get()?.userId
        remote["fromNickname"] = mineUserInfo.get()?.nickname
        remote["fromProfilePath"] = mineUserInfo.get()?.profilePath
        remote["targetUserId"] = friendUserInfo.get()?.userId
        remote["targetNickname"] = friendUserInfo.get()?.nickname
        remote["targetProfilePath"] = friendUserInfo.get()?.profilePath
        remote["isShowTime"] = isShowTime
        message.remoteExtension = remote
        NIMClient.getService(MsgService::class.java).sendMessage(message, false)
            .setCallback(object : RequestCallback<Void?> {
                override fun onSuccess(param: Void?) {

                }

                override fun onFailed(code: Int) {
                    if (code == 7101) {
                        customToast("您已被该用户拉黑，将无法与该用户进行私聊", true)
                    }
                }

                override fun onException(exception: Throwable?) {
                }
            })
    }


    fun sendAudioMessage(
        account: String,
        file: File,
        audioLength: Long,
        isShowTime: Int,
    ) {
        if (checkBlack()) {
            return
        }
        val message = MessageBuilder.createAudioMessage(
            account, SessionTypeEnum.P2P, file, audioLength
        )
        message.nimAntiSpamOption = NIMAntiSpamOption()
        val remote = mutableMapOf<String, Any?>()
        remote["fromUserId"] = mineUserInfo.get()?.userId
        remote["fromNickname"] = mineUserInfo.get()?.nickname
        remote["fromProfilePath"] = mineUserInfo.get()?.profilePath
        remote["targetUserId"] = friendUserInfo.get()?.userId
        remote["targetNickname"] = friendUserInfo.get()?.nickname
        remote["targetProfilePath"] = friendUserInfo.get()?.profilePath
        remote["isShowTime"] = isShowTime
        message.remoteExtension = remote
        NIMClient.getService(MsgService::class.java).sendMessage(message, false)
            .setCallback(object : RequestCallback<Void?> {
                override fun onSuccess(param: Void?) {
                }

                override fun onFailed(code: Int) {
                    if (code == 7101) {
                        customToast("您已被该用户拉黑，将无法与该用户进行私聊", true)
                    }
                }

                override fun onException(exception: Throwable?) {
                }
            })
    }

    fun sendGiftMessage(
        account: String,
        giftName: String,
        total: Int,
        url: String,
        svga: String?,
        price: Int,
        giftId: String,
        isShowTime: Int,
        block: (ChatModel) -> Unit
    ) {
        if (checkBlack()) {
            return
        }
        val giftData = ChatGiftMessage(
            giftName = giftName,
            total = total,
            url = url,
            svg = svga,
            price = price,
            giftId = giftId
        )
        val attachment = BaseAttachment(Constants.IMMessageType.MSG_CHAT_SEND_GIFT, giftData)
        val message = MessageBuilder.createCustomMessage(
            account, SessionTypeEnum.P2P, "这是礼物消息", attachment
        )
        message.nimAntiSpamOption = NIMAntiSpamOption()
        val remote = mutableMapOf<String, Any?>()
        remote["fromUserId"] = mineUserInfo.get()?.userId
        remote["fromNickname"] = mineUserInfo.get()?.nickname
        remote["fromProfilePath"] = mineUserInfo.get()?.profilePath
        remote["targetUserId"] = friendUserInfo.get()?.userId
        remote["targetNickname"] = friendUserInfo.get()?.nickname
        remote["targetProfilePath"] = friendUserInfo.get()?.profilePath
        remote["isShowTime"] = isShowTime
        message.remoteExtension = remote
        NIMClient.getService(MsgService::class.java).sendMessage(message, false)
            .setCallback(object : RequestCallback<Void?> {
                override fun onSuccess(param: Void?) {
                    val model = ChatModel(
                        avatar = mineUserInfo.get()?.profilePath,
                        nickname = mineUserInfo.get()?.nickname,
                        giftUrl = url,
                        giftName = giftName,
                        giftNum = total,
                        isReceive = false,
                        messageType = Constants.ChatMessageType.TYPE_GIFT,
                        timestamp = System.currentTimeMillis(),
                        imMessage = message
                    )
                    block(model)
                }

                override fun onFailed(code: Int) {
                    if (code == 7101) {
                        customToast("您已被该用户拉黑，将无法与该用户进行私聊", true)
                    }
                }

                override fun onException(exception: Throwable?) {
                }
            })
    }


    fun sendEmojiMessage(
        account: String, emoji: ChatEmojiListBean, isShowTime: Int, block: (ChatModel) -> Unit
    ) {
        if (checkBlack()) {
            return
        }

        val attachment = BaseAttachment(Constants.IMMessageType.MSG_CHAT_SEND_EMOJI, emoji)
        val message = MessageBuilder.createCustomMessage(
            account, SessionTypeEnum.P2P, attachment
        )
        message.nimAntiSpamOption = NIMAntiSpamOption()
        val remote = mutableMapOf<String, Any?>()
        remote["fromUserId"] = mineUserInfo.get()?.userId
        remote["fromNickname"] = mineUserInfo.get()?.nickname
        remote["fromProfilePath"] = mineUserInfo.get()?.profilePath
        remote["targetUserId"] = friendUserInfo.get()?.userId
        remote["targetNickname"] = friendUserInfo.get()?.nickname
        remote["targetProfilePath"] = friendUserInfo.get()?.profilePath
        remote["isShowTime"] = isShowTime
        message.remoteExtension = remote
        NIMClient.getService(MsgService::class.java).sendMessage(message, false)
            .setCallback(object : RequestCallback<Void?> {
                override fun onSuccess(param: Void?) {
                    val model = ChatModel(
                        avatar = mineUserInfo.get()?.profilePath,
                        nickname = mineUserInfo.get()?.nickname,
                        isReceive = false,
                        emojiUrl = emoji.dynamicImage,
                        messageType = Constants.ChatMessageType.TYPE_EMOJI,
                        timestamp = System.currentTimeMillis(),
                        imMessage = message,
                        name = emoji.name,
                        emojiGameIndex = emoji.emojiGameIndex,
                        isEmojiLoop = emoji.isEmojiLoop
                    )
                    block(model)
                }

                override fun onFailed(code: Int) {
                    if (code == 7101) {
                        customToast("您已被该用户拉黑，将无法与该用户进行私聊", true)
                    }
                }

                override fun onException(exception: Throwable?) {
                }
            })
    }

    fun queryMessage(account: String, isFirstLoad: Boolean) {
        //清空未读消息数
        NIMClient.getService(MsgService::class.java).clearUnreadCount(account, SessionTypeEnum.P2P)
        if (lastMessage == null) {
            lastMessage = NIMClient.getService(MsgService::class.java)
                .queryLastMessage(account, SessionTypeEnum.P2P)
        }
        NIMClient.getService(MsgService::class.java)
            .queryMessageListEx(lastMessage, QueryDirectionEnum.QUERY_OLD, 20, true)
            .setCallback(object : RequestCallbackWrapper<List<IMMessage>>() {
                override fun onResult(code: Int, result: List<IMMessage>?, exception: Throwable?) {
                    val messageList = mutableListOf<IMMessage>()
                    if (result != null && result.isNotEmpty()) {
                        messageList.addAll(result)
                    }
                    if (isFirstLoad && lastMessage != null) {
                        messageList.add(lastMessage!!)
                    }
                    parseMessageList(messageList)
                    if (messageList.isNotEmpty()) {
                        lastMessage = messageList[0]
                    }
                }
            })
    }

    private fun parseMessageList(messageList: MutableList<IMMessage>) {
        val chatModelList = mutableListOf<ChatModel>()
        messageList.forEach {
            val chatModel = when (it.msgType) {
                MsgTypeEnum.image -> parseImageMessage(it)
                MsgTypeEnum.text -> parseTextMessage(it)
                MsgTypeEnum.audio -> parseAudioMessage(it)
                MsgTypeEnum.custom -> {
                    val json = JSONObject(it.attachStr)
                    when (json.getString("type")) {
                        Constants.IMMessageType.MSG_CHAT_SEND_GIFT -> parseGiftMessage(it)
                        Constants.IMMessageType.MSG_CHAT_SEND_EMOJI -> parseEmojiMessage(it)
                        else -> parseEmojiMessage(it)
                    }
                }

                else -> parseTextMessage(it)
            }
            if (chatModel != null) {
                chatModelList.add(chatModel)
            }
        }
        viewModelScope.launch { getChatListEvent.emit(chatModelList) }
    }

    fun parseTextMessage(it: IMMessage): ChatModel {
        return ChatModel(
            avatar = it.getAvatar(),
            nickname = it.getNickname(),
            content = it.content,
            timestamp = it.time,
            isShowTime = it.isShowTime(),
            isReceive = it.fromAccount != MMKVProvider.accId,
            messageType = Constants.ChatMessageType.TYPE_CONTENT,
            imMessage = it
        )
    }

    fun parseGiftMessage(it: IMMessage): ChatModel {
        val json = JSONObject(it.attachStr)
        val data = parseCustomMessage<ChatGiftMessage>(json.getJSONObject("data"))
        return ChatModel(
            avatar = it.getAvatar(),
            nickname = it.getNickname(),
            timestamp = it.time,
            isShowTime = it.isShowTime(),
            giftUrl = data.url,
            giftName = data.giftName,
            giftNum = data.total,
            giftSvga = data.svg,
            isReceive = it.fromAccount != MMKVProvider.accId,
            messageType = Constants.ChatMessageType.TYPE_GIFT,
            imMessage = it
        )
    }

    fun parseEmojiMessage(it: IMMessage): ChatModel {
        val json = JSONObject(it.attachStr)
        val data = parseCustomMessage<ChatEmojiListBean>(json.getJSONObject("data"))
        if (EmojiGameUtil.isEmojiGame(data.name) && !TextUtils.isEmpty(data.emojiGameIndex)){
            data.dynamicImage = EmojiGameUtil.getEmojiPath(data.name,data.emojiGameIndex)
            data.isEmojiLoop = false
        } else {
            data.emojiGameIndex = ""
            data.isEmojiLoop = true
            data.emojiGameImage = 0
        }
        return ChatModel(
            avatar = it.getAvatar(),
            nickname = it.getNickname(),
            timestamp = it.time,
            isShowTime = it.isShowTime(),
            emojiUrl = data.dynamicImage,
            isReceive = it.fromAccount != MMKVProvider.accId,
            messageType = Constants.ChatMessageType.TYPE_EMOJI,
            imMessage = it,
            name = data.name,
            emojiGameIndex =  data.emojiGameIndex,
            isEmojiLoop = data.isEmojiLoop
        )
    }

    fun parseImageMessage(it: IMMessage): ChatModel {
        return ChatModel(
            avatar = it.getAvatar(),
            nickname = it.getNickname(),
            content = it.content,
            timestamp = it.time,
            isShowTime = it.isShowTime(),
            picture = (it.attachment as ImageAttachment).url,
            isReceive = it.fromAccount != MMKVProvider.accId,
            messageType = Constants.ChatMessageType.TYPE_PICTURE,
            thumbUrl = (it.attachment as ImageAttachment).thumbUrl,
            imMessage = it
        )
    }

    fun parseAudioMessage(it: IMMessage): ChatModel? {
        val audioAttachment = it.attachment as AudioAttachment
        var audioFile: File? = null
        if (audioAttachment.path != null) {
            audioFile = File(audioAttachment.path)
        }
        if (audioFile?.exists() != true) {
            return null
        }
        val audioTime = ceil(audioAttachment.duration.toDouble() / 1000).toLong()
        return ChatModel(
            avatar = it.getAvatar(),
            nickname = it.getNickname(),
            audioFile = audioFile,
            audioTime = audioTime,
            isShowTime = it.isShowTime(),
            timestamp = it.time,
            isReceive = it.fromAccount != MMKVProvider.accId,
            messageType = Constants.ChatMessageType.TYPE_AUDIO,
            isAudioPlayed = it.isAudioPlayed(),
            imMessage = it
        )
    }

    fun banUser() {
        val params = mutableMapOf<String, Any?>()
        params["userId"] = friendUserInfo.get()?.userId
        val url =
            if (friendUserInfo.get()?.pulledBlack == true) CommonApi.API_CANCNEL_BAN_USER else CommonApi.API_BAN_USER
        request<Int>(url, Method.POST, params, onSuccess = {
            val user = friendUserInfo.get()
            user?.let {
                customToast(if (it.pulledBlack) "已取消拉黑" else "已拉黑", true)
                if (!it.pulledBlack) {
                    viewModelScope.launch { _banUserEvent.emit(ResultState.onAppSuccess(it.accid)) }
                } else {
                    it.pulledBlack = false
                    friendUserInfo.set(it)
                    friendUserInfo.notifyChange()
                }
            }
        }, onError = {
            viewModelScope.launch { _banUserEvent.emit(ResultState.onAppError(it)) }
        })
    }

    private fun checkBlack(): Boolean {
        if (NIMClient.getService(FriendService::class.java)
                .isInBlackList(friendUserInfo?.get()?.accid)
        ) {
            customToast("您已拉黑此用户", true)
            return true
        }
        return false
    }


    private fun IMMessage.getAvatar(): String? {
        val url =
            if (this.fromAccount == friendUserInfo?.get()?.accid) friendUserInfo.get()?.profilePath else mineUserInfo.get()?.profilePath
        return url
    }

    private fun IMMessage.getNickname(): String? {
        return if (this.fromAccount == friendUserInfo?.get()?.accid) friendUserInfo.get()?.nickname else mineUserInfo.get()?.nickname
    }

    private fun IMMessage.isShowTime() =
        if (this.remoteExtension["isShowTime"] == null) 0 else this.remoteExtension["isShowTime"].toString()
            .toInt()

    private fun IMMessage.isAudioPlayed() = when {
        this.fromAccount == MMKVProvider.accId -> true
        this.localExtension == null || this.localExtension["isAudioPlayed"] == null -> false
        else -> this.localExtension["isAudioPlayed"].toString().toBoolean()
    }


}