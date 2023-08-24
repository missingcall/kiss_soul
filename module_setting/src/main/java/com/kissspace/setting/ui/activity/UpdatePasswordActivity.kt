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
import com.kissspace.module_setting.databinding.SettingActivityUpdateLoginPasswordBinding
import com.kissspace.network.net.Method
import com.kissspace.network.net.request
import com.kissspace.setting.http.SettingApi
import com.kissspace.util.hideKeyboard

@Router(path = RouterPath.PATH_UPDATE_LOGIN_PASSWORD)
class UpdatePasswordActivity : BaseActivity(R.layout.setting_activity_update_login_password) {
    private val mBinding by viewBinding<SettingActivityUpdateLoginPasswordBinding>()
    override fun initView(savedInstanceState: Bundle?) {
        setTitleBarListener(mBinding.titleBar)
        mBinding.ivCleanPasswordOld.safeClick {
            mBinding.editOldPassword.setText("")
        }
        mBinding.ivCleanPasswordNew.safeClick {
            mBinding.editNewPassword.setText("")
        }
        mBinding.tvSubmit.safeClick {
            val oldPassword = mBinding.editOldPassword.text.toString().trim()
            val newPassword = mBinding.editNewPassword.text.toString().trim()
            if (oldPassword.length < 6) {
                customToast("密码最少6位")
                return@safeClick
            }
            if (oldPassword.length > 32) {
                customToast("密码最长32位")
                return@safeClick
            }
            if (oldPassword == newPassword) {
                customToast("两次密码一致，请重新输入")
                return@safeClick
            }

            val params = mutableMapOf<String, Any?>()
            params["oldPassword"] = oldPassword
            params["newPassword"] = newPassword
            request<Int>(SettingApi.API_UPDATE_LOGIN_PASSWORD, Method.POST, params, onSuccess = {
                mBinding.editOldPassword.hideKeyboard()
                customToast("密码修改成功")
                finish()
            }, onError = {
                customToast(it.message)
            })
        }
    }

    override fun onPause() {
        super.onPause()
        mBinding.editOldPassword.hideKeyboard()
    }

}