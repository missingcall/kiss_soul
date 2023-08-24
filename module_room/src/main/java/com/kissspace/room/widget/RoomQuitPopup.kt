package com.kissspace.room.widget

import android.content.Context
import android.graphics.Rect
import android.view.View
import com.blankj.utilcode.util.ScreenUtils
import com.kissspace.module_room.R
import com.kissspace.module_room.databinding.RoomPoupNoticeBinding
import com.kissspace.module_room.databinding.RoomPoupQuitBinding
import razerdp.basepopup.BasePopupWindow

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/12/13 17:58
 * @Description: 退出房间弹窗
 *
 */
class RoomQuitPopup(
    context: Context,
    private val onMinimize: () -> Unit,
    private val onQuit: () -> Unit
) :
    BasePopupWindow(context) {
    private lateinit var mBinding: RoomPoupQuitBinding

    init {
        setContentView(R.layout.room_poup_quit)
    }

    override fun onViewCreated(contentView: View) {
        super.onViewCreated(contentView)
        mBinding = RoomPoupQuitBinding.bind(contentView)
        init()
    }

    private fun init() {
        mBinding.layoutQuit.setOnClickListener {
            onQuit()
            dismiss()
        }
        mBinding.layoutMinimize.setOnClickListener {
            onMinimize()
            dismiss()
        }
    }
}