package com.kissspace.room.ui.activity

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import com.blankj.utilcode.util.LogUtils
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.Observer
import com.netease.nimlib.sdk.chatroom.ChatRoomService
import com.netease.nimlib.sdk.chatroom.ChatRoomServiceObserver
import com.netease.nimlib.sdk.chatroom.model.ChatRoomMessage
import com.netease.nimlib.sdk.chatroom.model.EnterChatRoomData
import com.kissspace.common.config.AppConfigKey
import com.kissspace.common.config.Constants
import com.kissspace.common.flowbus.Event
import com.kissspace.common.flowbus.FlowBus
import com.kissspace.common.http.getAppConfigByKey
import com.kissspace.common.model.RoomInfoBean
import com.kissspace.common.model.immessage.BaseAttachment
import com.kissspace.common.model.immessage.ChangeBackgroundMessage
import com.kissspace.common.util.mmkv.MMKVProvider
import com.kissspace.common.util.parseCustomMessage
import com.kissspace.room.manager.RoomServiceManager
import com.kissspace.util.hasNotificationPermission
import com.kissspace.util.immersiveStatusBar
import com.kissspace.util.toast
import org.json.JSONObject


/**
 *
 * @Author: nicko
 * @CreateDate: 2022/12/12
 * @Description: 直播基类
 *
 */
abstract class BaseLiveActivity(layoutId: Int) : com.kissspace.common.base.BaseActivity(layoutId),
    Observer<List<ChatRoomMessage>> {
    private val REQUEST_NOTIFICATIONS = 1

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {

        } else {

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NIMClient.getService(ChatRoomServiceObserver::class.java).observeReceiveMessage(this, true)
        if (!hasNotificationPermission(this) && Build.VERSION.SDK_INT >= 33) {
            requestPermissionLauncher.launch("android.permission.POST_NOTIFICATIONS")
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        immersiveStatusBar(false)
        MMKVProvider.lastGiftTabIndex = 0
        MMKVProvider.lastGiftIds = ""
        loginSystemRoom()
    }

    private fun loginSystemRoom() {
        getAppConfigByKey<String>(AppConfigKey.DAEMON_NETEASE_ROOM_ID) {
            val enterRoomData = EnterChatRoomData(it)
            NIMClient.getService(ChatRoomService::class.java).enterChatRoomEx(enterRoomData, 3)
        }
    }

    private fun logoutSystemRoom() {
        getAppConfigByKey<String>(AppConfigKey.DAEMON_NETEASE_ROOM_ID) {
            NIMClient.getService(ChatRoomService::class.java).exitChatRoom(it)
        }
    }

    fun exitRoom() {
        RoomServiceManager.release()
        finish()
    }

    //解析自定义消息
    override fun onEvent(t: List<ChatRoomMessage>?) {
        t?.forEach {
            kotlin.runCatching {
                val json = JSONObject(it.attachStr)
                Log.e("", "收到自定义消息---->${json}")
                val type = json.getString("type")
                BaseAttachment(type, json.getJSONObject("data"))
            }.onSuccess {
                if (it.type == Constants.IMMessageType.MSG_CHANGE_BACKGROUND) {
                    val data = parseCustomMessage<ChangeBackgroundMessage>(it.data)
                    onChangeBackgroundMessage(data)
                }
                if (it.type == Constants.IMMessageType.MSG_ROOM_BAN) {
                    toast("该房间被封禁")
                    exitRoom()
                }
            }.onFailure {
                LogUtils.e("消息格式错误！${it.message}")
            }
        }
    }

    abstract fun onChangeBackgroundMessage(message: ChangeBackgroundMessage)

    abstract fun getRoomInfo(): RoomInfoBean


    override fun handleBackPressed() {
        getRoomInfo()?.let {
            FlowBus.post(Event.ShowRoomFloating(it.crId, it.roomIcon.orEmpty()))
        }
        super.handleBackPressed()
    }


    override fun onDestroy() {
        super.onDestroy()
        NIMClient.getService(ChatRoomServiceObserver::class.java).observeReceiveMessage(this, false)
    }
}