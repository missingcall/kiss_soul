package com.kissspace.setting.ui.activity

import android.os.Bundle
import by.kirich1409.viewbindingdelegate.viewBinding
import com.didi.drouter.annotation.Router
import com.kissspace.common.base.BaseActivity
import com.kissspace.common.ext.safeClick
import com.kissspace.common.ext.setTitleBarListener
import com.kissspace.common.router.RouterPath
import com.kissspace.common.util.customToast
import com.kissspace.module_setting.R
import com.kissspace.module_setting.databinding.SettingActivityLoginPasswordBinding
import com.kissspace.network.net.Method
import com.kissspace.network.net.request
import com.kissspace.setting.http.SettingApi
import com.kissspace.util.hideKeyboard
import com.qmuiteam.qmui.kotlin.onClick

@Router(path = RouterPath.PATH_SETTING_LOGIN_PASSWORD)
class SettingLoginPasswordActivity : BaseActivity(R.layout.setting_activity_login_password) {
    private val mBinding by viewBinding<SettingActivityLoginPasswordBinding>()
    override fun initView(savedInstanceState: Bundle?) {
        setTitleBarListener(mBinding.titleBar)
        mBinding.ivCleanPasswordOld.safeClick {
            mBinding.editOldPassword.setText("")
        }
        mBinding.ivCleanPasswordNew.safeClick {
            mBinding.editNewPassword.setText("")
        }
        mBinding.tvSubmit.onClick {
            val firstPwd = mBinding.editOldPassword.text.toString().trim()
            val secondPwd = mBinding.editNewPassword.text.toString().trim()
            if (firstPwd.isNullOrEmpty() || secondPwd.isNullOrEmpty()) {
                customToast("请输入密码")
                return@onClick
            }
            if (firstPwd.length < 6) {
                customToast("密码最短6位")
                return@onClick
            }
            if (firstPwd.length > 32) {
                customToast("密码最长32位")
                return@onClick
            }
            if (firstPwd != secondPwd) {
                customToast("两次密码输入不一致")
                return@onClick
            }
            val param = mutableMapOf<String, Any?>("password" to secondPwd)
            request<Int>(
                SettingApi.API_SETTING_LOGIN_PASSWORD,
                Method.POST,
                param,
                onSuccess = {
                    customToast("设置成功")
                    finish()
                },
                onError = {
                    customToast(it.message)
                })
        }
    }

    override fun onPause() {
        super.onPause()
        mBinding.editOldPassword.hideKeyboard()
    }
}