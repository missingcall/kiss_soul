package com.kissspace.common.widget

import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.drake.net.utils.scopeDialog
import com.kissspace.common.ext.safeClick
import com.kissspace.common.util.copyClip
import com.kissspace.module_common.R
import com.kissspace.module_common.databinding.CommonDialogHintBinding

/**
 * @Author gaohangbo
 * @Date 2022/12/28 07:55.
 * @Describe 确认弹窗
 */
class CommonHintDialog : BaseDialogFragment<CommonDialogHintBinding>(Gravity.CENTER) {

    private var hintContent: String? = null
    private var contactInformation: String? = null
    private var isShowButton: Boolean? = null
    var callBack: (() -> Unit?)? = null
    override fun getLayoutId(): Int {
        return R.layout.common_dialog_hint
    }

    companion object {
        fun newInstance(
            hintContent: String? = null,
            contactInformation: String? = null,
            isShowButton: Boolean? = false
        ) = CommonHintDialog().apply {
            arguments = bundleOf(
                "hintContent" to hintContent,
                "contactInformation" to contactInformation,
                "isShowButton" to isShowButton
            )
        }
    }

    override fun initView() {
        arguments?.let {
            hintContent = it.getString("hintContent")
            contactInformation = it.getString("contactInformation")
            isShowButton = it.getBoolean("isShowButton", false)
        }
        if (isShowButton == true) {
            mBinding.tvHintTitle.visibility = View.VISIBLE
            mBinding.tvHintTitle.text = contactInformation
            mBinding.tvNumber.visibility = View.GONE
            mBinding.tvCopy.visibility = View.GONE
            mBinding.clBottom.visibility = View.VISIBLE
            mBinding.clBottom.safeClick {
                callBack?.invoke()
            }
        } else {
            mBinding.tvNumber.text = contactInformation
            mBinding.tvCopy.visibility = View.VISIBLE
            mBinding.tvNumber.visibility = View.VISIBLE
            mBinding.tvHintTitle.visibility = View.GONE
            mBinding.clBottom.visibility = View.GONE
        }
        mBinding.tvHint.text = hintContent
        mBinding.ivClose.safeClick {
            dismiss()
        }
        mBinding.tvCopy.safeClick {
            //获取剪切板管理器
            copyClip(mBinding.tvNumber.text.toString())
        }
    }


    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            val attr = attributes
            attr.width = context.resources.displayMetrics.widthPixels - SizeUtils.dp2px(64f)
            attr.height = WindowManager.LayoutParams.WRAP_CONTENT
            attributes = attr
        }
    }
}