package com.kissspace.login.ui

import android.os.Bundle
import androidx.activity.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.blankj.utilcode.util.DeviceUtils
import com.blankj.utilcode.util.ToastUtils
import com.didi.drouter.annotation.Router
import com.kissspace.common.base.BaseActivity
import com.kissspace.common.config.CommonApi
import com.kissspace.common.ext.safeClick
import com.kissspace.common.ext.setTitleBarListener
import com.kissspace.common.model.LoginResultBean
import com.kissspace.common.model.UserAccountBean
import com.kissspace.common.router.RouterPath
import com.kissspace.common.router.jump
import com.kissspace.common.util.customToast
import com.kissspace.common.util.hideLoading
import com.kissspace.common.util.mmkv.MMKVProvider
import com.kissspace.common.util.showLoading
import com.kissspace.login.http.LoginApi
import com.kissspace.login.viewmodel.LoginViewModel
import com.kissspace.module_login.R
import com.kissspace.module_login.databinding.LoginActivityPasswordBinding
import com.kissspace.network.net.Method
import com.kissspace.network.net.request
import com.kissspace.network.result.collectData
import com.kissspace.util.hideKeyboard
import com.kissspace.util.toJson
import com.kissspace.util.toast

@Router(path = RouterPath.PATH_LOGIN_PASSWORD)
class PasswordLoginActivity : BaseActivity(R.layout.login_activity_password) {
    private val mBinding by viewBinding<LoginActivityPasswordBinding>()
    private val mViewModel by viewModels<LoginViewModel>()
    override fun initView(savedInstanceState: Bundle?) {
        setTitleBarListener(mBinding.titleBar)
        mBinding.ivCleanPhoneNumber.safeClick {
            mBinding.editPhoneNumber.setText("")
        }
        mBinding.ivCleanPasswordNew.safeClick {
            mBinding.editNewPassword.setText("")
        }
        mBinding.tvSubmit.safeClick {
            showLoading("登录中")
            val phoneNumber = mBinding.editPhoneNumber.text.toString().trim()
            val password = mBinding.editNewPassword.text.toString().trim()
            if (phoneNumber.isNullOrEmpty()) {
                customToast("请输入手机号")
                return@safeClick
            }
            if (password.isNullOrEmpty()) {
                customToast("请输入密码")
                return@safeClick
            }
            if (phoneNumber.length != 11) {
                customToast("请输入正确的手机号")
                return@safeClick
            }
            val param = mutableMapOf<String, Any?>()
            param["mobile"] = phoneNumber
            param["password"] = password
            request<Boolean>(LoginApi.API_PASSWORD_LOGIN, Method.POST, param, onSuccess = {
                if (it) {
                    mViewModel.requestUserListByPhone(phoneNumber.replace(" ", ""))
                }
            }, onError = {
                hideLoading()
                customToast(it.message)
            })

        }
    }

    override fun createDataObserver() {
        super.createDataObserver()
        collectData(mViewModel.token, onSuccess = {
            hideLoading()
            mViewModel.loginIm(it, onSuccess = {
                finish()
            })
        }, onError = {
            hideLoading()
            customToast("登录失败${it.message}")
        })

        collectData(mViewModel.accounts, onSuccess = {
            hideLoading()
            if (it.size == 1) {
                val userAccountBean = it[0]
                mViewModel.loginByUserId(
                    userAccountBean.userId,
                    userAccountBean.tokenHead,
                    userAccountBean.token
                )
            } else {
                jump(
                    RouterPath.PATH_CHOOSE_ACCOUNT,
                    "accounts" to toJson(it),
                    "phone" to mBinding.editPhoneNumber.text.toString().trim().replace(" ", "")
                )
            }
        }, onError = {
            hideLoading()
        })
    }

    override fun onPause() {
        super.onPause()
        mBinding.editNewPassword.hideKeyboard()
    }


}