package com.kissspace.message.ui.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import com.blankj.utilcode.util.TimeUtils
import com.didi.drouter.api.DRouter
import com.drake.brv.BindingAdapter
import com.drake.brv.utils.*
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.Observer
import com.netease.nimlib.sdk.msg.MsgService
import com.netease.nimlib.sdk.msg.MsgServiceObserve
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.msg.model.BroadcastMessage
import com.kissspace.common.base.BaseFragment
import com.kissspace.common.config.Constants
import com.kissspace.common.router.jump
import com.kissspace.common.ext.safeClick
import com.kissspace.common.ext.setMarginStatusBar
import com.kissspace.common.flowbus.Event
import com.kissspace.common.flowbus.FlowBus
import com.kissspace.common.model.ChatListModel
import com.kissspace.common.provider.IRoomProvider
import com.kissspace.common.router.RouterPath
import com.kissspace.common.util.getH5Url
import com.kissspace.common.util.mmkv.MMKVProvider
import com.kissspace.common.util.jumpRoom
import com.kissspace.common.util.customToast
import com.kissspace.common.widget.CommonConfirmDialog
import com.kissspace.message.viewmodel.MessageViewModel
import com.kissspace.network.result.collectData
import com.kissspace.common.model.ItemMessageMenu
import com.kissspace.message.widget.ChatDialog
import com.uc.crashsdk.export.LogType.addType
import kotlinx.coroutines.launch
import com.kissspace.module_message.R
import com.kissspace.module_message.databinding.FragmentMessageV2Binding
import com.kissspace.module_message.databinding.MessageItemMenuBinding
import com.kissspace.util.hasNotificationPermission
import com.kissspace.util.logE
import com.kissspace.util.requestNotificationPermission
import com.kissspace.util.swapWithHead

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/11/3
 * @Description: 消息fragment
 *
 */
class MessageFragment : BaseFragment(R.layout.fragment_message_v2) {
    private val mBinding by viewBinding<FragmentMessageV2Binding>()
    private val mViewModel by viewModels<MessageViewModel>()
    private lateinit var mRecentContactAdapter: BindingAdapter
    private var menuList: MutableList<ItemMessageMenu> = ArrayList()

    /**
     * 系统广播监听
     */
    private val broadcastObserver = Observer<BroadcastMessage> {
        mViewModel.requestSystemMessage()
    }

    @SuppressLint("UnsafeOptInUsageError")
    override fun initView(savedInstanceState: Bundle?) {
        mBinding.vm = mViewModel
        viewLifecycleOwner.lifecycle.addObserver(mViewModel)
        initTitleBar()
        initTopItemList()
        initRecyclerView()
        initRefreshLayout()
        registerObserver()

        if (context?.let { hasNotificationPermission(it) } != true) {
            if (!TimeUtils.isToday(MMKVProvider.lastShowNotificationPermission)) {
                mBinding.layoutNotification.visibility = View.VISIBLE
            }
        } else {
            mBinding.layoutNotification.visibility = View.GONE
        }
        mBinding.ivClose.safeClick {
            MMKVProvider.lastShowNotificationPermission = System.currentTimeMillis()
            mBinding.layoutNotification.visibility = View.GONE
        }

        mBinding.tvOpenNotification.safeClick {
            requestNotificationPermission(fragment = this) { isOpenNotification ->
                if (isOpenNotification) {
                    mBinding.layoutNotification.visibility = View.GONE
                }
            }

        }
    }

    private fun initTopItemList() {
        //关闭 RecyclerView 的item刷新动画
        mBinding.rvList.itemAnimator = null
        menuList = mViewModel.getMessageMenu()
        mBinding.rvList.grid(4).setup {
            addType<ItemMessageMenu> { R.layout.message_item_menu }
            onBind {
                val model = getModel<ItemMessageMenu>()
                val viewBinding = getBinding<MessageItemMenuBinding>()
                viewBinding.tvText.text = model.name
                viewBinding.ivTop.setImageResource(model.resourceId)
            }
            onClick(R.id.cl_root) {
                when (modelPosition) {
                    0 -> {
                        jump(RouterPath.PATH_LOVE_WALL)
                    }

                    1 -> {
                        jump(RouterPath.PATH_SYSTEM_MESSAGE)
                    }

                    2 -> {
                        val url = getH5Url(
                            Constants.H5.dynamicInteractivityType,
                            true
                        ) + "&interactionType=002"
                        jump(
                            RouterPath.PATH_WEBVIEW,
                            "url" to url,
                            "showTitle" to false,
                            "showTitleBarMargin" to true
                        )
                    }

                    3 -> {
                        jump(RouterPath.PATH_MESSAGE_TASK)
                    }
                }

            }
        }.models = menuList

    }

    override fun onResume() {
        super.onResume()
        if (mRecentContactAdapter!=null){
            mRecentContactAdapter?.mutable?.clear()
            mRecentContactAdapter?.notifyDataSetChanged()
        }
        initData()
        mViewModel.anchor = null
        mViewModel.queryRecentMessage()
    }

    private fun initTitleBar() {
        if (!isFromDialog()) {
            mBinding.titleBar.setMarginStatusBar()
        } else {
            mBinding.lltRoot.setBackgroundResource(R.drawable.message_bg_corner)
        }
        mBinding.ivClearMessage.safeClick {
            CommonConfirmDialog(
                requireContext(), "忽略未读", "消息气泡会删除掉，但仍然保留消息"
            ) {
                if (this) {
                    NIMClient.getService(MsgService::class.java).clearAllUnreadCount()
                    (mRecentContactAdapter.mutable as MutableList<ChatListModel>).forEach {
                        it.unReadCount = 0
                        it.notifyChange()
                    }
                }
            }.show()
        }
    }


    private fun initRecyclerView() {
        mBinding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        mRecentContactAdapter = BindingAdapter().apply {
            addType<ChatListModel> { R.layout.message_chat_list_item }
            onClick(R.id.root_chat) {
                val model = getModel<ChatListModel>()
                if (parentFragment is ChatDialog) {
                    (parentFragment as ChatDialog).addFragment(
                        ChatFragment.newInstance(
                            model.fromAccount!!,
                            model.fromAccount!!.substring(3, model.fromAccount!!.length),
                            true
                        )
                    )
                } else {
                    jump(
                        RouterPath.PATH_CHAT,
                        "account" to model.fromAccount!!,
                        "userId" to model.fromAccount!!.substring(3, model.fromAccount!!.length)
                    )
                }
            }
            onClick(R.id.layout_follow_room) {
                val model = getModel<ChatListModel>()
                val service = DRouter.build(IRoomProvider::class.java).getService()
                if (service.getRoomId() != model.followRoomId || !isFromDialog()) {
                    jumpRoom(crId = model.followRoomId)
                } else {
                    customToast("您已在当前房间")
                }
            }
            onLongClick(R.id.root_chat) {
                val model = getModel<ChatListModel>()
                val index = (mutable as MutableList<ChatListModel>).indexOf(model)
                CommonConfirmDialog(requireContext(), "确定要删除该条会话?") {
                    if (this) {
                        NIMClient.getService(MsgService::class.java)
                            .deleteRecentContact2(model.fromAccount, SessionTypeEnum.P2P)
                        NIMClient.getService(MsgService::class.java)
                            .clearChattingHistory(model.fromAccount, SessionTypeEnum.P2P, true)
                        mRecentContactAdapter.mutable.remove(model)
                        mRecentContactAdapter.notifyItemRemoved(index)
                        FlowBus.post(Event.RefreshUnReadMsgCount)
                    }
                }.show()
            }
            mutable = arrayListOf()
        }

        val adapter =
            ConcatAdapter(mRecentContactAdapter)

        mBinding.recyclerView.adapter = adapter

    }

    private fun registerObserver() {
        NIMClient.getService(MsgServiceObserve::class.java)
            .observeBroadcastMessage(broadcastObserver, true)
    }

    private fun initRefreshLayout() {
        mBinding.pageRefreshLayout.apply {
            setEnableRefresh(false)
            setEnableLoadMore(true)
            setOnLoadMoreListener {
                mViewModel.queryRecentMessage()
            }
        }
    }

    private fun initData() {
        mViewModel.requestBannerData()
        mViewModel.requestSystemMessage()
        mViewModel.requestDynamicMessageCount()
//        menuList[0].unReadCount = MMKVProvider.systemMessageUnReadCount
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun createDataObserver() {
        super.createDataObserver()
        collectData(mViewModel.getRecentMsgListEvent, onSuccess = {
            if (it.isEmpty() || it.size < 20) {
                mBinding.pageRefreshLayout.finishLoadMoreWithNoMoreData()
            } else {
                mBinding.pageRefreshLayout.finishLoadMore()
            }
            mRecentContactAdapter.models = it
            FlowBus.post(Event.RefreshUnReadMsgCount)
        }, onEmpty = {
            mBinding.pageRefreshLayout.finishLoadMoreWithNoMoreData()
            FlowBus.post(Event.RefreshUnReadMsgCount)
        })

        collectData(mViewModel.dynamicMessageCountEvent, onSuccess = {
//            menuList[0].unReadCount = it.likeMessage
            menuList[2].unReadCount = it.interactiveMessages
            mBinding.rvList.adapter?.notifyItemChanged(0)
            mBinding.rvList.adapter?.notifyItemChanged(2)
        })
        collectData(mViewModel.systemMessageEvent, onSuccess = {
            if (it.records.isNotEmpty()) {
                val data = it.records[0]
                data.unReadCount = it.total - MMKVProvider.systemMessageLastReadCount
                MMKVProvider.systemMessageUnReadCount = data.unReadCount
                menuList[1].unReadCount = MMKVProvider.systemMessageUnReadCount
                mBinding.rvList.adapter?.notifyItemChanged(1)
            }
            FlowBus.post(Event.RefreshUnReadMsgCount)
        })

        collectData(mViewModel.updateRecentEvent, onSuccess = {
            //从历史消息里面获取
            val history = mRecentContactAdapter.mutable as MutableList<ChatListModel>
            it.forEach { model ->
                var existModel: ChatListModel? = null
                var postion = -1
                history.forEachIndexed { index, chatListModel ->
                    if (chatListModel.fromAccount == model.fromAccount) {
                        existModel = chatListModel
                        postion = index
                    }
                }
                if (existModel == null) {
                    mRecentContactAdapter.addModels(mutableListOf(model), index = 0)
                } else {
                    //还是老的昵称和头像
                    existModel?.nickname = model.nickname
                    //显示的昵称
                    logE("model.nickname" + model.nickname)
                    existModel?.avatar = model.avatar
                    existModel?.content = model.content
                    existModel?.unReadCount = model.unReadCount
                    existModel?.date = model.date
                    existModel?.followRoomId = model.followRoomId
                    //交换位置
                    mRecentContactAdapter._data =
                        swapWithHead(mRecentContactAdapter.models?.toMutableList(), postion)
                    mRecentContactAdapter.notifyDataSetChanged()
                }
            }
        })

        FlowBus.observerEvent<Event.RefreshUnReadMsgCount>(this) {
            mViewModel.updateUnReadCount()
        }

        FlowBus.observerEvent<Event.RefreshChangeAccountEvent>(this) {
            mRecentContactAdapter.mutable.clear()
            mRecentContactAdapter.notifyDataSetChanged()
            mViewModel.anchor = null
            mViewModel.queryRecentMessage()
        }

        FlowBus.observerEvent<Event.MsgSystemEvent>(this) {
            mViewModel.requestSystemMessage()
        }

        FlowBus.observerEvent<Event.MsgRefreshDynamicNoticeEvent>(this) {
            mViewModel.requestDynamicMessageCount()
        }

        FlowBus.observerEvent<Event.NotificationEventOpen>(this) {
            mBinding.layoutNotification.visibility = View.GONE
        }
    }

    private fun isFromDialog() = parentFragment != null && parentFragment is ChatDialog


    override fun onDestroy() {
        super.onDestroy()
        //注销监听
        NIMClient.getService(MsgServiceObserve::class.java)
            .observeBroadcastMessage(broadcastObserver, false)

    }


}

