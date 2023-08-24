package com.kissspace.setting.ui.activity

import android.os.Bundle
import by.kirich1409.viewbindingdelegate.viewBinding
import com.didi.drouter.annotation.Router
import com.kissspace.common.base.BaseActivity
import com.kissspace.common.router.jump
import com.kissspace.common.ext.safeClick
import com.kissspace.common.ext.setTitleBarListener
import com.kissspace.common.router.RouterPath
import com.kissspace.module_setting.R
import com.kissspace.module_setting.databinding.SettingActivityTeenagerDescribeBinding

/**
 * @Author gaohangbo
 * @Date 2023/2/11 16:14.
 * @Describe 青少年模式
 */
@Router(path = RouterPath.PATH_TEENAGER_DESCRIBE)
class TeenagerModeDescribeActivity : com.kissspace.common.base.BaseActivity(R.layout.setting_activity_teenager_describe) {
    private val mBinding by viewBinding<SettingActivityTeenagerDescribeBinding>()

    override fun initView(savedInstanceState: Bundle?) {
        setTitleBarListener(mBinding.titleBar)

        mBinding.tvOpen.safeClick {
            jump(path = RouterPath.PATH_TEENAGER_MODE)
        }
    }
}