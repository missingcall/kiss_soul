package com.kissspace.room.widget

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.View
import com.kissspace.common.config.Constants
import com.kissspace.module_room.R
import com.kissspace.module_room.databinding.RoomPoupPredictionMenuBinding
import com.kissspace.module_room.databinding.RoomPoupProfileMenuBinding


/**
 *
 * @Author: nicko
 * @CreateDate: 2022/12/13 17:59
 * @Description: 用户资料菜单弹窗
 *
 */
class UserProfileMenuPop(
    context: Context,
    private val isBan: Boolean,
    private val role: String,
    private val block: ClickType.() -> Unit
) : Dialog(context, com.kissspace.module_common.R.style.Theme_CustomDialog) {
    private var mBinding = RoomPoupProfileMenuBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)
        init()
    }

    private fun init() {
        mBinding.tvBanUser.text = if (isBan) "取消拉黑" else "拉黑用户"
        mBinding.viewLineBottom.visibility =
            if (role != Constants.ROOM_USER_TYPE_NORMAL) View.VISIBLE else View.GONE
        mBinding.tvBanUserInRoom.visibility =
            if (role != Constants.ROOM_USER_TYPE_NORMAL) View.VISIBLE else View.GONE
        mBinding.tvBanUser.setOnClickListener {
            block(ClickType.BAN_USER)
            dismiss()
        }

        mBinding.tvReportUser.setOnClickListener {
            block(ClickType.REPORT_USER)
            dismiss()
        }

        mBinding.tvBanUserInRoom.setOnClickListener {
            block(ClickType.BAN_USER_IN_ROOM)
            dismiss()
        }
    }

    fun show(anchorView: View) {
        val location = intArrayOf(0, 0)
        anchorView.getLocationOnScreen(location)
        window?.setGravity(Gravity.TOP or Gravity.RIGHT)
        val lp = window?.attributes
        lp?.y = location[1] - 5
        window?.attributes = lp
        super.show()
    }

    sealed class ClickType {
        //举报
        object REPORT_USER : ClickType()

        //拉黑
        object BAN_USER : ClickType()

        //加入房间黑名单
        object BAN_USER_IN_ROOM : ClickType()

    }
}