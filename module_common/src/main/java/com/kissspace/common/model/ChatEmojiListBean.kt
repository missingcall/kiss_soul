package com.kissspace.common.model

@kotlinx.serialization.Serializable
data class ChatEmojiListBean(
    var chatEmojiId: String,
    var dynamicImage: String,
    var staticImage: String,
    var name: String,
    var emojiGameIndex:String = "",
    var emojiGameImage:Int = 0,
    var isEmojiLoop:Boolean = true
)