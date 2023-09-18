package com.kissspace.room.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.drake.brv.utils.grid
import com.drake.brv.utils.setup
import com.kissspace.common.model.MicUserModel
import com.kissspace.common.util.getMutable
import com.kissspace.common.util.getPagPath
import com.kissspace.common.widget.EasyPagView
import com.kissspace.module_room.R
import com.kissspace.module_room.databinding.RoomLayoutMicrophoneBinding

/**
 *
 * @Author: nicko
 * @CreateDate: 2023/5/11 15:58
 * @Description: 房间麦位列表
 *
 */

class RoomMicrophoneLayout : ConstraintLayout {
    private var mBinding: RoomLayoutMicrophoneBinding =
        RoomLayoutMicrophoneBinding.inflate(LayoutInflater.from(context), this, true)

    constructor(context: Context) : super(context, null)
    constructor(context: Context, attributeSet: AttributeSet?) : super(context, attributeSet)


    fun initRecyclerView(
        data: MutableList<MicUserModel>,
        onClick: (MicUserModel) -> Unit
    ) {
        mBinding.recyclerMicrophoneDefault.setup {
            addType<MicUserModel>(R.layout.room_item_microphone_audio)
            onClick(R.id.iv_user_avatar) {
                val model = getModel<MicUserModel>()
                onClick(model)
            }
        }.models = data
        val layoutManager = GridLayoutManager(context, 4).apply {
            spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (position == 0) 4 else 1
                }
            }
        }
        mBinding.recyclerMicrophoneDefault.layoutManager = layoutManager

    }


    fun getRecyclerView(): RecyclerView = mBinding.recyclerMicrophoneDefault

    /**
     * 麦位表情播放
     */
    fun playMicEmojiAnimation(model: MicUserModel, url: String) {
        val recyclerView = getRecyclerView()
        val position =
            recyclerView.getMutable<MicUserModel>()
                .indexOfFirst { it.wheatPositionId == model.wheatPositionId }
        val view = recyclerView.layoutManager?.findViewByPosition(position)
        val pagView = view?.findViewById<EasyPagView>(R.id.pag_emoji)
        getPagPath(url) {
            pagView?.play(it)
        }
    }


    fun playTalkAnimation(model: MicUserModel) {
        val recyclerView = getRecyclerView()
        val position = recyclerView.getMutable<MicUserModel>()
            .indexOfFirst { it.wheatPositionId == model.wheatPositionId }
        val soundView =
            recyclerView.layoutManager?.findViewByPosition(position)
                ?.findViewById<LottieAnimationView>(R.id.svga_sound)
        if (soundView?.isAnimating == false) {
            soundView?.visibility = View.VISIBLE
            soundView?.playAnimation()
        }
    }


    /**
     * 清除麦位动画，表情/语音光波
     */
    fun clearMicAnimation(model: MicUserModel) {
        val recyclerView = getRecyclerView()
        val position = recyclerView.getMutable<MicUserModel>()
            .indexOfFirst { it.wheatPositionId == model.wheatPositionId }
        val soundView = recyclerView.layoutManager?.findViewByPosition(position)
            ?.findViewById<LottieAnimationView>(R.id.svga_sound)
        if (soundView?.isAnimating == true) {
            soundView?.pauseAnimation()
            soundView?.visibility = View.INVISIBLE
        }
        val emojiView = recyclerView.layoutManager?.findViewByPosition(position)
            ?.findViewById<EasyPagView>(R.id.pag_emoji)
        if (emojiView?.isPlaying == true) {
            emojiView?.clear()
        }
    }


}

