package com.kissspace.login.widget

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams
import com.blankj.utilcode.util.SizeUtils
import com.qmuiteam.qmui.kotlin.onClick
import com.kissspace.common.router.jump
import com.kissspace.common.ext.safeClick
import com.kissspace.common.router.RouterPath
import com.kissspace.util.isNotEmptyBlank
import com.kissspace.common.util.customToast
import com.kissspace.common.widget.BaseDialog
import com.kissspace.module_login.R
import com.kissspace.module_login.databinding.LoginInvitationDialogBinding
import com.kissspace.util.hideKeyboard
import com.kissspace.util.loadImage

/**
 * @Author gaohangbo
 * @Date 2023/1/31 11:58.
 * @Describe 邀请码弹窗
 */
class InvitationDialog(context: Context) :
     BaseDialog<LoginInvitationDialogBinding>(context) {
    var callBack: ((String) -> Unit?)? = null

    override fun getLayoutId(): Int {
        return R.layout.login_invitation_dialog
    }

    override fun initView() {
        mBinding.ivClose.setOnClickListener {
            dismiss()
        }
        mBinding.ivSubmit.onClick {
            if (mBinding.etInvitation.text.isNotEmptyBlank()) {
                if (mBinding.etInvitation.text.length == 3) {
                    callBack?.invoke(mBinding.etInvitation.text.toString())
                } else {
                    customToast("邀请码数字为3位")
                }
            } else {
                customToast("请输入邀请码")
            }
        }

        mBinding.ivTearOpen.onClick {
            callBack?.invoke(mBinding.etInvitation.text.toString())
        }
    }

    fun setData(avatarUrl: String?, blindUrl: String?) {
        mBinding.ivBlindGift.loadImage(blindUrl)
        mBinding.ivAvatarGift.loadImage(avatarUrl)
    }

    fun showSuccess(isAutoWear: Boolean) {
        mBinding.etInvitation.hideKeyboard()
        mBinding.etInvitation.visibility = View.GONE
        if (isAutoWear) {
            mBinding.tvAvatarFrame.visibility = View.VISIBLE
        } else {
            mBinding.rlAvatarFrameWear.visibility = View.VISIBLE
            mBinding.rlAvatarFrameWear.safeClick {
                jump(RouterPath.PATH_MY_DRESS_UP, "position" to 1)
                dismiss()
            }
        }
        mBinding.tvBlind.visibility = View.VISIBLE
        mBinding.ivTitle.setImageResource(R.mipmap.login_icon_invitation_celebrate)
        mBinding.ivSubmit.visibility = View.GONE
        mBinding.ivTearOpen.visibility = View.VISIBLE
    }
}