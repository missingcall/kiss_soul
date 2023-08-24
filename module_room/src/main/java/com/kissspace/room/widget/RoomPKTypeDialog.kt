package com.kissspace.room.widget

import android.os.Bundle
import androidx.core.os.bundleOf
import com.drake.brv.utils.linear
import com.kissspace.common.ext.safeClick
import com.kissspace.common.flowbus.Event
import com.kissspace.common.flowbus.FlowBus
import com.kissspace.common.widget.BaseBottomSheetDialogFragment
import com.kissspace.module_room.databinding.RoomDialogPkTypeBinding
import com.kissspace.network.net.Method
import com.kissspace.network.net.request
import com.kissspace.room.http.RoomApi
import com.kissspace.util.toast

/**
 *
 * @Author: nicko
 * @CreateDate: 2023/5/10 15:06
 * @Description: 选择PK类型弹窗
 *
 */

class RoomPKTypeDialog : BaseBottomSheetDialogFragment<RoomDialogPkTypeBinding>() {
    private lateinit var roomId: String

    companion object {
        fun newInstance(roomId: String) = RoomPKTypeDialog().apply {
            arguments = bundleOf("roomId" to roomId)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            roomId = it.getString("roomId")!!
        }
    }

    override fun getViewBinding(): RoomDialogPkTypeBinding =
        RoomDialogPkTypeBinding.inflate(layoutInflater)

    override fun initView() {
        mBinding.tvSubmit.safeClick {
            startPK()
        }

        mBinding.ivQuestion.safeClick {
            RoomPKRuleDialog().show(childFragmentManager)
        }
    }

    private fun startPK() {
        val param = mutableMapOf<String, Any?>()
        param["roomId"] = roomId
        param["pkTimeCountdown"] = 600
        request<Any?>(RoomApi.API_START_PK, Method.POST, param, onSuccess = {
            dismiss()
        }, onError = {
            toast(it.errorMsg)
        })
    }
}