package com.kissspace.common.widget

import android.app.Dialog
import android.content.Context
import android.view.ViewGroup
import android.widget.TextView
import com.kissspace.common.ext.safeClick
import com.kissspace.module_common.R
import com.kissspace.util.dp
import com.kissspace.util.screenWidth

/**
 * @Author gaohangbo
 * @Date 2023/2/3 14:42.
 * @Describe 实名认证弹窗
 */
class VerifyNameDialog(context: Context,  private  var callBack: (() -> Unit?)? = null) :
    Dialog(context, R.style.Theme_CustomDialog) {

    private var tvNegative: TextView? = null
    private var tvPositive: TextView? = null

    init {
        setContentView(R.layout.common_verify_name)
        setCancelable(false)
        setCanceledOnTouchOutside(false)
        initView()
    }


    private fun initView() {
        tvNegative = findViewById(R.id.tv_negative)
        tvPositive = findViewById(R.id.tv_positive)

        tvNegative?.safeClick {
            dismiss()
        }
        tvPositive?.safeClick {
            callBack?.invoke()
            dismiss()
        }
    }


    override fun onStart() {
        super.onStart()
        this.window?.apply {
            val attr = attributes
            attr.width = screenWidth - 64.dp.toInt()
            attr.height = ViewGroup.LayoutParams.WRAP_CONTENT
            attributes = attr
        }
    }
}