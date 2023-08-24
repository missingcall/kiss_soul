package com.kissspace.mine.widget

import android.view.Gravity
import android.view.WindowManager
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.kissspace.common.model.UserInfoBean
import com.kissspace.common.widget.BaseDialogFragment
import com.kissspace.module_mine.R
import com.kissspace.module_mine.databinding.MineWalletConfirmDialogBinding

/**
 * @Author gaohangbo
 * @Date 2023/1/1 21:23.
 * @Describe 转账确认弹窗
 */
class TransferConfirmDialog : BaseDialogFragment<MineWalletConfirmDialogBinding>(Gravity.CENTER){

    override fun getLayoutId(): Int {
        return R.layout.mine_wallet_confirm_dialog
    }
    companion object{
        fun newInstance(userInfoBean: UserInfoBean?) = TransferConfirmDialog().apply {
            arguments = bundleOf("userInfoBean" to userInfoBean)
        }
    }
    override fun initView() {
       // var walletUserModel =WalletUserModel("111","nickname","https://img2.woyaogexing.com/2022/11/15/3a45cdfdb7898278!400x400.jpg")
        val userInfoBean=arguments?.getParcelable<UserInfoBean>("userInfoBean")
        mBinding.m = userInfoBean
        mBinding.tvNegative.setOnClickListener {
            dismiss()

        }
        mBinding.tvPositive.setOnClickListener {
            callBack?.invoke()
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            val attr = attributes
            attr.width = context.resources.displayMetrics.widthPixels- SizeUtils.dp2px(64f)
            attr.height = WindowManager.LayoutParams.WRAP_CONTENT
            attributes = attr
        }
    }
    private var callBack: (() -> Unit)? = null

    fun setCallBack(block: () -> Unit) {
        callBack = block
    }
}