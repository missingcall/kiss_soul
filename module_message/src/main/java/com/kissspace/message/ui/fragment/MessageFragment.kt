package com.kissspace.message.ui.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import com.didi.drouter.api.DRouter
import com.drake.brv.BindingAdapter
import com.drake.brv.utils.*
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.Observer
import com.netease.nimlib.sdk.msg.MsgService
import com.netease.nimlib.sdk.msg.MsgServiceObserve
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.msg.model.BroadcastMessage
import com.netease.nimlib.sdk.msg.model.CustomNotification
import com.kissspace.common.base.BaseFragment
import com.kissspace.common.config.Constants
import com.kissspace.common.router.jump
import com.kissspace.common.ext.safeClick
import com.kissspace.common.ext.setMarginStatusBar
import com.kissspace.common.flowbus.Event
import com.kissspace.common.flowbus.FlowBus
import com.kissspace.common.model.ChatListModel
import com.kissspace.common.model.LoveWallResponse
import com.kissspace.common.model.SystemMessageModel
import com.kissspace.common.provider.IRoomProvider
import com.kissspace.common.router.RouterPath
import com.kissspace.common.util.getH5Url
import com.kissspace.common.util.mmkv.MMKVProvider
import com.kissspace.common.util.jumpRoom
import com.kissspace.common.util.customToast
import com.kissspace.common.widget.CommonConfirmDialog
import com.kissspace.message.viewmodel.MessageViewModel
import com.kissspace.module_message.R
import com.kissspace.module_message.databinding.FragmentMessageV2Binding
import com.kissspace.network.result.collectData
import com.kissspace.common.http.getUserInfo
import com.kissspace.message.widget.ChatDialog

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
    private lateinit var mBannerAdapter: BindingAdapter
    private lateinit var mBossRankAdapter: BindingAdapter
    private lateinit var mSystemMessageAdapter: BindingAdapter
    private lateinit var mRecentContactAdapter: BindingAdapter

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
        initRecyclerView()
        initRefreshLayout()
        registerObserver()
        mViewModel.queryRecentMessage()
    }

    override fun onResume() {
        super.onResume()
        requestRankPermission()
        initData()
    }

    private fun initTitleBar() {
        if (!isFromDialog()) {
            mBinding.titleBar.setMarginStatusBar()
//            mBinding.lltRoot.setBackgroundResource(R.drawable.message_bg_normal)
        } else {
//            mBinding.lltRoot.setBackgroundResource(R.drawable.message_bg_corner)
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
                    (mSystemMessageAdapter.mutable as MutableList<SystemMessageModel>).forEach {
                        it.unReadCount = 0
                        it.notifyChange()
                    }
                    mBinding.tvUnreadCount.visibility = View.GONE
                    mViewModel.unReadSystemMessage()
                }
            }.show()
        }
    }


    private fun initRecyclerView() {
        mBinding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        mBannerAdapter = BindingAdapter().apply {
            addType<LoveWallResponse> { R.layout.message_layout_banner }
            mutable = arrayListOf()
        }
        mBossRankAdapter = BindingAdapter().apply {
            addType<String> { R.layout.message_boss_rank }
            mutable = arrayListOf()
            onClick(R.id.root_boss) {
                val url = getModel<String>()
                jump(RouterPath.PATH_WEBVIEW, "url" to url, "showTitle" to true)
            }
        }
        mSystemMessageAdapter = BindingAdapter().apply {
            addType<SystemMessageModel> { R.layout.message_chat_list_item_system }
            mutable = arrayListOf()
            onClick(R.id.root_system_message) {
                jump(RouterPath.PATH_SYSTEM_MESSAGE)
            }

        }
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

        val adapter = if (isFromDialog()) {
            ConcatAdapter(mSystemMessageAdapter, mRecentContactAdapter)
        } else {
            ConcatAdapter(
                mBannerAdapter,
                mBossRankAdapter,
                mSystemMessageAdapter,
                mRecentContactAdapter
            )
        }

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
    }

    override fun createDataObserver() {
        super.createDataObserver()
        collectData(mViewModel.getRecentMsgListEvent, onSuccess = {
            if (it.isEmpty() || it.size < 20) {
                mBinding.pageRefreshLayout.finishLoadMoreWithNoMoreData()
            } else {
                mBinding.pageRefreshLayout.finishLoadMore()
            }
            mRecentContactAdapter.addModels(it)
            FlowBus.post(Event.RefreshUnReadMsgCount)
        }, onEmpty = {
            mBinding.pageRefreshLayout.finishLoadMoreWithNoMoreData()
            FlowBus.post(Event.RefreshUnReadMsgCount)
        })

        collectData(mViewModel.bannerEvent, onSuccess = {
            mBannerAdapter.mutable.clear()
            if (it.giveGiftRecordList.isNotEmpty()) {
                mBannerAdapter.addModels(mutableListOf(it))
            } else {
                mBannerAdapter.notifyDataSetChanged()
            }
        })

        collectData(mViewModel.systemMessageEvent, onSuccess = {
            if (it.records.isNotEmpty()) {
                val data = it.records[0]
                data.unReadCount = it.total - MMKVProvider.systemMessageLastReadCount
                MMKVProvider.systemMessageUnReadCount = data.unReadCount
                mSystemMessageAdapter.mutable.clear()
                mSystemMessageAdapter.addModels(mutableListOf(data))
            } else {
                mSystemMessageAdapter.mutable.clear()
                mSystemMessageAdapter.notifyDataSetChanged()
            }
            FlowBus.post(Event.RefreshUnReadMsgCount)
        })

        collectData(mViewModel.updateRecentEvent, onSuccess = {
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
                    existModel?.nickname = model.nickname
                    existModel?.avatar = model.avatar
                    existModel?.content = model.content
                    existModel?.unReadCount = model.unReadCount
                    existModel?.date = model.date
                    existModel?.followRoomId = model.followRoomId
                    if (postion == 0) {
                        existModel?.notifyChange()
                    } else {
                        history.removeAt(postion)
                        mRecentContactAdapter.notifyItemRemoved(postion)
                        mRecentContactAdapter.addModels(mutableListOf(existModel), index = 0)
                    }
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
    }

    private fun isFromDialog() = parentFragment != null && parentFragment is ChatDialog

    /**
     * 查询是否拥有大佬榜查看权限
     */
    private fun requestRankPermission() {
        getUserInfo(onSuccess = {
            if (it.userRightList.contains(Constants.UserPermission.PERMISSION_CHECK_RICH_RANK) || it.userRightList.contains(
                    Constants.UserPermission.PERMISSION_CHECK_GIFT_RANK
                ) || it.userRightList.contains(Constants.UserPermission.PERMISSION_CHECK_POINTS_RANK)
            ) {
                mBossRankAdapter.mutable.clear()
                var rights =
                    it.userRightList.joinToString(separator = ",", prefix = "[", postfix = "]")
                val url = "${getH5Url(Constants.H5.rankBossUrl, true)}&rankType=${rights}"
                mBossRankAdapter.addModels(mutableListOf(url))
            } else {
                mBossRankAdapter.mutable.clear()
                mBossRankAdapter.notifyDataSetChanged()
            }
        })
    }


    override fun onDestroy() {
        super.onDestroy()
        //注销监听
        NIMClient.getService(MsgServiceObserve::class.java)
            .observeBroadcastMessage(broadcastObserver, false)

    }


}

