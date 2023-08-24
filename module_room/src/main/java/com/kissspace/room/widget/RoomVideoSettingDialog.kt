package com.kissspace.room.widget

import androidx.fragment.app.FragmentManager
import com.kissspace.util.toast
import com.kissspace.common.ext.safeClick
import com.kissspace.common.widget.BaseBottomSheetDialogFragment
import com.kissspace.module_common.R
import com.kissspace.module_room.databinding.RoomDialogVideoSettingBinding
import com.kissspace.network.net.Method
import com.kissspace.network.net.request
import com.kissspace.room.http.RoomApi

/**
 *
 * @Author: nicko
 * @CreateDate: 2023/3/14 19:34
 * @Description: 房间视频设置页
 *
 */

class RoomVideoSettingDialog(private val crId: String, private val direction: String) :
    BaseBottomSheetDialogFragment<RoomDialogVideoSettingBinding>() {
    override fun getViewBinding(): RoomDialogVideoSettingBinding =
        RoomDialogVideoSettingBinding.inflate(layoutInflater)

    override fun initView() {
        var state = direction
        resetAll(state)
        mBinding.ivCheckboxVertical.setOnClickListener {
            resetAll("001")
            state = "001"
        }
        mBinding.ivCheckbox43.setOnClickListener {
            resetAll("002")
            state = "002"
        }
        mBinding.ivCheckbox169.setOnClickListener {
            resetAll("003")
            state = "003"
        }
        mBinding.tvConfirm.safeClick {
            val param = mutableMapOf<String, Any?>()
            param["roomId"] = crId
            param["screenStatus"] = state
            request<Boolean>(RoomApi.API_UPDATE_SCREEN_DIRECTION, Method.GET, param, onSuccess = {
                this@RoomVideoSettingDialog.dismiss()
            }, onError = {
                toast(it.message)
            })
        }
    }

    private fun resetAll(state: String) {
        mBinding.ivCheckbox169.setImageResource(R.mipmap.common_icon_agreement_normal)
        mBinding.ivCheckbox43.setImageResource(R.mipmap.common_icon_agreement_normal)
        mBinding.ivCheckboxVertical.setImageResource(R.mipmap.common_icon_agreement_normal)
        when (state) {
            "001" -> {
                mBinding.ivCheckboxVertical.setImageResource(R.mipmap.common_icon_agreement_selected)
            }

            "002" -> {
                mBinding.ivCheckbox43.setImageResource(R.mipmap.common_icon_agreement_selected)
            }

            "003" -> {
                mBinding.ivCheckbox169.setImageResource(R.mipmap.common_icon_agreement_selected)
            }
        }
    }

}