package com.kissspace.message.widget

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.ToastUtils
import com.kissspace.util.withStyledAttributes
import com.kissspace.common.util.countDown
import com.kissspace.module_message.R
import kotlinx.coroutines.MainScope

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/12/26 15:41
 * @Description: 录音提示ui
 *
 */
class AudioInputToast : FrameLayout {
    private lateinit var icon: ImageView
    private lateinit var tips: TextView
    private lateinit var countdown: TextView
    private var isCancel = false

    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet?) : super(context, attributeSet)

    init {
        View.inflate(context, R.layout.message_layout_audio_toast, this)
        icon = findViewById(R.id.iv_toast)
        tips = findViewById(R.id.tv_tips)
        countdown = findViewById(R.id.tv_countdown)
    }

    fun startRecord(showCountDown: Boolean) {
        icon.setImageResource(R.mipmap.message_icon_chat_audio_talking)
        tips.text = StringUtils.getString(R.string.message_chat_audio_tips1)
        visibility = View.VISIBLE
        if (showCountDown) {
            icon.visibility = View.GONE
            countdown.visibility = View.VISIBLE
        } else {
            icon.visibility = View.VISIBLE
            countdown.visibility = View.GONE
        }

    }

    fun showCountDown(time: Long) {
        icon.visibility = View.GONE
        countdown.visibility = View.VISIBLE
        countdown.text = time.toString()
    }

    fun cancelRecord() {
        icon.setImageResource(R.mipmap.message_icon_chat_audio_cancel)
        icon.visibility = View.VISIBLE
        countdown.visibility = View.GONE
        tips.text = StringUtils.getString(R.string.message_chat_audio_tips2)
    }

    fun talkTooShort() {
        icon.visibility = View.VISIBLE
        icon.setImageResource(R.mipmap.message_icon_chat_audio_too_short)
        tips.text = StringUtils.getString(R.string.message_chat_audio_tips3)
        postDelayed({
            visibility = View.GONE
        }, 500)
    }

}