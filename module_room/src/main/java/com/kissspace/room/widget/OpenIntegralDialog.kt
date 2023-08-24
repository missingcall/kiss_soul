package com.kissspace.room.widget

import android.os.Bundle
import android.view.Gravity
import androidx.core.os.bundleOf
import com.kissspace.common.model.PointsBoxResultBean
import com.kissspace.common.widget.BaseDialogFragment
import com.kissspace.module_room.R
import com.kissspace.module_room.databinding.RoomDialogOpenIntegralBoxResultBinding

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/12/12
 * @Description: 开积分盲盒弹窗
 *
 */
class OpenIntegralDialog :
    BaseDialogFragment<RoomDialogOpenIntegralBoxResultBinding>(Gravity.CENTER) {
    private lateinit var model: PointsBoxResultBean

    companion object {
        fun newInstance(model: PointsBoxResultBean) =
            OpenIntegralDialog().apply {
                arguments = bundleOf("model" to model)
            }
    }

    override fun getLayoutId() = R.layout.room_dialog_open_integral_box_result

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            model = it.getParcelable("model")!!
        }
    }

    override fun initView() {
        mBinding.model = model
        mBinding.tvConfirm.setOnClickListener { dismiss() }
    }



}