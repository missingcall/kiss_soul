package com.kissspace.room.ui.fragment

import android.Manifest
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import by.kirich1409.viewbindingdelegate.viewBinding
import com.blankj.utilcode.util.LogUtils
import com.kissspace.common.base.BaseFragment
import com.kissspace.common.config.AppConfigKey
import com.kissspace.common.config.Constants
import com.tencent.qgame.animplayer.util.ScaleType
import com.kissspace.util.append
import com.kissspace.util.dp
import com.kissspace.common.ext.safeClick
import com.kissspace.common.http.getAppConfigByKey
import com.kissspace.common.model.RoomInfoBean
import com.kissspace.common.model.UserInfoBean
import com.kissspace.common.model.immessage.BaseAttachment
import com.kissspace.common.model.immessage.ChangeBackgroundMessage
import com.kissspace.common.model.immessage.GiftNotifyMessage
import com.kissspace.common.model.immessage.WaterNotifyMessage
import com.kissspace.common.util.fromJson
import com.kissspace.common.util.getMP4Path
import com.kissspace.common.util.hideLoading
import com.kissspace.common.util.jumpRoom
import com.kissspace.common.util.mmkv.MMKVProvider
import com.kissspace.common.util.parseCustomMessage
import com.kissspace.common.util.showLoading
import com.kissspace.common.widget.CommonConfirmDialog
import com.kissspace.util.resToColor
import com.kissspace.module_room.R
import com.kissspace.module_room.databinding.RoomActivityAudioBinding
import com.kissspace.module_room.databinding.RoomFragmentAudioBinding
import com.kissspace.module_room.databinding.RoomFragmentAudioMainBinding
import com.kissspace.network.result.collectData
import com.kissspace.room.manager.RoomServiceManager
import com.kissspace.room.viewmodel.LiveViewModel
import com.kissspace.util.hasNotificationPermission
import com.kissspace.util.ifNullOrEmpty
import com.kissspace.util.loadImage
import com.kissspace.util.logE
import com.kissspace.util.requestNotificationPermission
import com.kissspace.util.toast
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.Observer
import com.netease.nimlib.sdk.chatroom.ChatRoomService
import com.netease.nimlib.sdk.chatroom.ChatRoomServiceObserver
import com.netease.nimlib.sdk.chatroom.model.ChatRoomMessage
import com.netease.nimlib.sdk.chatroom.model.EnterChatRoomData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.lang.Exception
import java.net.URL

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/12/2
 * @Description: 九麦房
 *
 */
class LiveAudioFragment : BaseFragment(R.layout.room_fragment_audio),
    Observer<List<ChatRoomMessage>> {
    private val mBinding by viewBinding<RoomFragmentAudioBinding>()
    private val mViewModel by viewModels<LiveViewModel>()
    private var crId: String? = null
    private var stochastic: String? = null
    private var userId: String? = null
    var needRefresh: Boolean = false
    var mUserInfo: UserInfoBean? = null
    private var roomInfoBean: RoomInfoBean? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->

    }

    companion object {
        fun newInstance(
            crId: String?,
            stochastic: String?,
            userId: String?,
            roomInfo: String?
        ) = LiveAudioFragment().apply {
            arguments = bundleOf(
                "crId" to crId,
                "stochastic" to stochastic,
                "userId" to userId,
                "roomInfo" to roomInfo
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        crId = arguments?.getString("crId")
        stochastic = arguments?.getString("stochastic")
        userId = arguments?.getString("userOId")
        val roomInfo = arguments?.getString("roomInfo")
        if (roomInfo != null) {
            roomInfoBean = fromJson(roomInfo)
        }
        NIMClient.getService(ChatRoomServiceObserver::class.java).observeReceiveMessage(this, true)
        if (!hasNotificationPermission(requireContext()) && Build.VERSION.SDK_INT >= 33) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        mViewModel.getUserInfo()
        logE("roomInfoBean======${roomInfoBean}")
        if (roomInfoBean == null) {
            mViewModel.handleRoomInfo(crId, Constants.ROOM_TYPE_PARTY, stochastic, userId)
        } else {
            refreshRoomInfo(roomInfoBean!!)
        }
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

    private fun onChangeBackgroundMessage(message: ChangeBackgroundMessage) {
        if (message.chatRoomId == roomInfoBean!!.crId) {
            getRoomInfo()!!.backgroundDynamicImage = message.dynamicImage.ifNullOrEmpty("")
            getRoomInfo()!!.backgroundStaticImage = message.staticImage.ifNullOrEmpty("")
            loadBackground(message.dynamicImage.ifNullOrEmpty(message.staticImage))
        }
    }


    fun getRoomInfo() = roomInfoBean!!

    private fun loadBackground(url: String) {
        if (url.endsWith(".mp4")) {
            mBinding.animBackground.visibility = View.VISIBLE
            mBinding.animBackground.setScaleType(ScaleType.CENTER_CROP)
            mBinding.animBackground.setLoop(Int.MAX_VALUE)
            getMP4Path(url) {
                lifecycleScope.launch {
                    mBinding.animBackground.startPlay(File(it))
                }
            }
        } else {
            mBinding.animBackground.visibility = View.GONE
            mBinding.ivBackground.loadImage(url)
        }
    }

    private fun initViewPager() {
        mBinding.viewPager.apply {
            adapter = object : FragmentStateAdapter(this@LiveAudioFragment) {
                override fun getItemCount(): Int = 3
                override fun createFragment(position: Int): Fragment {
                    return when (position) {
                        0 -> RoomUserListFragment.newInstance(
                            getRoomInfo()!!.crId, getRoomInfo()!!.userRole
                        )

                        1 -> LiveAudioMainFragment()
                        else -> RoomRankFragment.newInstance(
                            getRoomInfo()!!.crId,
                            getRoomInfo()!!.userRole
                        )
                    }
                }

                override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
                    super.onAttachedToRecyclerView(recyclerView)
                    recyclerView.setItemViewCacheSize(3)
                }

            }
            setCurrentItem(1, false)
        }
    }

    override fun createDataObserver() {
        super.createDataObserver()
        collectData(mViewModel.getUserInfoEvent, onSuccess = {
            mUserInfo = it
        })

        collectData(mViewModel.getRoomInfoEvent, onSuccess = {
            needRefresh = false
            hideLoading()
            roomInfoBean = it
            loadBackground(it.backgroundDynamicImage.ifEmpty { it.backgroundStaticImage })
            initViewPager()
        })
        collectData(mViewModel.refreshRoomInfoEvent, onSuccess = {
            refreshRoomInfo(it)
        })

    }

    private fun refreshRoomInfo(room: RoomInfoBean) {
        needRefresh = true
        hideLoading()
        roomInfoBean = room
        loadBackground(room.backgroundDynamicImage.ifEmpty { room.backgroundStaticImage })
        initViewPager()
    }

    private fun playGiftNotifyAnimation(message: GiftNotifyMessage) {
        val span = buildSpannedString {
            color(com.kissspace.module_common.R.color.common_white.resToColor()) {
                append("哇哦！")
            }
            color(com.kissspace.module_common.R.color.color_E9C1FF.resToColor()) {
                append(message.sourceUserNickname)
            }
            color(com.kissspace.module_common.R.color.common_white.resToColor()) {
                append("赠送给")
            }
            color(com.kissspace.module_common.R.color.color_E9C1FF.resToColor()) {
                append(message.targetUserNickname)
            }
            color(com.kissspace.module_common.R.color.common_white.resToColor()) {
                append(message.giftName + "X" + message.number + "。豪气云天，火速去围观！")
            }
        }
        mBinding.giftNotifyView.playAnimation(
            message.floatTipUrl,
            message.giftUrl,
            span,
            lifecycleScope,
            "003"
        )
        mBinding.giftNotifyView.safeClick {
            if (message.chatRoomId != getRoomInfo().crId && message.chatRoomId.isNotEmpty()) {
                CommonConfirmDialog(
                    requireContext(), title = "确定前往${message.roomTitle}吗",
                ) {
                    if (this) {
                        jumpRoom(crId = message.chatRoomId)
                    }
                }.show()
            }
        }
    }


    private fun playWaterNotifyAnimation(message: WaterNotifyMessage) {
        lifecycleScope.launch {
            val drawable = buildGiftDrawable(message.giftUrl)
            val span = buildSpannedString {
                color(com.kissspace.module_common.R.color.common_white.resToColor()) {
                    append("人品爆发,恭喜")
                }
                color(com.kissspace.module_common.R.color.color_FAE421.resToColor()) {
                    append(message.waterTreeNickname)
                }
                color(com.kissspace.module_common.R.color.common_white.resToColor()) {
                    if (!message.gameName.isNullOrEmpty()) {
                        append("在${message.gameName}中获得")
                    } else {
                        append("在游戏中获得")
                    }
                    if (drawable != null) {
                        append(drawable, 22.dp.toInt(), 22.dp.toInt())
                    }
                    append("x" + message.number)
                }
            }
            mBinding.giftNotifyView.playAnimation(
                message.floatTipUrl,
                message.giftUrl,
                span,
                lifecycleScope,
                message.gameType
            )
            mBinding.giftNotifyView.safeClick {
                if (message.chatRoomId != getRoomInfo()!!.crId && message.chatRoomId.isNotEmpty()) {
                    jumpRoom(crId = message.chatRoomId)
                }
            }
        }
    }

    private suspend fun buildGiftDrawable(url: String): Drawable? {
        return try {
            withContext(Dispatchers.IO) {
                Drawable.createFromStream(URL(url).openStream(), "image.jpg")
            }
        } catch (e: Exception) {
            null
        }
    }

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
                if (it.type == Constants.IMMessageType.MSG_NOTIFY_GIFT) {
                    val data = parseCustomMessage<GiftNotifyMessage>(it.data)
                    playGiftNotifyAnimation(data)
                }
                if (it.type == Constants.IMMessageType.MSG_NOTIFY_WATER) {
                    val data = parseCustomMessage<WaterNotifyMessage>(it.data)
                    playWaterNotifyAnimation(data)
                }

            }.onFailure {
                LogUtils.e("消息格式错误！${it.message}")
            }
        }
    }

    fun exitRoom() {
        RoomServiceManager.release()
        requireActivity()?.finish()
    }


    override fun onDestroy() {
        super.onDestroy()
        NIMClient.getService(ChatRoomServiceObserver::class.java).observeReceiveMessage(this, false)
    }

}