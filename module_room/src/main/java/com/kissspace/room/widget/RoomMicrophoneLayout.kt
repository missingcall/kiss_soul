package com.kissspace.room.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.drake.brv.utils.linear
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

    private lateinit var micUserModel : MicUserModel

    fun initRecyclerView(
        data: MutableList<MicUserModel>,
        onClick: (MicUserModel) -> Unit
    ) {
        micUserModel = data[0]
        mBinding.m = micUserModel
        mBinding.recyclerMicrophoneDefault.linear(RecyclerView.VERTICAL).setup {
            addType<MicUserModel>(R.layout.room_item_microphone_audio)
            onClick(R.id.iv_user_avatar) {
                val model = getModel<MicUserModel>()
                onClick(model)
            }
        }.models = data.subList(1,data.size)
        mBinding.ivUserAvatar.setOnClickListener {
            onClick(micUserModel)
        }
    }


    fun getRecyclerView(): RecyclerView = mBinding.recyclerMicrophoneDefault

    fun getCenterMicroPhone():View = mBinding.ivUserAnim

    /**
     * 麦位表情播放
     */
    fun playMicEmojiAnimation(model: MicUserModel, url: String) {
        var pagView = if (model.onMicroPhoneNumber == 0){
            mBinding.pagEmoji
        }else{
            val recyclerView = getRecyclerView()
            val position =
                recyclerView.getMutable<MicUserModel>()
                    .indexOfFirst { it.wheatPositionId == model.wheatPositionId }
            val view = recyclerView.layoutManager?.findViewByPosition(position)
            view?.findViewById<EasyPagView>(R.id.pag_emoji)
        }
        getPagPath(url) {
            pagView?.play(it)
        }
    }


    fun playTalkAnimation(model: MicUserModel) {
        var soundView = if (model.onMicroPhoneNumber == 0){
            mBinding.svgaSound
        }else{
            val recyclerView = getRecyclerView()
            val position = recyclerView.getMutable<MicUserModel>()
                .indexOfFirst { it.wheatPositionId == model.wheatPositionId }
            recyclerView.layoutManager?.findViewByPosition(position)
                ?.findViewById<LottieAnimationView>(R.id.svga_sound)
        }
        if (soundView?.isAnimating == false) {
            soundView?.visibility = View.VISIBLE
            soundView?.playAnimation()
        }
    }


    /**
     * 清除麦位动画，表情/语音光波
     */
    fun clearMicAnimation(model: MicUserModel) {
        if (model.onMicroPhoneNumber == 0){
            if (mBinding.svgaSound?.isAnimating == true) {
                mBinding.svgaSound?.pauseAnimation()
                mBinding.svgaSound?.visibility = View.INVISIBLE
            }
            if (mBinding.pagEmoji?.isPlaying == true) {
                mBinding.pagEmoji?.clear()
            }
        }else{
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


}

