package com.kissspace.common.widget

import android.view.Gravity
import com.kissspace.common.util.customToast
import com.kissspace.module_common.R
import com.kissspace.module_common.databinding.CommonDialogRoomPasswordBinding

/**
 *
 * @Author: nicko
 * @CreateDate: 2023/3/8 17:30
 * @Description: 输入房间密码弹窗
 *
 */

class InputRoomPwdDialog :
    BaseDialogFragment<CommonDialogRoomPasswordBinding>(Gravity.CENTER) {
    private lateinit var pwd: String
    private lateinit var block: (String) -> Unit
    override fun getLayoutId() = R.layout.common_dialog_room_password

    fun initData(password: String, callback: (String) -> Unit) {
        pwd = password
        block = callback
    }

    override fun initView() {
        mBinding.ivClose.setOnClickListener { dismiss() }
        mBinding.tvSubmit.setOnClickListener {
            val text = mBinding.editPassword.text.toString().trim()
            if (text.isEmpty()) {
                customToast("请输入密码")
            } else if (text != pwd) {
                customToast("密码错误")
                dialog?.dismiss()
            } else {
                block(text)
                dialog?.dismiss()
            }
        }
    }

}