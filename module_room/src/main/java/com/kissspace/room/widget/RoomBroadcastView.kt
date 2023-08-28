package com.kissspace.room.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.kissspace.common.model.RoomScreenMessageModel
import com.kissspace.module_room.R
import com.kissspace.module_room.databinding.RoomWidgetBroadcastBinding
import com.kissspace.room.interfaces.RoomBroadCastCallBack
import com.kissspace.util.loadImageCircle

class RoomBroadcastView : FrameLayout {
    private var mBinding: RoomWidgetBroadcastBinding =
        RoomWidgetBroadcastBinding.inflate(LayoutInflater.from(context), this, true)
    private var callBack: RoomBroadCastCallBack? = null
    private var costCoin : Int = 1000
    private var isStart : Boolean = true

    constructor(context: Context) : super(context, null)
    constructor(context: Context, attributeSet: AttributeSet?) : super(context, attributeSet)

    init {
        mBinding.ivPublish.setOnClickListener {
            callBack?.showBroadCast(costCoin,isStart)
        }
    }

    fun initData(messageInfo: RoomScreenMessageModel?) {
        if (messageInfo != null){
            isStart = false
            costCoin = messageInfo.costCoin
            mBinding.ivAvatar.loadImageCircle(messageInfo.profilePath)
            mBinding.tvNickname.text = messageInfo.nickname
            mBinding.tvMessage.text = messageInfo.messageContent
            mBinding.tvPrice.text = messageInfo.costCoin.toString()
        }else{
            isStart = true
            costCoin = 1000
            mBinding.ivAvatar.setImageResource(R.mipmap.room_bg_broadcast_defalut_avatar)
            mBinding.tvNickname.text = "官方"
            mBinding.tvMessage.text = "发你想说的，让所有人都知道！"
            mBinding.tvPrice.text = "1000"
        }
    }

    fun setCallBack(callBack: RoomBroadCastCallBack) {
        this.callBack = callBack
    }

}