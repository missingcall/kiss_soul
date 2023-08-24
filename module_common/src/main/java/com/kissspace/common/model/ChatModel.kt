package com.kissspace.common.model

import androidx.databinding.BaseObservable
import com.netease.nimlib.sdk.msg.model.IMMessage
import com.kissspace.common.config.Constants
import java.io.File

data class ChatModel(
    val imMessage: IMMessage,
    var timestamp: Long,//时间戳,
    var nickname: String? = null,
    var uuid: String? = null,//消息uuid
    var avatar: String?,//发送人头像
    var messageType: Constants.ChatMessageType,//消息类型
    var isReceive: Boolean,//收到的还是发出去的消息
    var content: String? = null,//文本消息内容
    var audioFile: File? = null,//语音消息文件
    var isAudioPlayed: Boolean = false,//语音是否已读
    var audioTime: Long? = null,//语音消息长度（秒）
    var picture: String? = null,//图片消息url
    var thumbUrl: String? = null,//缩略图
    var progress: Long? = null,
    var total: Long? = null,
    var isShowTime: Int = 0,
    var isPlayAudioAnimation: Boolean = false,
    var giftUrl: String? = null,
    var giftSvga: String? = null,
    var emojiUrl: String? = null,
    var giftName: String? = null,
    var giftNum: Int? = null,
    var name:String ="",
    var emojiGameIndex:String = "",
    var isEmojiLoop:Boolean = true
) : BaseObservable()