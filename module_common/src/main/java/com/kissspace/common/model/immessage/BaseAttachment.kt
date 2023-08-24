package com.kissspace.common.model.immessage

import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.LogUtils
import com.netease.nimlib.sdk.msg.attachment.MsgAttachment

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/12/16 13:07
 * @Description:
 *
 */
data class BaseAttachment(var type: String, var data: Any) : MsgAttachment {
    override fun toJson(send: Boolean): String = GsonUtils.toJson(this)
}