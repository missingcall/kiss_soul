package com.kissspace.common.base

import com.blankj.utilcode.util.GsonUtils
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.msg.MsgServiceObserve
import com.kissspace.common.config.Constants
import com.kissspace.common.flowbus.Event
import com.kissspace.common.flowbus.FlowBus
import com.kissspace.common.model.custommessage.GlobalNotificationMessage
import com.kissspace.common.model.immessage.BanUserMessage
import com.kissspace.common.util.loginOut
import com.kissspace.common.util.mmkv.MMKVProvider
import com.kissspace.common.util.parseCustomMessage
import com.kissspace.common.widget.CommonConfirmDialog
import com.kissspace.util.context
import com.kissspace.util.fromJson
import com.kissspace.util.logE
import com.kissspace.util.toast
import com.kissspace.util.topActivity
import org.json.JSONObject

/**
 * @Author gaohangbo
 * @Date 2023/2/14 13:58.
 * @Describe 自定义消息通知
 */
object CustomNotificationObserver {
    //刷新金币
    const val MESSAGE_TRANSFER_COIN = "026"

    //刷新任务的领取积分
    const val MESSAGE_REFRESH_INTERGRAL = "034"

    //加入家族申请
    const val MESSAGE_FAMILY_APPLY: String = "029"

    //加入家族申请通过
    const val MESSAGE_FAMILY_PASS = "030"

    //家族成员退出
    const val MESSAGE_FAMILY_OUT = "031"

    //移出家族成员
    const val MESSAGE_FAMILY_MOVE_OUT = "032"

    //家族创建
    const val MESSAGE_FAMILY_CREATED = "033"

    //意见反馈
    const val MESSAGE_FEEDBACK_REPLY = "050"

    const val MESSAGE_BAN_USER = "058"

    //星际庄园游戏结束
    const val MESSAGE_INTERSTELLAR_GAME_END = "069"

    //星际庄园游戏开始
    const val MESSAGE_INTERSTELLAR_GAME_START = "070"


    fun initCustomNotificationObserver() {
        NIMClient.getService(MsgServiceObserve::class.java)
            .observeCustomNotification({ message -> // 处理自定义系统通知。
                logE("收到自定义通知---${message.content}")
                val json = JSONObject(message.content)
                when (json.getString("type")) {
                    MESSAGE_BAN_USER -> {
                        val data = fromJson<BanUserMessage>(json.getJSONObject("data").toString())
                        if (data.userId == MMKVProvider.userId) {
                            topActivity?.let {
                                CommonConfirmDialog(
                                    it.context,
                                    title = "提示",
                                    subTitle = data.reason + "解封时间：" + data.frozenDeadline,
                                    isShowNegative = false,
                                    cancelable = false
                                ) {
                                    if (this) {
                                        loginOut()
                                    }
                                }.show()
                            }
                        }
                    }
                    MESSAGE_TRANSFER_COIN -> {
                        FlowBus.post(Event.RefreshCoin)
                    }
                    MESSAGE_REFRESH_INTERGRAL -> {
                        FlowBus.post(Event.RefreshPoints)
                    }
                    MESSAGE_FEEDBACK_REPLY ->{
                        FlowBus.post(Event.RefreshPoints)
                    }
                    Constants.IMMessageType.MSG_SYSTEM  ->{
                        FlowBus.post(Event.MsgSystemEvent)
                    }
                    MESSAGE_FAMILY_APPLY,
                    MESSAGE_FAMILY_OUT,
                    MESSAGE_FAMILY_MOVE_OUT->{
                        FlowBus.post(Event.MsgFamilyEvent)
                    }
                    MESSAGE_FAMILY_PASS ->{
                        FlowBus.post(Event.MsgFamilyPassEvent)
                    }
                    else -> {

                    }
                }
            }, true)

        NIMClient.getService(MsgServiceObserve::class.java)
            .observeBroadcastMessage({ message -> // 处理自定义系统通知。
                val json = JSONObject(message.content)
                when (json.getString("type")) {
                    Constants.IMMessageType.MSG_SYSTEM  ->{
                        FlowBus.post(Event.MsgSystemEvent)
                    }
                    MESSAGE_INTERSTELLAR_GAME_END ->{
                        FlowBus.post(Event.H5InterstellarEvent(json.getJSONObject("data").toString()))
                    }
                    MESSAGE_INTERSTELLAR_GAME_START ->{
                        FlowBus.post(Event.H5InterstellarEvent(json.getJSONObject("data").toString()))
                    }
                    else -> {

                    }
                }
            }, true)

    }


}