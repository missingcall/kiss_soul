package com.kissspace.room.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.kissspace.common.model.RoomInfoBean
import com.kissspace.common.util.mmkv.MMKVProvider
import com.kissspace.common.util.resizeImageUrl
import com.kissspace.module_room.databinding.RoomWidgetActionBarBinding
import com.kissspace.room.interfaces.RoomActionCallBack
import com.kissspace.util.dp
import com.kissspace.util.loadImageCircle

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/12/13 17:54
 * @Description: 房间action bar
 *
 */
class RoomActionBarView : FrameLayout {
    private var mBinding: RoomWidgetActionBarBinding =
        RoomWidgetActionBarBinding.inflate(LayoutInflater.from(context), this, true)
    private var callBack: RoomActionCallBack? = null

    constructor(context: Context) : super(context, null)
    constructor(context: Context, attributeSet: AttributeSet?) : super(context, attributeSet)

    init {
        mBinding.tvCollect.setOnClickListener { callBack?.collect() }
        mBinding.ivNotice.setOnClickListener { callBack?.showNotice() }
        mBinding.ivShare.setOnClickListener { callBack?.share() }
        mBinding.ivClose.setOnClickListener { callBack?.quit() }
        mBinding.ivAnchorAvatar.setOnClickListener { callBack?.showProfile() }
        mBinding.ivBack.setOnClickListener { callBack?.back() }
    }

    fun initData(roomInfo: RoomInfoBean) {
        mBinding.ivAnchorAvatar.loadImageCircle(roomInfo.roomIcon)
        mBinding.tvAnchorNickname.text = roomInfo.roomTitle
        mBinding.tvAnchorId.text = "ID:${roomInfo.beautifulId.ifEmpty { roomInfo.showId }}"
        mBinding.tvCollect.visibility =
            if (roomInfo.collections || roomInfo.houseOwnerId == MMKVProvider.userId) View.GONE else View.VISIBLE
        resizeWidth(roomInfo)
    }

    private fun resizeWidth(roomInfo: RoomInfoBean) {
        val param = mBinding.layoutAnchorInfo.layoutParams as ConstraintLayout.LayoutParams
        param.width =
            if (roomInfo.collections || roomInfo.houseOwnerId == MMKVProvider.userId) 120.dp.toInt() else ViewGroup.LayoutParams.MATCH_PARENT
        mBinding.layoutAnchorInfo.layoutParams = param
    }

    fun setCallBack(callBack: RoomActionCallBack) {
        this.callBack = callBack
    }

    fun hideCollect(roomInfo: RoomInfoBean) {
        mBinding.tvCollect.visibility = GONE
        resizeWidth(roomInfo)
    }

    fun getNoticeView(): ImageView = mBinding.ivNotice

    fun getQuitView(): ImageView = mBinding.ivClose

}