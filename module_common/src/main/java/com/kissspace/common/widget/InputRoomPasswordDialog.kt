package com.kissspace.common.widget

import android.view.Gravity
import androidx.fragment.app.FragmentManager
import com.blankj.utilcode.util.ToastUtils
import com.kissspace.module_common.R
import com.kissspace.module_common.databinding.CommonDialogRoomPasswordBinding

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/12/20 13:53
 * @Description: 房间密码设置弹窗
 *
 */
class InputRoomPasswordDialog :
    BaseDialogFragment<CommonDialogRoomPasswordBinding>(Gravity.CENTER) {
    private lateinit var callBack: String.() -> Unit

    fun setCallBack(block: String.() -> Unit) {
        callBack = block
    }

    override fun getLayoutId() = R.layout.common_dialog_room_password

    override fun initView() {
        mBinding.ivClose.setOnClickListener {
            dismiss()
        }
        mBinding.tvSubmit.setOnClickListener {
            val text = mBinding.editPassword.text.toString().trim()
            if (text.isEmpty()) {
                ToastUtils.showLong("请输入密码")
            } else {
                callBack(text)
                dismiss()
            }
        }
    }

}