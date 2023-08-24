package com.kissspace.setting.ui.activity

import android.os.Bundle
import by.kirich1409.viewbindingdelegate.viewBinding
import com.didi.drouter.annotation.Router
import com.kissspace.common.base.BaseActivity
import com.kissspace.common.ext.setTitleBarListener
import com.kissspace.common.router.RouterPath
import com.kissspace.common.util.mmkv.MMKVProvider
import com.kissspace.module_setting.R
import com.kissspace.module_setting.databinding.SettingActivityIdentityAuthsuccessBinding

/**
 * @Author gaohangbo
 * @Date 2023/2/18 16:56.
 * @Describe 身份认证成功页面
 */
@Router(uri = RouterPath.PATH_USER_IDENTITY_AUTH_SUCCESS)
class IdentityAuthSuccessActivity: com.kissspace.common.base.BaseActivity(R.layout.setting_activity_identity_authsuccess){

    private val mBinding by viewBinding<SettingActivityIdentityAuthsuccessBinding>()
    override fun initView(savedInstanceState: Bundle?) {
        setTitleBarListener(mBinding.titleBar)
        mBinding.tvUserName.text = MMKVProvider.fullName
        mBinding.tvNumber.text= MMKVProvider.idNumber
    }
}