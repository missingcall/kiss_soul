package com.kissspace.setting.ui.activity

import android.os.Bundle
import by.kirich1409.viewbindingdelegate.viewBinding
import com.didi.drouter.annotation.Router
import com.kissspace.common.base.BaseActivity
import com.kissspace.common.ext.setTitleBarListener
import com.kissspace.common.router.RouterPath
import com.kissspace.module_setting.R
import com.kissspace.module_setting.databinding.SettingActivityNotifySettingBinding

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/12/28 14:14
 * @Description: 消息通知设置页面
 *
 */
@Router(path = RouterPath.PATH_MESSAGE_NOTIFY)
class NotifySettingActivity : com.kissspace.common.base.BaseActivity(R.layout.setting_activity_notify_setting) {
    private val mBinding by viewBinding<SettingActivityNotifySettingBinding>()
    override fun initView(savedInstanceState: Bundle?) {

        setTitleBarListener(mBinding.titleBar)
    }
}