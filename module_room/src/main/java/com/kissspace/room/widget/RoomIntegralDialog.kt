package com.kissspace.room.widget

import android.graphics.drawable.GradientDrawable.Orientation
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.kissspace.common.widget.BaseBottomSheetDialogFragment
import com.kissspace.module_room.databinding.RoomDialogIntegralBinding

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/12/29 10:10
 * @Description: 领取积分弹窗
 *
 */
class RoomIntegralDialog : BaseBottomSheetDialogFragment<RoomDialogIntegralBinding>() {
    override fun getViewBinding() = RoomDialogIntegralBinding.inflate(layoutInflater)

    override fun initView() {
        mBinding.recyclerView.linear(orientation = RecyclerView.HORIZONTAL).setup {

        }
    }

}