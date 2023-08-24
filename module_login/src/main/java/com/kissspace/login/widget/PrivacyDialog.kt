package com.kissspace.login.widget

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import com.kissspace.common.config.Constants
import com.kissspace.common.router.jump
import com.kissspace.common.router.RouterPath
import com.kissspace.common.util.getH5Url
import com.kissspace.common.widget.BaseDialog
import com.kissspace.module_login.R
import com.kissspace.module_login.databinding.LoginDialogPrivacyBinding

/**
 * @Author gaohangbo
 * @Date 2023/2/10 09:45.
 * @Describe
 */
class PrivacyDialog(context: Context, private val block: ((Boolean) -> Unit)) :
    BaseDialog<LoginDialogPrivacyBinding>(context) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCancelable(false)
        setCanceledOnTouchOutside(false)
    }
    override fun getLayoutId(): Int {
        return R.layout.login_dialog_privacy
    }

    override fun initView() {
        val privacyTips: String = context.resources.getString(R.string.privacy_tips)
        val privacyKey1: String = context.resources.getString(R.string.privacy_tips_key1)
        val privacyKey2: String = context.resources.getString(R.string.privacy_tips_key2)
        val privacyKey3: String = context.resources.getString(R.string.privacy_tips_key3)
        val index1 = privacyTips.indexOf(privacyKey1)
        val index2 = privacyTips.indexOf(privacyKey2)
        val index3 = privacyTips.indexOf(privacyKey3)
        //需要显示的字串
        val spannedString = SpannableString(privacyTips)
        //设置点击字体颜色
        val colorSpan1 =
            ForegroundColorSpan(context.resources.getColor(com.kissspace.module_common.R.color.color_FFFD62))
        spannedString.setSpan(
            colorSpan1,
            index1,
            index1 + privacyKey1.length,
            Spannable.SPAN_EXCLUSIVE_INCLUSIVE
        )
        val colorSpan2 =
            ForegroundColorSpan(context.resources.getColor(com.kissspace.module_common.R.color.color_FFFD62))
        spannedString.setSpan(
            colorSpan2,
            index2,
            index2 + privacyKey2.length,
            Spannable.SPAN_EXCLUSIVE_INCLUSIVE
        )
        val colorSpan3 =
            ForegroundColorSpan(context.resources.getColor(com.kissspace.module_common.R.color.color_FFFD62))
        spannedString.setSpan(
            colorSpan3,
            index3,
            index3 + privacyKey3.length,
            Spannable.SPAN_EXCLUSIVE_INCLUSIVE
        )
        //设置点击事件
        val clickableSpan1: ClickableSpan = object : ClickableSpan() {
            override fun onClick(view: View) {
                jump(
                    RouterPath.PATH_WEBVIEW,
                    "url" to getH5Url(Constants.H5.userAgreementUrl),
                    "showTitle" to true
                )
            }

            override fun updateDrawState(ds: TextPaint) {
                //点击事件去掉下划线
                ds.isUnderlineText = false
            }
        }
        spannedString.setSpan(
            clickableSpan1,
            index1,
            index1 + privacyKey1.length,
            Spannable.SPAN_EXCLUSIVE_INCLUSIVE
        )

        val clickableSpan2: ClickableSpan = object : ClickableSpan() {
            override fun onClick(view: View) {
                jump(RouterPath.PATH_WEBVIEW, "url" to getH5Url(Constants.H5.privacyUrl), "showTitle" to true)
            }

            override fun updateDrawState(ds: TextPaint) {
                //点击事件去掉下划线
                ds.isUnderlineText = false
            }
        }
        spannedString.setSpan(
            clickableSpan2,
            index2,
            index2 + privacyKey2.length,
            Spannable.SPAN_EXCLUSIVE_INCLUSIVE
        )

        val clickableSpan3: ClickableSpan = object : ClickableSpan() {
            override fun onClick(view: View) {
                jump(RouterPath.PATH_WEBVIEW, "url" to getH5Url(Constants.H5.threePartyResourcesUrl), "showTitle" to true)
            }

            override fun updateDrawState(ds: TextPaint) {
                //点击事件去掉下划线
                ds.isUnderlineText = false
            }
        }
        spannedString.setSpan(
            clickableSpan3,
            index3,
            index3 + privacyKey3.length,
            Spannable.SPAN_EXCLUSIVE_INCLUSIVE
        )

        //设置点击后的颜色为透明，否则会一直出现高亮
         mBinding.tvPrivacyTips.highlightColor = Color.TRANSPARENT
        //开始响应点击事件
        mBinding.tvPrivacyTips.movementMethod = LinkMovementMethod.getInstance()

        mBinding.tvPrivacyTips.text = spannedString

        mBinding.tvAgree.setOnClickListener {
            block.invoke(true)
            dismiss()
        }

        mBinding.tvNotAgree.setOnClickListener {
            block.invoke(false)
            dismiss()
        }
    }

}