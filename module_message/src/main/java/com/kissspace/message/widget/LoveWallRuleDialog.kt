package com.kissspace.message.widget

import android.view.Gravity
import androidx.fragment.app.FragmentManager
import com.blankj.utilcode.util.StringUtils
import com.kissspace.common.config.AppConfigKey
import com.kissspace.common.http.getAppConfigByKey
import com.kissspace.common.widget.BaseDialogFragment
import com.kissspace.module_message.R
import com.kissspace.module_message.databinding.MessageDialogLoveWallTipsBinding

class LoveWallRuleDialog : BaseDialogFragment<MessageDialogLoveWallTipsBinding>(Gravity.CENTER) {
    override fun getLayoutId() = R.layout.message_dialog_love_wall_tips

    override fun initView() {
        mBinding.tvClose.setOnClickListener { dismiss() }

        getAppConfigByKey<Int>(AppConfigKey.KEY_LOVE_WALL_RULE_NEWEST) {
            mBinding.tvNewest.text = StringUtils.getString(R.string.message_love_wall_tips_newest, it)
        }
        getAppConfigByKey<Int>(AppConfigKey.KEY_LOVE_WALL_RULE_RICHEST) {
            mBinding.tvRichest.text = StringUtils.getString(R.string.message_love_wall_tips_richest, it)
        }

    }

}