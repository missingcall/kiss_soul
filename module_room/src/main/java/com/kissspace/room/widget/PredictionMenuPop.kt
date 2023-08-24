package com.kissspace.room.widget

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.View
import com.kissspace.module_room.R
import com.kissspace.module_room.databinding.RoomPoupPredictionMenuBinding


/**
 *   积分预言菜单弹窗
 */
class PredictionMenuPop(context: Context, private val block: ClickType.() -> Unit) :
    Dialog(context, com.kissspace.module_common.R.style.Theme_CustomDialog) {
    private var mBinding = RoomPoupPredictionMenuBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)
        init()
    }

    private fun init() {
        mBinding.layoutMine.setOnClickListener {
            block(ClickType.MENU_MINE)
            dismiss()
        }

        mBinding.layoutSquare.setOnClickListener {
            block(ClickType.MENU_SQUARE)
            dismiss()
        }

        mBinding.layoutTips.setOnClickListener {
            block(ClickType.MENU_TIPS)
            dismiss()
        }
    }

    fun show(anchorView: View) {
        val location = intArrayOf(0, 0)
        anchorView.getLocationOnScreen(location)
        window?.setGravity(Gravity.TOP or Gravity.RIGHT)
        val lp = window?.attributes
        lp?.y = location[1] - 20
        window?.attributes = lp
        super.show()
    }

    sealed class ClickType {
        //玩法说明
        object MENU_TIPS : ClickType()

        //玩法说明
        object MENU_MINE : ClickType()

        //预言广场
        object MENU_SQUARE : ClickType()
    }
}