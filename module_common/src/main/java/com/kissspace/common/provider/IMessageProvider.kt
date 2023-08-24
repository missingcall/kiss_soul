package com.kissspace.common.provider

import androidx.fragment.app.FragmentManager

/**
 *
 * @Author: nicko
 * @CreateDate: 2023/3/4 14:58
 * @Description: 消息模块provider
 *
 */

interface IMessageProvider {

    /**
     * 显示聊天弹窗
     * @param fm
     * @param account 对方云信账号
     */
    fun showChatDialog(fm: FragmentManager, account: String)
}