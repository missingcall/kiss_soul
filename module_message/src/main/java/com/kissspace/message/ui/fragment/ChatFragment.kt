package com.kissspace.message.ui.fragment

import android.media.AudioManager
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import cc.shinichi.library.ImagePreview
import com.blankj.utilcode.constant.TimeConstants
import com.blankj.utilcode.util.TimeUtils
import com.didi.drouter.api.DRouter
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.linear
import com.drake.brv.utils.mutable
import com.drake.brv.utils.setup
import com.drake.softinput.hasSoftInput
import com.drake.softinput.setWindowSoftInput
import com.hjq.bar.OnTitleBarListener
import com.hjq.bar.TitleBar
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.Observer
import com.netease.nimlib.sdk.media.player.AudioPlayer
import com.netease.nimlib.sdk.media.player.OnPlayListener
import com.netease.nimlib.sdk.msg.MsgService
import com.netease.nimlib.sdk.msg.MsgServiceObserve
import com.netease.nimlib.sdk.msg.constant.AttachStatusEnum
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.msg.model.IMMessage
import com.qmuiteam.qmui.widget.QMUIRadiusImageView2
import com.kissspace.common.base.BaseActivity
import com.kissspace.common.base.BaseFragment
import com.kissspace.common.callback.ActivityTouchEvent
import com.kissspace.common.config.Constants
import com.kissspace.common.router.jump
import com.kissspace.common.ext.safeClick
import com.kissspace.common.ext.scrollToBottom
import com.kissspace.common.flowbus.Event
import com.kissspace.common.flowbus.FlowBus
import com.kissspace.common.model.ChatEmojiListBean
import com.kissspace.common.model.ChatModel
import com.kissspace.common.provider.IRoomProvider
import com.kissspace.common.router.RouterPath
import com.kissspace.common.util.EmojiGameUtil
import com.kissspace.common.util.GiftAnimationTaskQueue
import com.kissspace.common.util.mmkv.MMKVProvider
import com.kissspace.common.util.getMutable
import com.kissspace.common.util.jumpRoom
import com.kissspace.common.util.customToast
import com.kissspace.common.util.getMP4Path
import com.kissspace.common.widget.CommonConfirmDialog
import com.kissspace.message.viewmodel.ChatViewModel
import com.kissspace.message.widget.ChatDialog
import com.kissspace.message.widget.FriendShipDialog
import com.kissspace.message.widget.OnChatPanelCallBack
import com.kissspace.module_message.R
import com.kissspace.module_message.databinding.MessageFragmentChatBinding
import com.kissspace.network.result.collectData
import com.kissspace.util.hideKeyboard
import com.kissspace.util.isClickThisArea
import com.kissspace.util.orZero
import com.kissspace.util.toast
import org.json.JSONObject
import java.io.File
import kotlin.math.abs


/**
 *
 * @Author: nicko
 * @CreateDate: 2023/1/5 17:18
 * @Description: 私聊fragment
 *
 */
class ChatFragment : BaseFragment(R.layout.message_fragment_chat), ActivityTouchEvent {
    private val mBinding by viewBinding<MessageFragmentChatBinding>()
    private val mViewModel by viewModels<ChatViewModel>()
    private lateinit var friendAccount: String
    private lateinit var friendUserId: String
    private var isFromDialog = false
    private lateinit var audioPlayer: AudioPlayer
    private var clickItemPosition = 0
    private lateinit var giftAnimationQueue: GiftAnimationTaskQueue
    private val imageList = mutableListOf<String>()

    private var isFirstLoadMessage = true

    //处理文本消息接收
    private val receiveMessageObserver = Observer<List<IMMessage>> {
        if (!isAdded) {
            return@Observer
        }
        it?.forEach { message ->
            if (message.sessionId == friendAccount) {
                if (message.msgType == MsgTypeEnum.text) {
                    val textMessage = mViewModel.parseTextMessage(message)
                    insertMessage(textMessage)
                }
                if (message.msgType == MsgTypeEnum.custom) {
                    val json = JSONObject(message.attachStr)
                    if (json.getString("type") == Constants.IMMessageType.MSG_CHAT_SEND_GIFT) {
                        val chatModel = mViewModel.parseGiftMessage(message)
                        chatModel.giftSvga?.let { t ->
                            getMP4Path(t) { url ->
                                giftAnimationQueue.addTask(url)
                            }
                        }
                        insertMessage(chatModel)
                    }
                    if (json.getString("type") == Constants.IMMessageType.MSG_CHAT_SEND_EMOJI) {
                        insertMessage(mViewModel.parseEmojiMessage(message))
                    }
                }
            }

        }
    }


    //处理图片与语音消息接收，监听附件下载成功后再插入消息
    private val messageStatusObserver = Observer<IMMessage> {
        if (!isAdded) {
            return@Observer
        }
        if (it.sessionId != friendAccount && it.sessionId != mViewModel.mineUserInfo.get()?.accId) {
            return@Observer
        }
        if (it.yidunAntiSpamRes != null) {
            customToast("发送内容包含违禁内容，请重新发送")
            NIMClient.getService(MsgService::class.java).deleteChattingHistory(it)
            return@Observer
        }
        when (it.msgType) {
            MsgTypeEnum.image -> {
                if (it.attachStatus == AttachStatusEnum.transferred && it.status == MsgStatusEnum.success) {
                    val model = mViewModel.parseImageMessage(it)
                    insertMessage(model)
                    if (it.fromAccount == MMKVProvider.accId) {
                        mViewModel.sendChatTask()
                    }
                }
            }

            MsgTypeEnum.audio -> {
                if (it.attachStatus == AttachStatusEnum.transferred && it.status == MsgStatusEnum.success) {
                    val model = mViewModel.parseAudioMessage(it)
                    if (model != null) {
                        insertMessage(model)
                    }
                    if (it.fromAccount == MMKVProvider.accId) {
                        mViewModel.sendChatTask()
                    }
                }
            }

            MsgTypeEnum.text -> {
                if (it.yidunAntiSpamRes == null && it.status == MsgStatusEnum.success) {
                    val model = mViewModel.parseTextMessage(it)
                    insertMessage(model)
                    if (it.fromAccount == MMKVProvider.accId) {
                        mViewModel.sendChatTask()
                    }
                }
            }

            else -> {}
        }
    }

    companion object {
        fun newInstance(friendAccount: String, friendUserId: String, isFromDialog: Boolean) =
            ChatFragment().apply {
                arguments = bundleOf(
                    "friendAccount" to friendAccount,
                    "friendUserId" to friendUserId,
                    "isFromDialog" to isFromDialog
                )
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        friendAccount = arguments?.getString("friendAccount")!!
        friendUserId = arguments?.getString("friendUserId")!!
        isFromDialog = arguments?.getBoolean("isFromDialog")!!
    }

    override fun initView(savedInstanceState: Bundle?) {
        mBinding.vm = mViewModel
        mViewModel.requestUserInfo(friendAccount, friendUserId)
        mViewModel.requestEmojiList()
        giftAnimationQueue = GiftAnimationTaskQueue(this, mBinding.giftView)
        lifecycle.addObserver(giftAnimationQueue)
        initTitleBar()
        initRecyclerView()
        registerObserver()
        initRefreshLayout()
        initClickEvents()
        initSoftInput()
        initAudio()
        (requireActivity() as com.kissspace.common.base.BaseActivity).registerActivityTouchEvent(
            this
        )
    }

    private fun initTitleBar() {
//        if (isFromDialog) {
//            mBinding.root.setBackgroundResource(R.drawable.message_bg_corner)
//            mBinding.titleBar.setBackgroundResource(R.drawable.message_bg_corner)
//        } else {
//            mBinding.root.setBackgroundResource(R.drawable.message_bg_normal)
//            mBinding.titleBar.setBackgroundResource(R.drawable.message_bg_normal)
//        }
        mBinding.titleBar.setOnTitleBarListener(object : OnTitleBarListener {
            override fun onLeftClick(titleBar: TitleBar?) {
                stopAudio()
                if (isFromDialog) {
                    (parentFragment as ChatDialog).addFragment(MessageFragment())
                } else {
                    activity?.finish()
                }
            }
        })

        mBinding.ivMenu.setOnClickListener {
            val blackText =
                if (mViewModel.friendUserInfo.get()?.pulledBlack == true) "取消拉黑" else "拉黑用户"
            FriendShipDialog(blackText) {
                if (it == 0) {
                    if (mViewModel.friendUserInfo.get()?.pulledBlack == true) {
                        mViewModel.banUser()
                    } else {
                        CommonConfirmDialog(requireContext(), "拉黑后将无法接收对方消息") {
                            if (this) mViewModel.banUser()
                        }.show()
                    }
                } else {
                    jump(
                        RouterPath.PATH_REPORT,
                        "reportType" to Constants.ReportType.USER.type,
                        "userId" to mViewModel.friendUserInfo.get()?.userId.orEmpty()
                    )
                }

            }.show(parentFragmentManager)
        }

    }

    private fun registerObserver() {
        NIMClient.getService(MsgServiceObserve::class.java)
            .observeReceiveMessage(receiveMessageObserver, true)
        NIMClient.getService(MsgServiceObserve::class.java)
            .observeMsgStatus(messageStatusObserver, true)

    }


    private fun initAudio() {
        audioPlayer = AudioPlayer(requireContext())
        audioPlayer.onPlayListener = object : OnPlayListener {
            override fun onPrepared() {
            }

            override fun onCompletion() {
                val data =
                    mBinding.recyclerView.bindingAdapter.mutable[clickItemPosition] as ChatModel
                data.isPlayAudioAnimation = false
                data.notifyChange()
            }

            override fun onInterrupt() {
            }

            override fun onError(error: String?) {
            }

            override fun onPlaying(curPosition: Long) {
            }

        }
    }

    private fun initRecyclerView() {
        //查询历史消息
        mBinding.recyclerView.linear().setup {
            addType<ChatModel> {
                when (messageType) {
                    Constants.ChatMessageType.TYPE_CONTENT -> {
                        if (this.isReceive) R.layout.message_layout_chat_item_content_left else R.layout.message_layout_chat_item_content_right
                    }

                    Constants.ChatMessageType.TYPE_PICTURE -> {
                        if (this.isReceive) R.layout.message_layout_chat_item_picture_left else R.layout.message_layout_chat_item_picture_right
                    }

                    Constants.ChatMessageType.TYPE_AUDIO -> {
                        if (this.isReceive) R.layout.message_layout_chat_item_audio_left else R.layout.message_layout_chat_item_audio_right
                    }

                    Constants.ChatMessageType.TYPE_GIFT -> {
                        if (this.isReceive) R.layout.message_layout_chat_item_gift_left else R.layout.message_layout_chat_item_gift_right
                    }

                    Constants.ChatMessageType.TYPE_EMOJI -> {
                        if (this.isReceive) R.layout.message_layout_chat_item_emoji_left else R.layout.message_layout_chat_item_emoji_right
                    }

                }
            }
            onFastClick(R.id.layout_audio_right, R.id.layout_audio_left) {
                mutable.forEach {
                    (it as ChatModel).isPlayAudioAnimation = false
                    it.notifyChange()
                }
                val model = getModel<ChatModel>()
                if (audioPlayer.isPlaying && clickItemPosition == modelPosition) {
                    audioPlayer.stop()
                } else {
                    audioPlayer.stop()
                    audioPlayer.setDataSource(model.audioFile?.absolutePath)
                    audioPlayer.start(AudioManager.STREAM_MUSIC)
                    model.isPlayAudioAnimation = true
                    model.notifyChange()
                }
                clickItemPosition = modelPosition
                model.isAudioPlayed = true
                model.notifyChange()
                val im = model.imMessage
                val remote = mutableMapOf<String, Any?>()
                remote["isAudioPlayed"] = true
                im.localExtension = remote
                NIMClient.getService(MsgService::class.java).updateIMMessage(im)
                hideKeyboard()
            }
            onFastClick(R.id.iv_avatar_left) {
                hideKeyboard()
                jump(RouterPath.PATH_USER_PROFILE, "userId" to friendUserId)
            }
            onFastClick(R.id.iv_avatar_right) {
                hideKeyboard()
                jump(RouterPath.PATH_USER_PROFILE, "userId" to MMKVProvider.userId)
            }
            onFastClick(R.id.iv_picture) {
                val model = getModel<ChatModel>()
                val target = findView<QMUIRadiusImageView2>(R.id.iv_picture)
                previewPicture(imageList.indexOf(model.picture!!), target, imageList)
                hideKeyboard()
            }
        }.models = mutableListOf()
    }

    private fun initRefreshLayout() {
        mBinding.smartRefreshLayout.apply {
            setEnableLoadMore(false)
            setOnRefreshListener {
                isFirstLoadMessage = false
                mViewModel.queryMessage(friendAccount, isFirstLoadMessage)
            }
        }
    }

    private fun checkChatPermission(): Boolean {
        return if (mBinding.recyclerView.mutable.isEmpty()) {
            val user = mViewModel.mineUserInfo.get()
            val levelEnable = user?.consumeLevel.orZero() >= MMKVProvider.userChatMinLevel
            if (user?.family == true) {
                return true
            } else {
                if (levelEnable) {
                    return true
                } else {
                    customToast("您暂时无法主动发送消息")
                    return false
                }
            }
        } else true
    }

    private fun initClickEvents() {
        mBinding.chatPanel.setOnChatPanelCallBack(this, friendUserId, object : OnChatPanelCallBack {
            override fun onSendTextMessage(text: String) {
                if (!checkChatPermission()) {
                    return
                }
                mViewModel.sendTextMessage(
                    friendAccount, text, isShowTime()
                ) {

                }

            }

            override fun onSendImageMessage(path: String) {
                if (!checkChatPermission()) {
                    return
                }
                mViewModel.sendPictureMessage(friendAccount, File(path), isShowTime())
            }

            override fun onSendAudioMessage(file: File?, length: Long) {
                if (!checkChatPermission()) {
                    return
                }
                mViewModel.sendAudioMessage(friendAccount, file!!, length, isShowTime())
            }

            override fun onRecordCountDown(count: Long) {
                mBinding.toastView.showCountDown(count)
            }

            override fun onTalkTooShort() {
                mBinding.toastView.visibility = View.VISIBLE
                mBinding.toastView.talkTooShort()
            }

            override fun onShowCancelTalk() {
                mBinding.toastView.cancelRecord()
            }

            override fun onShowStartTalk(isCountDown: Boolean) {
                mBinding.toastView.startRecord(isCountDown)
            }

            override fun dismissAudioToast() {
                mBinding.toastView.visibility = View.GONE
            }

            override fun onClickEmoji(emojiListBean: ChatEmojiListBean) {
                if (!checkChatPermission()) {
                    return
                }
                if (EmojiGameUtil.isEmojiGame(emojiListBean.name)) {
                    val emojiIndex = EmojiGameUtil.getEmojiIndex(emojiListBean.name)
                    emojiListBean.dynamicImage =
                        EmojiGameUtil.getEmojiPath(emojiListBean.name, emojiIndex)
                    emojiListBean.emojiGameIndex = emojiIndex
                    emojiListBean.isEmojiLoop = false
                }
                mViewModel.sendEmojiMessage(friendAccount, emojiListBean, isShowTime()) {
                    mViewModel.sendChatTask()
                    insertMessage(it)
                }
            }


        })

        mBinding.layoutFollowRoom.safeClick {
            var crId = mViewModel.friendUserInfo.get()?.followRoomId
            val service = DRouter.build(IRoomProvider::class.java).getService()
            if (service.getRoomId() != crId || !isFromDialog) {
                jumpRoom(crId = crId)
            } else {
                customToast("您已在当前房间")
            }
        }
    }

    private fun initSoftInput() {
        if (isFromDialog) {
//            (parentFragment as DialogFragment).setWindowSoftInput(
//                float = mBinding.chatPanel,
//                transition = mBinding.chatPanel,
//                setPadding = true
//            ) {
//                if (hasSoftInput()) {
//                    mBinding.recyclerView.scrollToBottom()
//                    mBinding.chatPanel.hideEmojiLayout()
//                }
//            }
            return
        }
        setWindowSoftInput(
            float = mBinding.chatPanel,
            transition = mBinding.chatPanel,
            setPadding = true
        ) {
            if (hasSoftInput()) {
                mBinding.recyclerView.scrollToBottom()
                mBinding.chatPanel.hideEmojiLayout()
            }
        }
    }


    override fun createDataObserver() {
        super.createDataObserver()

        collectData(mViewModel.getEmojiListEvent, onSuccess = { listBeans ->
            var list = ArrayList<ChatEmojiListBean>()
            listBeans.forEach {
                if (!EmojiGameUtil.isEmojiGame(it.name)) {
                    list.add(it)
                }
            }
            mBinding.chatPanel.setEmojiData(list)
        })

        collectData(mViewModel.banUserEvent, onSuccess = {
            NIMClient.getService(MsgService::class.java)
                .deleteRecentContact2(it, SessionTypeEnum.P2P)
            NIMClient.getService(MsgService::class.java)
                .clearChattingHistory(it, SessionTypeEnum.P2P, true)
            if (isFromDialog) {
                (parentFragment as ChatDialog).addFragment(MessageFragment())
            } else {
                activity?.finish()
            }
        })

        lifecycleScope.launchWhenResumed {
            mViewModel.getChatListEvent.collect {
                if (it.size < 20) {
                    mBinding.smartRefreshLayout.setEnableRefresh(false)
                }
                mBinding.smartRefreshLayout.finishRefresh()
                mBinding.recyclerView.bindingAdapter.addModels(it, index = 0)
                it.forEach { that ->
                    if (that.picture != null) {
                        imageList.add(that.picture!!)
                    }
                }
                if (isFirstLoadMessage) {
                    mBinding.recyclerView.scrollToBottom()
                }
            }
        }

        FlowBus.observerEvent<Event.SendGiftEvent>(this) {
            it.data.targetUsers.forEach { target ->
                target.giftInfos.forEach { info ->
                    getMP4Path(info.svg) { path ->
                        giftAnimationQueue.addTask(path)
                    }
                    mViewModel.sendGiftMessage(
                        friendAccount,
                        info.giftName,
                        info.total,
                        info.url,
                        info.svg,
                        info.price,
                        info.giftId,
                        isShowTime()
                    ) { model ->
                        insertMessage(model)
                    }
                }
            }
        }
    }


    private fun insertMessage(message: ChatModel) {
        message.picture?.let {
            imageList.add(message.picture!!)
        }
        mBinding.recyclerView.bindingAdapter.addModels(mutableListOf(message))
        mBinding.recyclerView.scrollToBottom()
        NIMClient.getService(MsgService::class.java)
            .clearUnreadCount(mViewModel.friendUserInfo.get()?.accid, SessionTypeEnum.P2P)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAudio()
        (requireActivity() as com.kissspace.common.base.BaseActivity).unRegisterActivityTouchEvent(
            this
        )
        NIMClient.getService(MsgServiceObserve::class.java)
            .observeReceiveMessage(receiveMessageObserver, false)
        NIMClient.getService(MsgServiceObserve::class.java)
            .observeMsgStatus(messageStatusObserver, false)
    }

    private fun stopAudio() {
        if (audioPlayer.isPlaying) {
            audioPlayer.stop()
        }
        if (isAdded) {
            mBinding.chatPanel.stopRecord()
        }
    }


    private fun isShowTime(): Int {
        val data = mBinding.recyclerView.getMutable<ChatModel>()
        return if (data.isNullOrEmpty()) {
            0
        } else {
            val last = data.last()
            val distance = TimeUtils.getTimeSpanByNow(last.timestamp, TimeConstants.SEC)
            if (abs(distance) > 5 * 60) 1 else 0
        }
    }

    override fun onTouchEvent(event: MotionEvent) {
    }

    override fun onDispatchTouchEvent(ev: MotionEvent) {
        if (ev.action == MotionEvent.ACTION_DOWN && !isClickThisArea(
                mBinding.chatPanel, ev
            )
        ) {
            hideKeyboard()
        }
    }

    private fun previewPicture(
        modelPosition: Int,
        target: QMUIRadiusImageView2,
        imageList: MutableList<String>
    ) {
        ImagePreview.instance
            .setContext(requireActivity())
            .setIndex(modelPosition)
            .setImageList(imageList)
            .setTransitionView(target)
            .setTransitionShareElementName("shared_element_container")
            .setEnableUpDragClose(true)
            .setEnableDragClose(true)
            .setEnableDragCloseIgnoreScale(true)
            .setShowDownButton(false)
            .setShowIndicator(false)
            .start()
    }

}
