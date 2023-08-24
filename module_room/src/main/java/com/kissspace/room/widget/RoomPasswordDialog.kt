package com.kissspace.room.widget

import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.kissspace.common.config.CommonApi
import com.kissspace.common.model.CheckRoomInfoModel
import com.kissspace.common.util.customToast
import com.kissspace.common.widget.BaseDialogFragment
import com.kissspace.module_room.R
import com.kissspace.module_room.databinding.RoomDialogRoomPasswordBinding
import com.kissspace.network.net.Method
import com.kissspace.network.result.collectData
import com.kissspace.network.net.request
import com.kissspace.room.viewmodel.RoomPasswordViewModel
import com.kissspace.util.toast

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/12/20 13:53
 * @Description: 房间密码设置弹窗
 *
 */
class RoomPasswordDialog : BaseDialogFragment<RoomDialogRoomPasswordBinding>(Gravity.CENTER) {
    private lateinit var crId: String
    private val mViewModel by viewModels<RoomPasswordViewModel>()
    private var callBack: (String.() -> Unit)? = null

    companion object {
        fun newInstance(crId: String) = RoomPasswordDialog().apply {
            arguments = bundleOf("crdId" to crId)
        }
    }

    fun setCallBack(block: String.() -> Unit) {
        callBack = block
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        crId = arguments?.getString("crdId")!!
    }

    override fun getLayoutId() = R.layout.room_dialog_room_password

    override fun initView() {
        val params = mutableMapOf<String, Any?>("chatRoomId" to crId)
        request<CheckRoomInfoModel>(
            CommonApi.API_QUERY_ROOM_PASSWORD,
            Method.GET,
            params,
            onSuccess = {
                mBinding.editPassword.setText(it.roomPwd)
                mBinding.tvUpdateSingle.visibility = if (it.roomPwd.isEmpty()) View.VISIBLE else View.GONE
                mBinding.tvClose.visibility = if (it.roomPwd.isEmpty()) View.GONE else View.VISIBLE
                mBinding.tvUpdate.visibility = if (it.roomPwd.isEmpty()) View.GONE else View.VISIBLE
                mBinding.ivClose.setOnClickListener {
                    dismiss()
                }
                mBinding.tvUpdateSingle.setOnClickListener {
                    updatePassword()
                }
                mBinding.tvUpdate.setOnClickListener {
                    updatePassword()
                }
                mBinding.tvClose.setOnClickListener {
                    mViewModel.closePassword(crId)
                }
            },
            onError = { customToast(it.message) })
    }

    private fun updatePassword() {
        val text = mBinding.editPassword.text.toString().trim()
        if (text.isEmpty()) {
            customToast("请设置密码")
        } else {
            mViewModel.setPassword(crId, text)
        }
    }

    override fun observerData() {
        super.observerData()
        collectData(mViewModel.setPasswordEvent, onSuccess = {
            callBack?.invoke(mBinding.editPassword.text.toString().trim())
            dismiss()
            toast("密码设置成功")
        }, onError = {
            toast(it.message)
        })

        collectData(mViewModel.closePasswordEvent, onSuccess = {
            callBack?.invoke("")
            dismiss()
            customToast("房间密码已关闭")
        })

    }



}