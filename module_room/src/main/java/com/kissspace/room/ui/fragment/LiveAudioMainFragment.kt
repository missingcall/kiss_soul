package com.kissspace.room.ui.fragment

import android.Manifest
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.blankj.utilcode.util.GsonUtils
import com.didi.drouter.api.DRouter
import com.drake.brv.utils.addModels
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.mutable
import com.google.gson.reflect.TypeToken
import com.kissspace.common.base.BaseActivity
import com.kissspace.common.callback.ActivityTouchEvent
import com.kissspace.common.config.Constants
import com.kissspace.common.ext.safeClick
import com.kissspace.common.ext.scrollToBottom
import com.kissspace.common.ext.showSoftInput
import com.kissspace.common.flowbus.Event
import com.kissspace.common.flowbus.FlowBus
import com.kissspace.common.model.MicUserModel
import com.kissspace.common.model.RoomPKInfoMessage
import com.kissspace.common.model.WaterGamePoolInfo
import com.kissspace.common.model.config.RoomGameConfig
import com.kissspace.common.model.immessage.*
import com.kissspace.common.provider.IMessageProvider
import com.kissspace.common.util.GiftAnimationTaskQueue
import com.kissspace.common.util.customToast
import com.kissspace.common.util.getH5Url
import com.kissspace.common.util.getMP4Path
import com.kissspace.common.util.mmkv.MMKVProvider
import com.kissspace.module_room.R
import com.kissspace.module_room.databinding.RoomFragmentAudioMainBinding
import com.kissspace.network.net.Method
import com.kissspace.network.net.request
import com.kissspace.network.result.collectData
import com.kissspace.room.http.RoomApi
import com.kissspace.room.manager.IMQTTMessageCallBack
import com.kissspace.room.manager.RoomServiceManager
import com.kissspace.room.util.CarAnimationTaskQueue
import com.kissspace.room.util.GiftFlyAnimationUtil
import com.kissspace.room.viewmodel.LiveViewModel
import com.kissspace.room.widget.*
import com.kissspace.util.fromJson
import com.kissspace.util.hideKeyboard
import com.kissspace.util.isClickThisArea
import com.kissspace.util.logE
import com.kissspace.util.postDelay
import com.kissspace.util.runOnUi
import com.kissspace.webview.widget.BrowserDialog
import com.kongzue.dialogx.dialogs.BottomDialog
import com.kongzue.dialogx.dialogs.PopTip
import com.kongzue.dialogx.interfaces.OnBindView
import com.permissionx.guolindev.PermissionX
import com.qmuiteam.qmui.kotlin.onClick
import com.tencent.qgame.animplayer.util.ScaleType
import com.zhpan.bannerview.BannerViewPager


/**
 *
 * @Author: nicko
 * @CreateDate: 2022/12/13 17:54
 * @Description: 九麦房main fragment
 *
 */
class LiveAudioMainFragment : BaseLiveFragment(R.layout.room_fragment_audio_main),
    ActivityTouchEvent, IMQTTMessageCallBack {
    private val mBinding by viewBinding<RoomFragmentAudioMainBinding>()
    private val mViewModel by activityViewModels<LiveViewModel>()
    private var mStartPKInfo: RoomStartPkMessage? = null


    //坐骑进场动效队列
    private lateinit var carAnimationTaskQueue: CarAnimationTaskQueue

    //礼物动效队列
    private lateinit var giftAnimationTaskQueue: GiftAnimationTaskQueue

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        mBinding.vm = mViewModel
        lifecycle.addObserver(mViewModel)
        //初始化礼物动效View
        initAnimView()
        //初始化动效队列
        initAnimationTask()
        //初始化聊天tabLayout
        initChatTabLayout()
        //设置点击事件
        initClickEvents()
        //设置上麦按钮状态
        updateMicStatus()
        //初始化输入监听
        initSoftInput()
        //初始化event监听
        initEventObserver()
        initWaterGame()
        //获取积分任务列表
        mViewModel.getChatRoomTaskRewardList()
        (requireActivity() as BaseActivity).registerActivityTouchEvent(this)
    }

    private fun initAnimView() {
        mBinding.giftView.enableVersion1(true)
        mBinding.animCar.enableVersion1(true)
        mBinding.giftView.setScaleType(ScaleType.CENTER_CROP)
        mBinding.animCar.setScaleType(ScaleType.CENTER_CROP)
    }

    private fun initAnimationTask() {
        carAnimationTaskQueue = CarAnimationTaskQueue(mBinding.animCar, mBinding.userEnterRoomView)
        giftAnimationTaskQueue = GiftAnimationTaskQueue(this, mBinding.giftView)
        viewLifecycleOwner.lifecycle.addObserver(carAnimationTaskQueue)
        viewLifecycleOwner.lifecycle.addObserver(giftAnimationTaskQueue)
    }

    private fun initChatTabLayout() {
        mBinding.tabChat.configTabLayoutConfig {
            onSelectItemView = { _, index, selected, _ ->
                if (selected) {
                    val newMessage = mutableListOf<RoomChatMessageModel>()
                    val newIndex = when (index) {
                        0 -> "001"
                        1 -> "002"
                        else -> "003"
                    }
                    newMessage.addAll(RoomServiceManager.allMessageList)
                    newMessage.removeAll { !it.messageKindList.contains(newIndex) }
                    mBinding.recyclerChat.mutable.clear()
                    if (newMessage.isNullOrEmpty()) {
                        mBinding.recyclerChat.bindingAdapter.notifyDataSetChanged()
                    } else {
                        mBinding.recyclerChat.addModels(newMessage)
                    }
                    mBinding.recyclerChat.scrollToBottom()
                }
                false
            }
        }
    }


    private fun initSoftInput() {
        mBinding.layoutChat.onClick {
            InputTextDialogFragment.Builder().setConfirmCallback {
                mViewModel.checkCanChat(getRoomInfo()!!.crId) {
                    sendTextMessage(
                        getRoomInfo()!!.neteaseChatId,
                        it,
                        getUserInfo()!!,
                    )
                }
            }.showNow(childFragmentManager)
        }
    }

    private fun initClickEvents() {
        //点击送礼
        mBinding.ivGift.safeClick {
            GiftDialog.newInstance(
                getRoomInfo().crId, "", false, Constants.GiftDialogFrom.FROM_PARTY
            ).show(childFragmentManager)
        }
        //点击上麦
        mBinding.tvUpMic.setOnClickListener {
            if (isUserOnMic(MMKVProvider.userId)) {
                if (RoomServiceManager.isPublishStream) {
                    RoomServiceManager.stopPublishStream()
                    updateMicStatus()
                } else {
                    mViewModel.checkCanOpenMic(getRoomInfo()!!.crId)
                }
            } else {
                if (RoomServiceManager.isInMicQueue) {
                    UserMicQueueDialog.newInstance(getRoomInfo()!!.crId).show(childFragmentManager)
                } else {
                    mViewModel.upMic(getRoomInfo()!!.crId, Constants.ROOM_TYPE_PARTY)
                }
            }
        }
        //点击排麦
        mBinding.tvQueue.safeClick {
            AnchorMicQueueDialog.newInstance(getRoomInfo()!!.crId).show(childFragmentManager)
        }
        //点击消息
        mBinding.ivMessage.safeClick {
            val service = DRouter.build(IMessageProvider::class.java).getService()
            service.showChatDialog(childFragmentManager, "")
        }
        //点击更多
        mBinding.ivMore.safeClick {
            showRoomSettingDialog()
        }


//        //点击发送消息
//        mBinding.tvChatSend.safeClick {
//            val content = mBinding.editChat.text.toString().trim()
//            if (content.isEmpty()) {
//                customToast("请输入消息")
//            } else {
//                mViewModel.checkCanChat(getRoomInfo()!!.crId) {
//                    sendTextMessage(
//                        getRoomInfo()!!.neteaseChatId,
//                        content,
//                        getUserInfo()!!,
//                    )
//                    mBinding.editChat.hideKeyboard()
//                    mBinding.layoutChatInput.visibility = View.GONE
//                    mBinding.editChat.setText("")
//                }
//            }
//        }
        //点击新消息
        mBinding.ivNewMessage.setOnClickListener {
            mBinding.ivNewMessage.visibility = View.GONE
            mBinding.recyclerChat.scrollToBottom()
        }

        //点击右上角积分竞猜
        mBinding.predictionView.safeClick {
            RoomPredictionDialog.newInstance(
                getRoomInfo()!!.crId, getRoomInfo()!!.userRole, getRoomInfo()!!.roomTagCategory
            ).show(childFragmentManager)
        }
        //领取积分
        mBinding.clIntegral.safeClick {
            TaskRewardListDialogFragment().show(
                childFragmentManager, "TaskRewardListDialogFragment"
            )
        }
        //发送表情
        mBinding.ivEmoji.setOnClickListener {
            mViewModel.checkCanChat(getRoomInfo()!!.crId) {
                val dialog = RoomEmojiDialog()
                dialog.setCallBack {
                    sendEmojiMessage(
                        getRoomInfo()!!.neteaseChatId,
                        it.name,
                        it.emojiGameIndex,
                        it.isEmojiLoop,
                        it.emojiGameImage,
                        it.dynamicImage,
                        getUserInfo()!!,
                    )
                }
                dialog.show(childFragmentManager)
            }
        }

    }

    private fun initEventObserver() {
        FlowBus.observerEvent<Event.RefreshPoints>(this) {
            //获取积分任务列表
            mViewModel.getChatRoomTaskRewardList()
        }

        FlowBus.observerEvent<Event.UseTaskRewardPointsEvent>(this) {
            RoomPredictionDialog.newInstance(
                getRoomInfo()!!.crId, getRoomInfo()!!.userRole, getRoomInfo()!!.roomTagCategory
            ).show(childFragmentManager)
        }

        FlowBus.observerEvent<Event.OpenBlindBoxEvent>(this) {
            GiftDialog.newInstance(
                getRoomInfo()!!.crId, MMKVProvider.userId, true, Constants.GiftDialogFrom.FROM_PARTY
            ).show(childFragmentManager)
        }

        FlowBus.observerEvent<Event.InsertUserInMessage>(this) {
            if (!it.message.carPath.isNullOrEmpty()) {
                playCarAnimation(it.message)
            }
            insertChatModel(parseUserJoinMessage(it.message))
        }


    }

    private fun initWaterGame() {
        mBinding.ivWaterGame.visibility =
            if (MMKVProvider.isShowGame) View.VISIBLE else View.GONE
        val type = object : TypeToken<List<RoomGameConfig>>() {}.type
        val games = GsonUtils.fromJson<List<RoomGameConfig>>(MMKVProvider.gameConfig, type)
        val waterUrl = games.find { it.game_name == "神奇浇水" }?.game_url
        mBinding.ivWaterGame.safeClick {
            waterUrl?.let {
                val url = "${
                    getH5Url(it, true)
                }&chatRoomId=${getRoomInfo().crId}"
                BrowserDialog.newInstance(url)
                    .show(childFragmentManager, "BrowserDialog")
            }
        }

        request<WaterGamePoolInfo>(RoomApi.API_QUERY_WATER_GAME_POOL, Method.POST, onSuccess = {
            if (it.type == "001") {
                mBinding.ivWaterGame.setImageResource(R.mipmap.room_icon_green_tree)
            } else {
                mBinding.ivWaterGame.setImageResource(R.mipmap.room_icon_gold_tree)
            }
        }, onError = {
            mBinding.ivWaterGame.setImageResource(R.mipmap.room_icon_green_tree)
        })
    }

    override fun getMicrophoneLayout() = mBinding.layoutMicrophone
    override fun getChatRecyclerView() = mBinding.recyclerChat
    override fun getPredictionView() = mBinding.predictionView
    override fun getActivityBannerView(): BannerViewPager<Any> = mBinding.bannerActivity

    override fun getActionBarView() = mBinding.layoutActionBar

    override fun showNewMessageButton(isShow: Boolean) {
        mBinding.ivNewMessage.visibility = if (isShow) View.VISIBLE else View.GONE
    }


    override fun updateMicQueueAmount(amount: Int) {
        mBinding.tvQueueAmount.visibility =
            if (amount > 0 && getRoomInfo()!!.userRole != Constants.ROOM_USER_TYPE_NORMAL) View.VISIBLE else View.GONE
        mBinding.tvQueueAmount.text = amount.toString()
    }


    override fun lockMic(isLock: Boolean, isAll: Boolean, index: Int) {
        if (isLock) {
            mViewModel.lockMic(isAll, index, getRoomInfo()?.crId, Constants.ROOM_TYPE_PARTY)
        } else {
            mViewModel.unLockMic(isAll, index, getRoomInfo()?.crId, Constants.ROOM_TYPE_PARTY)
        }
    }

    override fun updateMicStatus() {
        mBinding.tvUpMic.visibility =
            if (isNormalUser() || isUserOnMic(MMKVProvider.userId)) View.VISIBLE else View.GONE
        mBinding.tvQueue.visibility = if (isNormalUser()) View.GONE else View.VISIBLE
        if (isUserOnMic(MMKVProvider.userId)) {
            if (RoomServiceManager.isPublishStream) {
                mBinding.tvUpMic.setImageResource(R.mipmap.room_icon_mic_talking)
            } else {
                mBinding.tvUpMic.setImageResource(R.mipmap.room_icon_mic_mute)
            }
        } else {
            if (RoomServiceManager.isInMicQueue) {
                mBinding.tvUpMic.setImageResource(R.mipmap.room_icon_waiting_mic)
            } else {
                mBinding.tvUpMic.setImageResource(R.mipmap.room_icon_up_mic)
            }
        }
    }

    override fun kickOut(micModel: MicUserModel) {
        mViewModel.kickOutMic(
            getRoomInfo()!!.crId,
            micModel.wheatPositionId,
            Constants.ROOM_TYPE_PARTY,
            micModel.onMicroPhoneNumber
        )
    }

    override fun playCarAnimation(message: UserEnterMessage) {
        carAnimationTaskQueue.addTask(message)
    }

    override fun playGiftAnimation(url: String) {
        getMP4Path(url) { path ->
            giftAnimationTaskQueue.addTask(path)
        }
    }

    override fun changeTreeState(state: String) {
        mBinding.ivWaterGame.setImageResource(if (state == "001") R.mipmap.room_icon_green_tree else R.mipmap.room_icon_gold_tree)
    }

    override fun updateUnReadCount() {
        mViewModel.updateUnReadCount()
    }

    override fun updateVideoDirection() {

    }

    override fun getChatTabLayout() = mBinding.tabChat

    override fun isOldRoom(): Boolean = (requireParentFragment() as LiveAudioFragment).needRefresh

    override fun playGiftFlyAnimation(message: GiftMessage) {
        val source = getRoomInfo().wheatPositionList.find { t -> t.wheatPositionId == message.sourceUser.userId }
        val startView: View? = if (source != null) {
            val recyclerView = mBinding.layoutMicrophone.getRecyclerView()
            getMicrophoneItemView(recyclerView, source.onMicroPhoneNumber)
        } else {
            mBinding.ivGift
        }
        message.targetUsers.forEach {
            val model = getRoomInfo().wheatPositionList.find { t -> t.wheatPositionId == it.userId }
            if (model != null) {
                val recyclerView = mBinding.layoutMicrophone.getRecyclerView()
                val targetView = if (model != null) {
                    getMicrophoneItemView(recyclerView, model.onMicroPhoneNumber)
                } else {
                    mBinding.giftFlyEnd
                }
                if (isUserOnMic(source?.wheatPositionId) || isUserOnMic(model?.wheatPositionId)) {
                    GiftFlyAnimationUtil.playAnimation(
                        requireContext(),
                        startView!!,
                        targetView!!,
                        mBinding.root,
                        message.targetUsers[0].giftInfos[0].url,
                        1200
                    )
                }
            }

        }
    }


    private fun getMicrophoneItemView(recyclerView: RecyclerView, micPosition: Int): View? {
        val layoutManager = recyclerView.layoutManager
        return layoutManager?.findViewByPosition(micPosition)?.findViewById(R.id.iv_user_avatar)
    }


    override fun upMic(index: Int) {
        mViewModel.upMic(getRoomInfo()!!.crId, Constants.ROOM_TYPE_PARTY, index)
    }

    override fun quitMic(index: Int) {
        RoomServiceManager.stopPublishStream()
        mViewModel.quitMic(getRoomInfo()!!.crId, index, Constants.ROOM_TYPE_PARTY)
    }

    override fun createDataObserver() {
        super.createDataObserver()

        //监听上麦
        collectData(mViewModel.upMicEvent, onSuccess = {
            if (it.status != "002") {
                UserMicQueueDialog.newInstance(getRoomInfo()!!.crId).show(childFragmentManager)
            }
        }, onError = {
            if (it.errCode == "50038") {
                //显示排麦弹窗
                UserMicQueueDialog.newInstance(getRoomInfo()!!.crId).show(childFragmentManager)
            } else {
                customToast(it.message)
            }
        })

        collectData(mViewModel.quitMicEvent, onSuccess = {
            //下麦成功
            getRoomInfo().wheatPositionList.forEach {
                if (it.wheatPositionId == MMKVProvider.userId) {
                    clearMicInfo(it)
                }
            }
        }, onError = {
            customToast(it.message)
        })

        collectData(mViewModel.checkCanOpenMicEvent, onSuccess = {
            if (!it.isForbiddenMike) {
                PermissionX.init(this).permissions(Manifest.permission.RECORD_AUDIO)
                    .onExplainRequestReason { scope, deniedList ->
                        val message =
                            "为了您能正常体验【房间语音聊天】功能，kiss空间需向你申请麦克风权限"
                        scope.showRequestReasonDialog(deniedList, message, "确定", "取消")
                    }.explainReasonBeforeRequest()

                    .request { allGranted, _, _ ->
                        if (allGranted) {
                            RoomServiceManager.startPublishStream(
                                getUserInfo()?.userId.orEmpty(), false
                            )
                            updateMicStatus()
                        } else {
                            customToast("请打开麦克风权限")
                        }
                    }
            } else {
                customToast("您已被禁麦")
            }
        })
        //获取积分列表弹窗
        collectData(mViewModel.taskRewardListEvent, onSuccess = {
            it.filter { item1 -> item1.finishStatus == Constants.TaskStatus.TOBE_COLLECTED.type }
                .let { list ->
                    logE("tvIntegralCount$list.size")
                    if (list.isNotEmpty()) {
                        mBinding.tvIntegralCount.text = list.size.toString()
                        mBinding.clIntegral.visibility = View.VISIBLE
                    } else {
                        //隐藏领取积分接口
                        mBinding.clIntegral.visibility = View.GONE
                    }
                }

        })
    }


    override fun getUserInfo() = (requireParentFragment() as LiveAudioFragment).mUserInfo

    override fun onResume() {
        super.onResume()
        if (!MMKVProvider.isShowRoomGuide) {
            RoomGuideDialogFragment().show(childFragmentManager)
            MMKVProvider.isShowRoomGuide = true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        (requireActivity() as BaseActivity).unRegisterActivityTouchEvent(this)
    }

    override fun onTouchEvent(event: MotionEvent) {

    }

    override fun onDispatchTouchEvent(ev: MotionEvent) {

//        if (isClickThisArea(mBinding.recyclerChat, ev)) {
//            (requireParentFragment().requireActivity().window.decorView as ViewGroup).requestDisallowInterceptTouchEvent(
//                true
//            )
//            logE("我要拦截")
//        } else {
//            (requireParentFragment().requireActivity().window.decorView as ViewGroup).requestDisallowInterceptTouchEvent(
//                true
//            )
//            logE("我不拦截")
//        }
    }

    override fun onMQTTMessageArrived(topic: String, msg: String) {
        if (topic == mStartPKInfo?.topic) {
            val data = fromJson<RoomPKInfoMessage>(msg)
            runOnUi {
                //刷新PK值
                getRoomInfo().wheatPositionList.forEachIndexed { index, micUserModel ->
                    if (index > 0) {
                        micUserModel.pkValue =
                            data.microphonePkValueDtoList[index].microphonePkValue
                        micUserModel.notifyChange()
                    }
                }
                mBinding.pkInfoView.updatePKInfo(data)
            }
        }


    }


}
