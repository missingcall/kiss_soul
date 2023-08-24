package com.kissspace.setting.widget

import android.content.Context
import com.kissspace.common.router.RouterPath
import com.kissspace.common.router.jump
import com.kissspace.common.widget.BaseDialog
import com.kissspace.module_setting.R
import com.kissspace.module_setting.databinding.SeetingTeenagerDialogBinding

/**
 * @Author gaohangbo
 * @Date 2023/2/11 19:35.
 * @Describe
 */
class TeenagerDialog(context:Context) : BaseDialog<SeetingTeenagerDialogBinding>(context) {

    init {
        setCancelable(false)
        setCanceledOnTouchOutside(false)
    }

    override fun getLayoutId(): Int {
        return R.layout.seeting_teenager_dialog
    }

    override fun initView() {
        mBinding.tvCancel.setOnClickListener {
            dismiss()
        }
        mBinding.tvTeenager.setOnClickListener {
            jump(RouterPath.PATH_TEENAGER_MODE)
        }
    }
}