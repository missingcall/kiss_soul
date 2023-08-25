package com.kissspace.message.widget

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ToastUtils
import com.didi.drouter.api.DRouter
import com.drake.brv.utils.grid
import com.drake.brv.utils.setup
import com.kissspace.util.hideKeyboard
import com.netease.nimlib.sdk.media.record.AudioRecorder
import com.netease.nimlib.sdk.media.record.IAudioRecordCallback
import com.netease.nimlib.sdk.media.record.RecordType
import com.permissionx.guolindev.PermissionX
import com.kissspace.common.config.Constants
import com.kissspace.common.ext.showSoftInput
import com.kissspace.common.model.ChatEmojiListBean
import com.kissspace.common.provider.IRoomProvider
import com.kissspace.common.util.countDown
import com.kissspace.common.util.openCamera
import com.kissspace.common.util.openPictureSelector
import com.kissspace.module_message.R
import kotlinx.coroutines.Job
import java.io.File
import kotlin.math.abs

/**
 *
 * @Author: nicko
 * @CreateDate: 2023/1/9 16:15
 * @Description: 聊天面板
 *
 */
class ChatPanelView : FrameLayout, IAudioRecordCallback {
    private val mSwitchAudioButton: ImageView
    private val mCameraButton: ImageView
    private val mPictureButton: ImageView
    private val mGiftButton: ImageView
    private val mSendButton: TextView
    private val mChatEdit: EditText
    private val mChatEmojiButton: ImageView
    private val mTalkEmojiButton: ImageView
    private val mTalkButton: TextView
    private val mEmojiRecyclerView: RecyclerView
    private val mEmojiLayout: FrameLayout
    private var isShowTalk = false
    private var isShowEmoji = false
    private lateinit var onChatPanelCallBack: OnChatPanelCallBack
    private lateinit var fragmeent: Fragment
    private lateinit var userId: String
    private var audioRecorder: AudioRecorder

    //是否取消发送
    private var isCancelAudio = false
    private var countDownJob: Job? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet?) : super(context, attributeSet)

    init {
        View.inflate(context, R.layout.message_layout_chat_panel, this)
        audioRecorder = AudioRecorder(context, RecordType.AAC, 60, this)
        mSwitchAudioButton = findViewById(R.id.iv_chat_talking)
        mCameraButton = findViewById(R.id.iv_chat_camera)
        mPictureButton = findViewById(R.id.iv_chat_picture)
        mGiftButton = findViewById(R.id.iv_send_gift)
        mSendButton = findViewById(R.id.tv_send_chat)
        mChatEdit = findViewById(R.id.edit_chat)
        mChatEmojiButton = findViewById(R.id.iv_emoji_chat)
        mTalkEmojiButton = findViewById(R.id.iv_emoji_talking)
        mTalkButton = findViewById(R.id.tv_talking)
        mEmojiRecyclerView = findViewById(R.id.recycler_view_emoji)
        mEmojiLayout = findViewById(R.id.flt_emoji)
        initEvent()
    }

    fun setOnChatPanelCallBack(fragment: Fragment, userId: String, callBack: OnChatPanelCallBack) {
        this.fragmeent = fragment
        this.userId = userId
        this.onChatPanelCallBack = callBack
    }

    fun setEmojiData(list: List<ChatEmojiListBean>) {
        mEmojiRecyclerView.grid(5).setup {
            addType<ChatEmojiListBean> { R.layout.message_layout_emoji_item }
            onFastClick(R.id.root) {
                val model = getModel<ChatEmojiListBean>()
                resetEmoji()
                hideEmojiLayout()
                onChatPanelCallBack.onClickEmoji(model)
            }
        }.models = list
    }

    private fun initEvent() {
        mSwitchAudioButton.setOnClickListener {
            if (isShowTalk) {
                mSwitchAudioButton.setImageResource(R.mipmap.message_icon_chat_type)
                mTalkButton.visibility = View.GONE
                mTalkEmojiButton.visibility = View.GONE
                mChatEdit.visibility = View.VISIBLE
                mChatEmojiButton.visibility = View.VISIBLE
                mSendButton.visibility = View.VISIBLE
                isShowTalk = !isShowTalk
            } else {
                PermissionX.init(fragmeent).permissions(Manifest.permission.RECORD_AUDIO)
                    .onExplainRequestReason { scope, deniedList ->
                        val message =
                            "为了您能正常体验【发送语音消息】功能，kiss空间需向你申请麦克风权限"
                        scope.showRequestReasonDialog(deniedList, message, "确定", "取消")
                    }
                    .explainReasonBeforeRequest()
                    .request { allGranted, _, _ ->
                        if (allGranted) {
                            mSwitchAudioButton.setImageResource(R.mipmap.message_icon_chat_talking)
                            mTalkButton.visibility = View.VISIBLE
                            mTalkEmojiButton.visibility = View.VISIBLE
                            mChatEdit.visibility = View.GONE
                            mChatEmojiButton.visibility = View.GONE
                            mSendButton.visibility = View.GONE
                            isShowTalk = !isShowTalk
                            hideKeyboard()
                        } else {
                            ToastUtils.showShort("请打开麦克风权限")
                        }
                    }
            }
        }

        mSendButton.setOnClickListener {
            val text = mChatEdit.text.toString().trim()
            if (text.isEmpty()) {
                ToastUtils.showShort("请输入聊天内容")
            } else {
                mChatEdit.setText("")
                onChatPanelCallBack.onSendTextMessage(text)
            }
        }

       mChatEdit.setOnKeyListener(View.OnKeyListener { _, keyCode, keyEvent ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && keyEvent.action == KeyEvent.ACTION_UP) {
                mSendButton.performClick()
                return@OnKeyListener true
            }
            false
        })

        mChatEdit.addTextChangedListener {
            if (it.toString().isNotEmpty()) {
                mSendButton.setBackgroundResource(R.mipmap.message_bg_chat_send_btn_enable_bg)
            } else {
                mSendButton.setBackgroundResource(R.mipmap.message_bg_chat_send_btn_disable_bg)
            }
        }

        mPictureButton.setOnClickListener {
           openPictureSelector(context,1) {
                it?.forEach { path ->
                    onChatPanelCallBack.onSendImageMessage(path)
                }
            }
        }

        mCameraButton.setOnClickListener {
            openCamera { path ->
                path?.let {
                    onChatPanelCallBack.onSendImageMessage(it)
                }
            }
        }

        mChatEmojiButton.setOnClickListener {
            if (isShowEmoji) {
                mChatEmojiButton.setImageResource(R.mipmap.message_icon_chat_emoji)
                mEmojiLayout.visibility = View.GONE
                mChatEdit.postDelayed({
                    mChatEdit.showSoftInput()
                }, 100)
            } else {
                mChatEmojiButton.setImageResource(R.mipmap.message_icon_chat_type)
                mChatEdit.hideKeyboard()
                mEmojiLayout.postDelayed({
                    mEmojiLayout.visibility = View.VISIBLE
                }, 100)
            }
            isShowEmoji = !isShowEmoji
        }

        mTalkEmojiButton.setOnClickListener {
            if (isShowEmoji) {
                mTalkEmojiButton.setImageResource(R.mipmap.message_icon_chat_emoji)
                mEmojiLayout.visibility = View.GONE
                mChatEdit.postDelayed({
                    mChatEdit.showSoftInput()
                }, 100)
            } else {
                mTalkEmojiButton.setImageResource(R.mipmap.message_icon_chat_type)
                mChatEdit.hideKeyboard()
                mEmojiLayout.postDelayed({
                    mEmojiLayout.visibility = View.VISIBLE
                }, 100)
            }
            isShowEmoji = !isShowEmoji
        }

        mGiftButton.setOnClickListener {
            hideKeyboard()
            val service = DRouter.build(IRoomProvider::class.java).getService()
            service.showGiftDialog(
                fragmeent.childFragmentManager,
                crId = "",
                targetUserId = userId,
                isOpenPointsBox = false,
                from = Constants.GiftDialogFrom.FROM_CHAT
            )
        }
        handleAudio()
    }

    fun hideEmojiLayout() {
        mEmojiLayout.visibility = View.GONE
    }

    fun resetEmoji() {
        isShowEmoji = false
        mTalkEmojiButton.setImageResource(R.mipmap.message_icon_chat_emoji)
        mChatEmojiButton.setImageResource(R.mipmap.message_icon_chat_emoji)
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun handleAudio() {
        var startTouchY = 0f
        var startRecordTime = 0L
        var isStartCountDown = false
        mTalkButton.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                isCancelAudio = false
                startTouchY = event.y
                startRecordTime = System.currentTimeMillis()
                audioRecorder.startRecord()
                onChatPanelCallBack.onShowStartTalk(isStartCountDown)
                countDownJob?.cancel()
                countDownJob = countDown(59, scope = fragmeent.lifecycleScope, onTick = {
                    if (it <= 10) {
                        isStartCountDown = true
                        onChatPanelCallBack.onRecordCountDown(it)
                    }
                }, onFinish = {
                    isStartCountDown = false
                    audioRecorder.completeRecord(false)
                    onChatPanelCallBack?.dismissAudioToast()
                })
            }
            if (event.action == MotionEvent.ACTION_UP) {
                if (System.currentTimeMillis() - startRecordTime < 1000) {
                    isCancelAudio = true
                    audioRecorder.completeRecord(true)
                    onChatPanelCallBack.onTalkTooShort()
                } else {
                    countDownJob?.cancel()
                    audioRecorder.completeRecord(isCancelAudio)
                    startRecordTime = 0
                    startTouchY = 0F
                    onChatPanelCallBack.dismissAudioToast()
                }

            }
            if (event.action == MotionEvent.ACTION_MOVE) {
                if (abs(event.y - startTouchY) > 300) {
                    isCancelAudio = true
                    onChatPanelCallBack.onShowCancelTalk()
                } else {
                    isCancelAudio = false
                    onChatPanelCallBack.onShowStartTalk(isStartCountDown)
                }
            }
            true
        }
    }

    override fun onRecordReady() {
    }

    override fun onRecordStart(audioFile: File?, recordType: RecordType?) {
    }

    override fun onRecordSuccess(audioFile: File?, audioLength: Long, recordType: RecordType?) {
        countDownJob?.cancel()
        if (isCancelAudio) {
            return
        }
        onChatPanelCallBack.onSendAudioMessage(audioFile, audioLength)
    }

    override fun onRecordFail() {
    }

    override fun onRecordCancel() {

    }

    override fun onRecordReachedMaxTime(maxTime: Int) {

    }

    fun stopRecord() {
        if (audioRecorder.isRecording) {
            audioRecorder.completeRecord(true)
        }
    }

}