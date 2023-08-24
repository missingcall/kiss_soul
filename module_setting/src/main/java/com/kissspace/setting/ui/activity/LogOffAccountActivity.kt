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
import com.kissspace.module_setting.databinding.SettingActivityLogOffAccountBinding
import com.kissspace.common.callback.ActivityResult.CancelAccount

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/12/28 13:55
 * @Description: 注销账号页面
 *
 */
@Router(path = RouterPath.PATH_LOG_OFF_ACCOUNT)
class LogOffAccountActivity : com.kissspace.common.base.BaseActivity(R.layout.setting_activity_log_off_account){
    private val mBinding by viewBinding<SettingActivityLogOffAccountBinding>()
    override fun initView(savedInstanceState: Bundle?) {
        setTitleBarListener(mBinding.titleBar)
        mBinding.tvSubmit.safeClick {
          jump(RouterPath.PATH_SEND_SMS_CODE,"type" to CancelAccount)
        }
    }
}