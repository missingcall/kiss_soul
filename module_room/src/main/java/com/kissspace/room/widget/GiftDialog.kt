package com.kissspace.room.widget

import android.content.DialogInterface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.angcyo.tablayout.DslTabLayout
import com.angcyo.tablayout.ViewPagerDelegate
import com.angcyo.tablayout.delegate2.ViewPager2Delegate
import com.blankj.utilcode.util.LogUtils
import com.didi.drouter.api.DRouter
import com.drake.brv.utils.addModels
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.linear
import com.drake.brv.utils.mutable
import com.drake.brv.utils.setup
import com.kissspace.network.net.Method
import com.kissspace.network.net.request
import com.kissspace.util.dp
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.Observer
import com.netease.nimlib.sdk.chatroom.ChatRoomServiceObserver
import com.netease.nimlib.sdk.chatroom.model.ChatRoomMessage
import com.kissspace.common.config.AppConfigKey
import com.kissspace.common.config.Constants
import com.kissspace.common.router.jump
import com.kissspace.common.ext.safeClick
import com.kissspace.common.ext.scrollToBottom
import com.kissspace.common.ext.setUpViewPager2
import com.kissspace.common.flowbus.Event
import com.kissspace.common.flowbus.FlowBus
import com.kissspace.common.http.getAppConfigByKey
import com.kissspace.common.http.getSelectPayChannelList
import com.kissspace.common.http.getUserInfo
import com.kissspace.common.model.GiftModel
import com.kissspace.common.model.GiftTabModel
import com.kissspace.common.model.MicUserModel
import com.kissspace.common.model.PackGiftModel
import com.kissspace.common.model.immessage.BaseAttachment
import com.kissspace.common.model.immessage.RoomChatMessageModel
import com.kissspace.common.provider.IPayProvider
import com.kissspace.common.router.RouterPath
import com.kissspace.common.util.mmkv.MMKVProvider
import com.kissspace.common.util.getMutable
import com.kissspace.common.util.customToast
import com.kissspace.common.widget.BaseDialogFragment
import com.kissspace.common.widget.CommonConfirmDialog
import com.kissspace.module_room.R
import com.kissspace.module_room.databinding.RoomDialogGiftBinding
import com.kissspace.network.net.*
import com.kissspace.network.result.collectData
import com.kissspace.room.http.RoomApi
import com.kissspace.room.manager.RoomServiceManager
import com.kissspace.room.ui.fragment.GiftListFragment
import com.kissspace.room.viewmodel.GiftViewModel
import org.json.JSONObject
import java.lang.Exception

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/12/19 14:22
 * @Description: 送礼弹窗
 *
 */
class GiftDialog : BaseDialogFragment<RoomDialogGiftBinding>(Gravity.BOTTOM) {
    private lateinit var crId: String
    private lateinit var userId: String
    private var isDarkMode = true
    private var sendCount = 1
    private var isChooseLucky = false

    //上次选择的用户id集合
    private var lastCheckedUser = mutableListOf<String>()

    //是否要开积分盲盒
    private var isOpenBox: Boolean = false

    //点击来源
    private var from = Constants.GiftDialogFrom.FROM_PARTY

    private var isIntegralBoxChecked = false

    private val mViewModel by viewModels<GiftViewModel>()

    companion object {
        fun newInstance(
            crId: String,
            userId: String,
            isOpenBox: Boolean,
            from: Int,
            isChooseLucky: Boolean = false
        ) = GiftDialog().apply {
            arguments = bundleOf(
                "crId" to crId,
                "userId" to userId,
                "isOpenBox" to isOpenBox,
                "from" to from,
                "isChooseLucky" to isChooseLucky
            )
        }
    }

    private val customMessageObservable = Observer<List<ChatRoomMessage>> {
        it.forEach { that ->
            try {
                val json = JSONObject(that.attachStr)
                val type = json.getString("type")
                val attachment = BaseAttachment(type, json.get("data"))
                when (attachment.type) {
                    Constants.IMMessageType.MSG_TYPE_UP_MIC, Constants.IMMessageType.MSG_TYPE_INVITE_MIC -> {
                        //用户上麦
                        if (!isIntegralBoxChecked && from != Constants.GiftDialogFrom.FROM_PROFILE) {
                            mViewModel.switchUserList(crId, userId, isIntegralBoxChecked, from)
                        }
                    }

                    Constants.IMMessageType.MSG_TYPE_KICK_MIC, Constants.IMMessageType.MSG_TYPE_QUIT_MIC -> {
                        //用户下麦
                        if (!isIntegralBoxChecked && from != Constants.GiftDialogFrom.FROM_PROFILE) {
                            mViewModel.switchUserList(crId, userId, isIntegralBoxChecked, from)
                        }
                    }
                }
            } catch (e: Exception) {
                LogUtils.e("消息格式错误！${e.message}")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            crId = it.getString("crId")!!
            userId = it.getString("userId")!!
            isOpenBox = it.getBoolean("isOpenBox", false)
            from = it.getInt("from")
            isChooseLucky = it.getBoolean("isChooseLucky", false)
        }
        if (from == Constants.GiftDialogFrom.FROM_PROFILE || isOpenBox) {
            lastCheckedUser.add(userId)
        }
        NIMClient.getService(ChatRoomServiceObserver::class.java)
            .observeReceiveMessage(customMessageObservable, true)
    }

    override fun getLayoutId() = R.layout.room_dialog_gift

    override fun initView() {
        mBinding.vm = mViewModel
        initRecyclerView()
        initEvent()
        mViewModel.isDarkMode.set(isDarkMode)
        mViewModel.requestUserInfo()
        mViewModel.switchUserList(crId, userId, isOpenBox, from)
        mViewModel.getTabList()
    }

    private fun initRecyclerView() {
        mBinding.recyclerUsers.linear(LinearLayout.HORIZONTAL).setup {
            addType<MicUserModel> { R.layout.room_dialog_gift_user_item }
            onChecked { position, isChecked, checkedAll ->
                val model = getModel<MicUserModel>(position)
                model.checked = isChecked
                model.notifyChange()
                mViewModel.isCheckedAllUser.set(checkedAll)

            }
            onFastClick(R.id.root) {
                checkedSwitch(modelPosition)
                lastCheckedUser.clear()
                lastCheckedUser.addAll(getCheckedModels<MicUserModel>().map { it.wheatPositionId })
            }
        }.models = mutableListOf()
    }

    private fun initEvent() {
        //全选用户
        mBinding.tvCheckedAllUser.setOnClickListener {
            if (mBinding.recyclerUsers.mutable.isNotEmpty()) {
                val checked = mViewModel.isCheckedAllUser.get()!!
                mBinding.recyclerUsers.bindingAdapter.checkedAll(!checked)
                mViewModel.isCheckedAllUser.set(!checked)
            }
            if (mViewModel.isCheckedAllUser.get()!!) {
                lastCheckedUser.clear()
                lastCheckedUser.addAll(mBinding.recyclerUsers.getMutable<MicUserModel>()
                    .map { it.wheatPositionId })
            } else {
                lastCheckedUser.clear()
            }
        }

        //点击充值
        mBinding.rltRecharge.safeClick {
            getSelectPayChannelList { list ->
                DRouter.build(IPayProvider::class.java).getService()
                    .showPayDialogFragment(parentFragmentManager, list) {
                        getUserInfo(onSuccess = { userinfo ->
                            mViewModel.userInfo.set(userinfo)
                        })
                    }
            }

        }

        //选择数量
        mBinding.layoutGiftSendCount.setOnClickListener {
            if (mViewModel.isCheckedAllGift.get() == true) {
                return@setOnClickListener
            }
            GiftCountPop(requireContext()) {
                sendCount = this
                mBinding.tvGiftSendCount.text = this.toString()
            }.showDialog()
        }

        mBinding.tvCheckedAllGift.setOnClickListener {
            val checked = mViewModel.isCheckedAllGift.get()!!
            checkedAllGift(!checked)
            mViewModel.isCheckedAllGift.set(!checked)
            if (mViewModel.isCheckedAllGift.get() == true) {
                mBinding.tvCheckedAllUser.visibility = View.GONE
                mBinding.recyclerUsers.bindingAdapter.singleMode = true
                if (mBinding.recyclerUsers.bindingAdapter.checkedCount > 1) {
                    mBinding.recyclerUsers.bindingAdapter.checkedAll(false)
                }
            } else {
                mBinding.tvCheckedAllUser.visibility = View.VISIBLE
                mBinding.recyclerUsers.bindingAdapter.singleMode = false
            }
        }

        //点击赠送
        mBinding.tvSendGift.safeClick {
            val checkedGift = getCheckedGift()
            val checkedUser =
                if (!crId.isNullOrEmpty()) mBinding.recyclerUsers.bindingAdapter.getCheckedModels() else mutableListOf(
                    MicUserModel(wheatPositionId = userId)
                )
            if (checkedGift.isNullOrEmpty()) {
                customToast("请选择礼物及送礼对象")
                return@safeClick
            }
            if (checkedUser.isNullOrEmpty()) {
                customToast("请选择礼物及送礼对象")
                return@safeClick
            }
            if (mViewModel.isCheckedAllGift.get() == true) {
                CommonConfirmDialog(requireContext(), "是否将背包内礼物全部送出？") {
                    if (this) {
                        mViewModel.sendGift(
                            crId,
                            sendCount,
                            checkedGift,
                            checkedUser,
                            mViewModel.isCheckedAllGift.get()!!
                        )
                    }
                }.show()
            } else {
                mViewModel.sendGift(
                    crId, sendCount, checkedGift, checkedUser, mViewModel.isCheckedAllGift.get()!!
                )
            }
        }

    }

    override fun observerData() {
        super.observerData()
        collectData(mViewModel.getTabListEvent, onSuccess = {
            initTab(it)
        })
        collectData(mViewModel.getOnMicUsersEvent, onSuccess = {
            mBinding.recyclerUsers.mutable.clear()
            mBinding.recyclerUsers.bindingAdapter.checkedPosition.clear()
            mBinding.recyclerUsers.addModels(it)
            //恢复上次选中的送礼人
            it.forEachIndexed { index, model ->
                if (lastCheckedUser.contains(model.wheatPositionId)) {
                    mBinding.recyclerUsers.bindingAdapter.setChecked(index, true)
                }
            }
            mViewModel.isCheckedAllUser.set(mBinding.recyclerUsers.bindingAdapter.isCheckedAll())
        }, onEmpty = {
            mBinding.recyclerUsers.mutable.clear()
            mBinding.recyclerUsers.bindingAdapter.checkedPosition.clear()
            mBinding.recyclerUsers.bindingAdapter.notifyDataSetChanged()
            mViewModel.isCheckedAllUser.set(false)
        })
        collectData(mViewModel.sendGiftEvent, onSuccess = {
            customToast("送礼成功")
            if (from == Constants.GiftDialogFrom.FROM_CHAT) {
                FlowBus.post(Event.SendGiftEvent(it))
            }
            mViewModel.requestUserInfo()
            if (mBinding.viewPager.currentItem == mBinding.tabLayout.childCount - 1) {
                val fragment =
                    childFragmentManager.findFragmentByTag("f${mBinding.viewPager.currentItem}")
                (fragment as GiftListFragment).updateGiftList(it)
            }

        }, onError = {
            customToast(it.errorMsg)
        })
        collectData(mViewModel.openPointsBoxEvent, onSuccess = {
            OpenIntegralDialog.newInstance(it).show(childFragmentManager)
            FlowBus.post(Event.RefreshIntegralEvent)
            mViewModel.requestUserInfo()
        }, onError = {
            customToast(it.message)
        })

    }

    private fun initTab(tabList: MutableList<GiftTabModel>) {
        tabList.add(GiftTabModel(Constants.GIFT_TAB_ID_PACKAGE, "背包"))
        val param = DslTabLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT
        )
        param.setMargins(8.dp.toInt(), 0, 8.dp.toInt(), 0)
        tabList.forEachIndexed { _, it ->
            val textView = TextView(requireContext())
            textView.text = it.tabName
            textView.gravity = Gravity.CENTER
            textView.layoutParams = param
            mBinding.tabLayout.addView(textView)
        }

        mBinding.viewPager.apply {
            adapter = object : FragmentStateAdapter(this@GiftDialog) {
                override fun getItemCount() = tabList.size
                override fun createFragment(position: Int) = run {
                    GiftListFragment.newInstance(
                        tabList[position].giftTabId,
                        position,
                        isOpenBox,
                        isDarkMode,
                        position == tabList.size - 1
                    )
                }
            }
        }
        ViewPager2Delegate.install(mBinding.viewPager, mBinding.tabLayout)
        mBinding.tabLayout.configTabLayoutConfig {
            onSelectItemView = { _, index, selected, _ ->
                if (selected) {
                    showCheckAllGiftButton(index)
                    mBinding.tvCheckedAllUser.visibility = View.VISIBLE
                    if (index < mBinding.tabLayout.childCount - 1) {
                        mBinding.recyclerUsers.bindingAdapter.singleMode = false
                    }
                    mViewModel.switchUserList(crId, userId, isOpenBox, from)
                    val fragment =
                        childFragmentManager.findFragmentByTag("f${mBinding.viewPager.currentItem}")
                    if (fragment != null && fragment.isAdded) {
                        (fragment as GiftListFragment).clearChecked()
                        MMKVProvider.lastGiftIds = ""
                    }
                }
                false
            }
        }

        if (isOpenBox) {
            getAppConfigByKey<String>(AppConfigKey.KEY_POINTS_BOX_TAB_ID) {
                var integralBoxTabIndex = 0
                tabList.forEachIndexed { index, model ->
                    if (model.giftTabId == it) {
                        integralBoxTabIndex = index
                    }
                }
                mBinding.tabLayout.setCurrentItem(integralBoxTabIndex)
                mBinding.viewPager.setCurrentItem(integralBoxTabIndex, false)
            }
        } else if (isChooseLucky) {
            getAppConfigByKey<String>(AppConfigKey.KEY_GIFT_LUCKY_TAB_ID) {
                var tabIndex = 0
                tabList.forEachIndexed { index, model ->
                    if (model.giftTabId == it) {
                        tabIndex = index
                    }
                }
                mBinding.tabLayout.setCurrentItem(tabIndex)
                mBinding.viewPager.setCurrentItem(tabIndex, false)
            }
        } else {
            mBinding.tabLayout.setCurrentItem(MMKVProvider.lastGiftTabIndex, false)
            mBinding.viewPager.currentItem = MMKVProvider.lastGiftTabIndex
            if (MMKVProvider.lastGiftTabIndex == mBinding.tabLayout.childCount - 1) {
                showCheckAllGiftButton(MMKVProvider.lastGiftTabIndex)
            }
        }
    }

    private fun getCheckedGift(): List<GiftModel> {
        val fragment = childFragmentManager.findFragmentByTag("f${mBinding.viewPager.currentItem}")
        return (fragment as GiftListFragment).getCheckedGift()
    }

    private fun checkedAllGift(isChecked: Boolean) {
        val fragment = childFragmentManager.findFragmentByTag("f${mBinding.viewPager.currentItem}")
        (fragment as GiftListFragment).checkAllGift(isChecked)
        if (isChecked) {
            mBinding.tvGiftSendCount.text = "全部"
            mBinding.ivChooseCount.visibility = View.GONE
        } else {
            sendCount = 1
            mBinding.tvGiftSendCount.text = "1"
            mBinding.ivChooseCount.visibility = View.VISIBLE
        }
    }

    fun checkAllGift(checked: Boolean) {
        mViewModel.isCheckedAllGift.set(checked)
        sendCount = 1
        mBinding.tvGiftSendCount.text = "1"
        mBinding.ivChooseCount.visibility = View.VISIBLE
    }

    fun onGiftClick(gift: GiftModel) {
        isIntegralBoxChecked = gift.checked && gift.giftOrBox == "002" && gift.boxType == "002"
        mViewModel.switchUserList(crId, userId, isIntegralBoxChecked, from)
        if (gift.checked) {
            MMKVProvider.lastGiftIds = gift.id
            if (gift.info.isNotEmpty()) {
                mBinding.layoutGiftInfo.visibility = View.VISIBLE
                mBinding.tvGiftDetail.visibility =
                    if (gift.introductionUrl.isNullOrEmpty()) View.GONE else View.VISIBLE
                mBinding.tvInfo.text = gift.info
                mBinding.layoutGiftInfo.safeClick {
                    if (gift.introductionUrl.isNotEmpty()) {
                        jump(
                            RouterPath.PATH_WEBVIEW,
                            "url" to gift.introductionUrl,
                            "showTitle" to true
                        )
                    }
                }
            } else {
                mBinding.layoutGiftInfo.visibility = View.INVISIBLE
            }
        } else {
            mBinding.layoutGiftInfo.visibility = View.INVISIBLE
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        MMKVProvider.lastGiftTabIndex = mBinding.viewPager.currentItem
        super.onDismiss(dialog)
    }

    private fun showCheckAllGiftButton(position: Int) {
        if (position < mBinding.tabLayout.childCount - 1) {
            mBinding.tvCheckedAllGift.visibility = View.GONE
        } else {
            request<MutableList<PackGiftModel>>(RoomApi.API_GET_PACK_GIFT_LIST,
                Method.GET,
                onSuccess = {
                    mBinding.tvCheckedAllGift.visibility =
                        if (it.isNullOrEmpty()) View.GONE else View.VISIBLE
                },
                onError = {
                    mBinding.tvCheckedAllGift.visibility = View.GONE
                })
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        //注销注册
        NIMClient.getService(ChatRoomServiceObserver::class.java)
            .observeReceiveMessage(customMessageObservable, false)
    }

}