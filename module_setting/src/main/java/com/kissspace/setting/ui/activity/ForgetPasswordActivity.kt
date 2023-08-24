package com.kissspace.setting.ui.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.didi.drouter.annotation.Router
import com.github.gzuliyujiang.calendarpicker.core.Interval
import com.kissspace.common.base.BaseActivity
import com.kissspace.common.ext.safeClick
import com.kissspace.common.ext.setTitleBarListener
import com.kissspace.common.http.sendSms
import com.kissspace.common.http.verificationCode
import com.kissspace.common.router.RouterPath
import com.kissspace.common.util.countDown
import com.kissspace.common.util.customToast
import com.kissspace.common.util.loginOut
import com.kissspace.module_setting.R
import com.kissspace.module_setting.databinding.SettingActivityForgetPasswordBinding
import com.kissspace.network.net.Method
import com.kissspace.network.net.request
import com.kissspace.setting.http.SettingApi
import com.kissspace.setting.viewmodel.ForgetPasswordViewModel
import com.kissspace.util.addAfterTextChanged
import com.kissspace.util.logE
import com.kissspace.util.toast
import kotlinx.coroutines.Job

@Router(path = RouterPath.PATH_FORGET_PASSWORD)
class ForgetPasswordActivity : BaseActivity(R.layout.setting_activity_forget_password) {
    private val mBinding by viewBinding<SettingActivityForgetPasswordBinding>()
    private val mViewModel by viewModels<ForgetPasswordViewModel>()
    private var mCountDown: Job? = null
    override fun initView(savedInstanceState: Bundle?) {
        mBinding.m = mViewModel
        setTitleBarListener(mBinding.titleBar)
        mBinding.tvGetCode.safeClick {
            sendSms(mViewModel.phoneNumber.get().toString(), "5") {
                //倒计时60秒
                mCountDown = countDown(60, reverse = false, scope = lifecycleScope, onTick = {
                    mBinding.tvGetCode.text = "${it}s"
                    mViewModel.sendSmsEnable.set(false)
                }, onFinish = {
                    mViewModel.sendSmsEnable.set(true)
                    mBinding.tvGetCode.text = "获取验证码"
                    mCountDown?.cancel()
                })
            }
        }

        mBinding.tvConfirm.safeClick {
            if (mViewModel.phoneNumber.get().toString().isNullOrEmpty()) {
                toast("请输入手机号")
                return@safeClick
            }
            if (mViewModel.verificationCode.get().toString().isNullOrEmpty()) {
                toast("请输入验证码")
                return@safeClick
            }
            if (mViewModel.password.get().toString().isNullOrEmpty()) {
                toast("请输入密码")
                return@safeClick
            }
            if (mViewModel.confirmPwd.get().toString().isNullOrEmpty()) {
                toast("请确认密码")
                return@safeClick
            }
            if (mViewModel.confirmPwd.get().toString() != mViewModel.confirmPwd.get().toString()) {
                toast("两次密码输入不一致")
                return@safeClick
            }
            verificationCode(
                mBinding.etPhoneNumber.text.toString(), mBinding.etCode.text.toString(), "5"
            ) {
                if (it) {
                    updatePwd()
                }
            }
        }
    }

    private fun updatePwd() {
        val param = mutableMapOf<String, Any?>("password" to mViewModel.password.get().toString())
        request<Int>(SettingApi.API_RESET_LOGIN_PASSWORD, Method.POST, param, onSuccess = {
            customToast("设置成功")
            finish()
        }, onError = {
            customToast(it.errorMsg)
        })
    }
}