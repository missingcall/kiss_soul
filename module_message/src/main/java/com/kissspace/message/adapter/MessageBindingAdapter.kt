package com.kissspace.message.adapter

import android.graphics.Outline
import android.graphics.drawable.AnimationDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.databinding.BindingAdapter
import com.blankj.utilcode.util.StringUtils
import com.hjq.bar.TitleBar
import com.opensource.svgaplayer.SVGAImageView
import com.kissspace.common.ext.setDrawable
import com.kissspace.common.model.ChatModel
import com.kissspace.common.model.LoveWallListBean
import com.kissspace.common.model.LoveWallResponse
import com.kissspace.util.ellipsizeString
import com.kissspace.common.util.getFriendlyTimeSpanByNow
import com.kissspace.common.util.getPagPath
import com.kissspace.util.resToColor
import com.kissspace.common.widget.EasyPagImageView
import com.kissspace.module_message.R
import com.kissspace.util.dp
import com.kissspace.util.loadImage
import com.youth.banner.Banner
import java.io.File

object MessageBindingAdapter {
    @JvmStatic
    @BindingAdapter("chatInputType", requireAll = false)
    fun chatInputType(imageView: ImageView, mode: Int) {
        imageView.setImageResource(if (mode == 0) R.mipmap.message_icon_chat_talking else R.mipmap.message_icon_chat_type)
    }

    @JvmStatic
    @BindingAdapter("chatDate", requireAll = false)
    fun chatDate(textView: TextView, model: ChatModel) {
        textView.visibility = if (model.isShowTime == 1) View.VISIBLE else View.GONE
        textView.text = getFriendlyTimeSpanByNow(model.timestamp)
    }

    @JvmStatic
    @BindingAdapter("editVisible", requireAll = false)
    fun editVisible(editText: View, mode: Int) {
        editText.visibility = if (mode == 0) View.VISIBLE else View.GONE
    }

    @JvmStatic
    @BindingAdapter("audioVisible", requireAll = false)
    fun audioVisible(text: TextView, mode: Int) {
        text.visibility = if (mode == 0) View.GONE else View.VISIBLE
    }

    @JvmStatic
    @BindingAdapter(value = ["imageFile", "imageFileRadius"])
    fun loadImageFile(imageView: ImageView, imageFile: String, imageFileRadius: Float = 0f) {
        imageView.loadImage(File(imageFile), radius = imageFileRadius)

    }

    @JvmStatic
    @BindingAdapter("messageUnReadCount", requireAll = false)
    fun messageUnReadCount(text: TextView, count: Int) {
        text.visibility = if (count > 0) View.VISIBLE else View.GONE
        text.text = if (count < 100) count.toString() else "99+"
    }

    @JvmStatic
    @BindingAdapter("rightAudioIconState", requireAll = false)
    fun rightAudioIconState(imageView: ImageView, isPlay: Boolean) {
        if (isPlay) {
            imageView.setBackgroundResource(R.drawable.message_anim_audio_play_right)
            val drawable = imageView.background as AnimationDrawable
            drawable.start()
        } else {
            if (imageView.background is AnimationDrawable) {
                (imageView.background as AnimationDrawable).stop()
            }
            imageView.setBackgroundResource(R.mipmap.message_icon_audio_right_3)
        }
    }

    @JvmStatic
    @BindingAdapter("leftAudioIconState", requireAll = false)
    fun leftAudioIconState(imageView: ImageView, isPlay: Boolean) {
        if (isPlay) {
            imageView.setBackgroundResource(R.drawable.message_anim_audio_play_left)
            val drawable = imageView.background as AnimationDrawable
            drawable.start()
        } else {
            if (imageView.background is AnimationDrawable) {
                (imageView.background as AnimationDrawable).stop()
            }
            imageView.setBackgroundResource(R.mipmap.message_icon_audio_left_3)
        }
    }

    @JvmStatic
    @BindingAdapter("sendChatBtnEnable", requireAll = false)
    fun sendChatBtnEnable(textView: TextView, enable: Boolean) {
        if (enable) {
            textView.setBackgroundResource(R.drawable.message_bg_chat_send_btn_enable)
        } else {
            textView.setBackgroundResource(R.drawable.message_bg_chat_send_btn_disable)
        }
    }


    @JvmStatic
    @BindingAdapter("followBackground", requireAll = false)
    fun followBackground(layout: FrameLayout, isFollow: Boolean) {
        layout.setBackgroundResource(if (isFollow) R.drawable.message_bg_chat_has_follow else R.drawable.message_bg_chat_un_follow)
    }

    @JvmStatic
    @BindingAdapter("chatFollowText", requireAll = false)
    fun chatFollowText(imageView: ImageView, isFollow: Boolean) {
        if (isFollow) {
            imageView.setImageResource(R.mipmap.message_chat_icon_follow)
        } else {
            imageView.setImageResource(R.mipmap.message_chat_icon_no_follow)
        }

    }


    @JvmStatic
    @BindingAdapter("loveWallUserInfo", requireAll = false)
    fun loveWallUserInfo(textView: TextView, loveWallListBean: LoveWallListBean) {
        val nameColor = com.kissspace.module_common.R.color.common_white.resToColor()
        textView.text = buildSpannedString {
            color(nameColor) {
                bold {
                    append(loveWallListBean.sourceUserNickname.ellipsizeString(7))
                }
            }
            append(" 对 ")
            color(nameColor) {
                bold {
                    append(loveWallListBean.targetUserNickname.ellipsizeString(7))
                }
            }
            append("一见倾心")
        }
    }

    @JvmStatic
    @BindingAdapter("loveWallGiftInfo", requireAll = false)
    fun loveWallGiftInfo(textView: TextView, model: LoveWallListBean) {
        textView.text = buildSpannedString {
            color(com.kissspace.module_common.R.color.common_white.resToColor()) {
                append("壕刷")
            }
            color(com.kissspace.module_common.R.color.color_FFFD62.resToColor()) {
                append(model.giftName + "x" + model.number)
            }
        }
    }

    @JvmStatic
    @BindingAdapter("enterRoomText", requireAll = false)
    fun enterRoomText(textView: TextView, loveWallListBean: LoveWallListBean) {
        val normalColor = com.kissspace.module_common.R.color.common_white.resToColor()
        val redColor = com.kissspace.module_common.R.color.color_FFFD62.resToColor()
        textView.text = buildSpannedString {
            color(normalColor) {
                append("前往  ")
            }
            color(redColor) {
                append(loveWallListBean.chatRoomTitle.ellipsizeString(10))
            }
            color(normalColor) {
                append(" 围观")
            }
        }
    }


    @JvmStatic
    @BindingAdapter("audioUnReadDotVisible", requireAll = false)
    fun audioUnReadDotVisible(view: View, isPlayed: Boolean) {
        view.visibility = if (isPlayed) View.GONE else View.VISIBLE
    }

    @JvmStatic
    @BindingAdapter("messageUnReadTotal", requireAll = false)
    fun messageUnReadTotal(textView: TextView, count: Int) {
        textView.visibility = if (count > 0) View.VISIBLE else View.GONE
        textView.text = StringUtils.getString(R.string.message_un_read_count, count)
    }

    @JvmStatic
    @BindingAdapter("messageFollowRoomVisible", requireAll = false)
    fun messageFollowRoomVisible(layout: LinearLayout, roomId: String?) {
        layout.visibility = if (roomId.isNullOrEmpty()) View.GONE else View.VISIBLE
    }

    @JvmStatic
    @BindingAdapter("messageDate", requireAll = false)
    fun messageDate(textView: TextView, date: Long?) {
        textView.text = getFriendlyTimeSpanByNow(date ?: 0)
    }

    @JvmStatic
    @BindingAdapter("messagePicture", requireAll = false)
    fun messagePicture(imageView: ImageView, url: String) {
        imageView.loadImage(url, topLeftRadius = 12f, topRightRadius = 12f)
    }

    @JvmStatic
    @BindingAdapter("chatTitle", requireAll = false)
    fun chatTitle(textView: TitleBar, title: String?) {
        textView.title = title?.ellipsizeString(7)
    }

    @JvmStatic
    @BindingAdapter("chatFollowRoomVisible", requireAll = false)
    fun chatFollowRoomVisible(layout: ConstraintLayout, roomId: String?) {
        layout.visibility = if (roomId.isNullOrEmpty()) View.GONE else View.VISIBLE
    }

    @JvmStatic
    @BindingAdapter("chatUserOnlineState", requireAll = false)
    fun chatUserOnlineState(svgaImageView: SVGAImageView, onlineState: String?) {
        if (onlineState == "001") {
            svgaImageView.visibility = View.VISIBLE
            svgaImageView.startAnimation()
        } else {
            svgaImageView.visibility = View.GONE
            svgaImageView.pauseAnimation()
        }
    }

    @JvmStatic
    @BindingAdapter("bindMessageBanner", requireAll = false)
    fun bindMessageBanner(
        banner: Banner<LoveWallListBean, MessageBannerAdapter>, model: LoveWallResponse
    ) {
        banner.visibility = if (model.giveGiftRecordList.isEmpty()) View.GONE else View.VISIBLE
        banner.apply {
            setAdapter(MessageBannerAdapter(model.giveGiftRecordList))
            setLoopTime(5000)
            outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View?, outline: Outline?) {
                    outline?.setRoundRect(
                        0, 0, view?.width!!, view.height, 12.dp
                    )
                }
            }
            clipToOutline = true
        }
    }

    @JvmStatic
    @BindingAdapter("loadEmoji", requireAll = false)
    fun loadEmoji(pagView: EasyPagImageView,model: ChatModel) {
        pagView.setRepeatCount(if (model.isEmojiLoop) Int.MAX_VALUE else 1)
        val emojiUrl = model.emojiUrl
        if (!emojiUrl.isNullOrEmpty()){
            getPagPath(emojiUrl) {
                pagView.play(it)
            }
        }
    }
}