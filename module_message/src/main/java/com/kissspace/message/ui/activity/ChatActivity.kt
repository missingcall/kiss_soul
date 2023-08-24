package com.kissspace.message.ui.activity

import android.os.Bundle
import androidx.fragment.app.commit
import by.kirich1409.viewbindingdelegate.viewBinding
import com.didi.drouter.annotation.Router
import com.kissspace.util.*
import com.kissspace.common.base.BaseActivity
import com.kissspace.common.router.parseIntent
import com.kissspace.common.ext.setMarginStatusBar
import com.kissspace.common.router.RouterPath
import com.kissspace.message.ui.fragment.ChatFragment
import com.kissspace.module_message.R
import com.kissspace.module_message.databinding.MessageActivityChatBinding

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/12/16 11:08
 * @Description: 聊天页面
 *
 */
@Router(path = RouterPath.PATH_CHAT)
class ChatActivity : com.kissspace.common.base.BaseActivity(R.layout.message_activity_chat) {
    private val account by parseIntent<String>()
    private val userId by parseIntent<String>()
    private val mBinding by viewBinding<MessageActivityChatBinding>()
    override fun initView(savedInstanceState: Bundle?) {
        immersiveStatusBar(true)
        supportFragmentManager.commit {
            replace(R.id.container, ChatFragment.newInstance(account, userId, false))
        }
    }



}