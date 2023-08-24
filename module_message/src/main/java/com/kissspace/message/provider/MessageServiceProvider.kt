package com.kissspace.message.provider

import androidx.fragment.app.FragmentManager
import com.didi.drouter.annotation.Service
import com.kissspace.common.provider.IMessageProvider
import com.kissspace.message.widget.ChatDialog


@Service(function = [IMessageProvider::class])
class MessageServiceProvider : IMessageProvider {
    override fun showChatDialog(fm: FragmentManager, account: String) {
        ChatDialog.newInstance(account).show(fm, "ChatDialog")
    }

}