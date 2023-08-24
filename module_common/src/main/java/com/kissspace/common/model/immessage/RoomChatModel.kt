package com.kissspace.common.model.immessage

import android.text.SpannedString
import androidx.databinding.BaseObservable
import com.kissspace.common.model.UserMedalBean

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/12/16 10:06
 * @Description: 房间公聊消息model
 *
 */
data class RoomChatMessageModel(
    var profilePath: String? = null,//头像地址
    var userId: String? = null,//用户ID
    var nickname: String? = null,//昵称
    var contentSpan: SpannedString? = null,//内容
    var content: String? = null,
    var emojiUrl: String? = null,//表情包
    var wealthLevel: Int? = null,//财富等级
    var charmLevel: Int? = null,//魅力等级
    var type: Int = 0,//消息类型
    var privilege: String = "",//特权用户
    var headwearUrl: String? = null,
    var medalList: List<UserMedalBean>? = null,
    var name:String ="",
    var emojiGameIndex:String = "",
    var isEmojiLoop:Boolean = true,
    var isEmojiEnd:Boolean = false,
    var isEmojiImage : Int = 0,
    var messageKindList: List<String> = mutableListOf(),
    var chatTabIndex: String = "001"
) : BaseObservable()