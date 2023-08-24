package com.kissspace.room.widget

import android.content.Context
import android.graphics.Rect
import android.view.View
import com.blankj.utilcode.util.ScreenUtils
import com.kissspace.module_room.R
import com.kissspace.module_room.databinding.RoomPoupNoticeBinding
import com.kissspace.util.dp
import razerdp.basepopup.BasePopupWindow

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/12/13 17:58
 * @Description: 房间公告弹窗
 *
 */
class RoomNoticePopup(context: Context, private val content: String) : BasePopupWindow(context) {
    private lateinit var mBinding: RoomPoupNoticeBinding

    init {
        setContentView(R.layout.room_poup_notice)
        init()
    }

    override fun onViewCreated(contentView: View) {
        super.onViewCreated(contentView)
        mBinding = RoomPoupNoticeBinding.bind(contentView)
    }

    private fun init() {
        width = ScreenUtils.getScreenWidth()
        mBinding.tvContent.text = content
    }

    override fun onPopupLayout(popupRect: Rect, anchorRect: Rect) {
        super.onPopupLayout(popupRect, anchorRect)
        //让箭头居anchor中间显示
        mBinding.ivArrow.translationX = anchorRect.left.toFloat()+13.dp.toInt()
    }
}