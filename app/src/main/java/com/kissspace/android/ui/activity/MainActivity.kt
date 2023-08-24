package com.kissspace.android.ui.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import by.kirich1409.viewbindingdelegate.viewBinding
import coil.load
import com.blankj.utilcode.util.GsonUtils
import com.didi.drouter.annotation.Router
import com.kissspace.util.finishAllActivities
import com.kissspace.util.toast
import com.kissspace.login.widget.InvitationDialog
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.Observer
import com.netease.nimlib.sdk.StatusCode
import com.netease.nimlib.sdk.auth.AuthServiceObserver
import com.netease.nimlib.sdk.msg.MsgService
import com.petterp.floatingx.assist.FxGravity
import com.petterp.floatingx.assist.helper.ScopeHelper
import com.petterp.floatingx.listener.control.IFxControl
import com.kissspace.android.R
import com.kissspace.android.databinding.ActivityMainBinding
import com.kissspace.android.ui.fragment.PartyFragment
import com.kissspace.android.viewmodel.MainViewModel
import com.kissspace.android.widget.UpgradeDialog
import com.kissspace.android.widget.bottombar.BottomTabBean
import com.kissspace.common.config.AppConfigKey
import com.kissspace.common.config.Constants
import com.kissspace.common.router.jump
import com.kissspace.common.ext.safeClick
import com.kissspace.common.flowbus.Event
import com.kissspace.common.flowbus.FlowBus.observerEvent
import com.kissspace.common.http.getAppConfigByKey
import com.kissspace.common.router.RouterPath
import com.kissspace.common.util.*
import com.kissspace.common.util.mmkv.MMKVProvider
import com.kissspace.common.util.mmkv.isShowUpgrade
import com.kissspace.message.ui.fragment.MessageFragment
import com.kissspace.mine.ui.fragment.MineFragment
import com.kissspace.network.result.collectData
import com.kissspace.common.http.getUserInfo
import com.kissspace.common.model.config.RoomGameConfig
import com.kissspace.common.model.config.WaterConfig
import com.kissspace.room.manager.RoomServiceManager
import com.kissspace.util.apkAbsolutePath
import com.kissspace.util.appVersionCode
import com.kissspace.util.deleteDir
import com.kissspace.util.immersiveStatusBar
import com.kissspace.util.loadImageCircle
import com.kissspace.util.logE
import com.kissspace.util.orZero
import com.petterp.floatingx.listener.control.IFxScopeControl
import com.tencent.mmkv.MMKV
import java.io.File
import java.util.*

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/11/4
 * @Description: main activity
 *
 */
@Router(uri = RouterPath.PATH_MAIN)
class MainActivity : com.kissspace.common.base.BaseActivity(R.layout.activity_main) {
    private val mBinding by viewBinding<ActivityMainBinding>()
    private val mViewModel by viewModels<MainViewModel>()
    private var roomFloating: IFxScopeControl<Activity> ?= null
    private var invitationDialog: InvitationDialog? = null
    private var index = 0

    private val onlineStatusObserver = Observer<StatusCode> {
        logE("${it}云信code")
        if (it == StatusCode.KICKOUT || it == StatusCode.KICK_BY_OTHER_CLIENT || it == StatusCode.FORBIDDEN || it == StatusCode.PWD_ERROR || it == StatusCode.DATA_UPGRADE) {
            toast("您的账号已在另一台设备登录")
            loginOut()
        } else if (it.wontAutoLogin()) {
            mViewModel.loginNim()
        } else {
            logE("IM自动重连")
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        index = intent?.getIntExtra("index", 0).orZero()
        mBinding.tabLayout.setDefaultItem(index.orZero())
        mBinding.viewPager.setCurrentItem(index.orZero(), false)
    }


    override fun initView(savedInstanceState: Bundle?) {
        immersiveStatusBar(false)
        pressBackTwiceToExitApp()
        initViewPager()
        mBinding.collectRoomView.setOnClickListener {
            jump(RouterPath.PATH_MY_COLLECT)
        }
        initData()
        initAppConfig()

    }

    private fun initData() {
        mViewModel.checkVersion()
        mViewModel.sayHi()
        //更新用户配置
        mViewModel.getUserInfo()
        initConfig()
        NIMClient.getService(AuthServiceObserver::class.java)
            .observeOnlineStatus(onlineStatusObserver, true)
    }

    private fun initConfig() {
        getAppConfigByKey<Int>(AppConfigKey.CHAT_MIN_LEVEL) {
            MMKVProvider.userChatMinLevel = it
        }
    }

    private fun initAppConfig() {
        Constants.isTokenExpired = false
        //获取微信公众号
        getAppConfigByKey<String>(AppConfigKey.COMMERCE_WECHAT) {
            MMKVProvider.wechatPublicAccount = it
        }
        getAppConfigByKey<List<RoomGameConfig>>(AppConfigKey.ROOM_ACTIVE_PATH_CONFIG) { gameConfig ->
            MMKVProvider.gameConfig = GsonUtils.toJson(gameConfig)
        }
    }

    private fun showOtherDialog() {
//        if (showAdolescentDialog(MMKVProvider.userId)) {
//            TeenagerDialog(this).show()
//        }
    }

    private fun initViewPager() {
        val fragments = arrayOf(PartyFragment(), MessageFragment(), MineFragment())
        mBinding.viewPager.apply {
            isUserInputEnabled = false
            adapter = object : FragmentStateAdapter(this@MainActivity) {
                override fun getItemCount(): Int = fragments.size
                override fun createFragment(position: Int): Fragment = fragments[position]
                override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
                    super.onAttachedToRecyclerView(recyclerView)
                    recyclerView.setItemViewCacheSize(fragments.size)
                }
            }
        }

        val tab1 = BottomTabBean(
            "派对",
            0,
            com.kissspace.module_common.R.color.color_7B6F9F,
            com.kissspace.module_common.R.color.color_FFEF71,
            R.mipmap.icon_tab_party_normal,
            R.mipmap.icon_tab_party_selected,
            true
        )
        val tab2 = BottomTabBean(
            "消息",
            1,
            com.kissspace.module_common.R.color.color_7B6F9F,
            com.kissspace.module_common.R.color.color_7ADFFF,
            R.mipmap.icon_tab_message_normal,
            R.mipmap.icon_tab_message_selected,
            false
        )
        val tab3 = BottomTabBean(
            "我的",
            2,
            com.kissspace.module_common.R.color.color_7B6F9F,
            com.kissspace.module_common.R.color.color_92FFA0,
            R.mipmap.icon_tab_mine_normal,
            R.mipmap.icon_tab_mine_selected,
            false
        )

        mBinding.tabLayout.addTabItem(tab1)
        mBinding.tabLayout.addTabItem(tab2)
        mBinding.tabLayout.addTabItem(tab3)
        mBinding.tabLayout.setDefaultItem(index)
        mBinding.viewPager.setCurrentItem(index.orZero(), false)
        mBinding.tabLayout.setOnTabChangedListener {
            mBinding.viewPager.setCurrentItem(it, false)
            mViewModel.requestCollectList()
            if (it != 0) {
                immersiveStatusBar(true)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateMessageCount()
        mViewModel.requestCollectList()
    }

    override fun createDataObserver() {
        super.createDataObserver()
        collectData(mViewModel.checkVersionEvent, onSuccess = {
            if (it.intVersion > appVersionCode && it.isShowUpgrade()) {
                val dialog = UpgradeDialog.newInstance(it)
                dialog.setDismissCallback {
                    showOtherDialog()
                }
                dialog.show(supportFragmentManager)
            } else {
                showOtherDialog()
            }
        }, onError = {
            deleteDir(File(apkAbsolutePath))
            showOtherDialog()
        }, onEmpty = {
            deleteDir(File(apkAbsolutePath))
            showOtherDialog()
        })
        collectData(mViewModel.collectListEvent, onSuccess = {
            if (it.total > 0 && mBinding.viewPager.currentItem < 2) {
                mBinding.collectRoomView.visibility = View.VISIBLE
                mBinding.collectRoomView.initData(it.records[0].roomIcon, it.total)
            } else {
                mBinding.collectRoomView.visibility = View.GONE
            }

        })
        //监听开启房间悬浮窗事件
        observerEvent<Event.ShowRoomFloating>(this) { room ->
            if (roomFloating == null) {
                showFloating(room.crId, room.roomCover)
            } else {
                roomFloating?.show()
            }
        }

        //监听悬浮窗关闭事件
        observerEvent<Event.CloseRoomFloating>(this) {
            roomFloating?.cancel()
            roomFloating = null
        }
        //监听悬浮窗刷新封面
        observerEvent<Event.RefreshRoomFloatingCoverEvent>(this) {
            val image = roomFloating?.getView()?.findViewById<ImageView>(R.id.image)
            image?.loadImageCircle(it.cover)
        }

        //刷新未读消息数量
        observerEvent<Event.RefreshUnReadMsgCount>(this) {
            updateMessageCount()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        logE("MainActivity_onDestroy")
        NIMClient.getService(AuthServiceObserver::class.java)
            .observeOnlineStatus(onlineStatusObserver, false)
        roomFloating?.cancel()
    }

    private fun showFloating(crId: String, cover: String) {
        roomFloating = ScopeHelper.build {
            val view =
                LayoutInflater.from(this@MainActivity).inflate(R.layout.layout_room_floating, null)
            val imageView = view?.findViewById<ImageView>(R.id.image)
            imageView?.loadImageCircle(cover)
            val animation = AnimationUtils.loadAnimation(
                this@MainActivity, com.kissspace.module_common.R.anim.common_room_floating_rotate
            )
            imageView?.startAnimation(animation)
            setLayoutView(view)
            setGravity(FxGravity.RIGHT_OR_BOTTOM)
            setEnableAssistDirection(0f, 600f, 0f, 40f)
            setEnableEdgeAdsorption(false)
            val ivClose: ImageView = view.findViewById(R.id.iv_close)
            ivClose.safeClick {
                RoomServiceManager.release()
            }
            setOnClickListener {
                jumpRoom(crId = crId)
            }
        }.toControl(this@MainActivity)
        roomFloating?.show()
    }


    private fun updateMessageCount() {
        try {
            val unReadCount =
                NIMClient.getService(MsgService::class.java).totalUnreadCount + MMKVProvider.systemMessageUnReadCount
            mBinding.tabLayout.updateMessageUnReadCount(unReadCount)
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    private fun pressBackTwiceToExitApp() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            private var lastBackTime: Long = 0
            override fun handleOnBackPressed() {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastBackTime > 2000) {
                    toast("再次点击退出应用")
                    lastBackTime = currentTime
                } else {
                    RoomServiceManager.release {
                        finishAllActivities()
                    }
                }
            }
        })
    }
}

