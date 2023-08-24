package com.kissspace.message.widget

import com.kissspace.common.model.ChatEmojiListBean
import java.io.File

interface OnChatPanelCallBack {
    fun onSendTextMessage(text: String)

    fun onSendImageMessage(path: String)

    fun onSendAudioMessage(file: File?, length: Long)

    fun onRecordCountDown(count: Long)

    fun onTalkTooShort()

    fun onShowCancelTalk()

    fun onShowStartTalk(isCountDown: Boolean)

    fun dismissAudioToast()

    fun onClickEmoji(emojiListBean: ChatEmojiListBean)

}