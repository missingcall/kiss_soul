package com.kissspace.login.ui

import android.os.Bundle
import androidx.activity.viewModels
import com.didi.drouter.annotation.Router
import com.kissspace.common.base.BaseActivity
import com.kissspace.common.binding.dataBinding
import com.kissspace.common.ext.*
import com.kissspace.common.http.sendSms
import com.kissspace.common.router.RouterPath
import com.kissspace.common.router.RouterPath.PATH_INPUT_SMS_CODE
import com.kissspace.common.router.jump
import com.kissspace.login.viewmodel.LoginViewModel
import com.kissspace.module_login.R
import com.kissspace.module_login.databinding.LoginActivityPhoneCodeLoginBinding
import com.kissspace.util.addAfterTextChanged

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/11/17
 * @Description: 手机验证码登录页面
 *
 */
@Router(path = RouterPath.PATH_LOGIN_PHONE_CODE)
class PhoneCodeLoginActivity : BaseActivity(R.layout.login_activity_phone_code_login) {
    private val mBinding by dataBinding<LoginActivityPhoneCodeLoginBinding>()
    private val mViewModel by viewModels<LoginViewModel>()
    override fun initView(savedInstanceState: Bundle?) {
        setTitleBarListener(mBinding.titleBar)
        mBinding.vm = mViewModel
        mBinding.ivClear.setOnClickListener {
            mBinding.editPhoneNumber.setText("")
        }
        mBinding.editPhoneNumber.setSeparator(" ")
        mBinding.editPhoneNumber.setPattern(intArrayOf(3, 4, 4))
        mBinding.editPhoneNumber.addAfterTextChanged {
            mViewModel.phoneIconState.set(it?.isNotEmpty())
            mViewModel.getCodeBtnState.set(it?.length == 13)
        }
        mBinding.textSubmit.safeClick {
            if (mViewModel.getCodeBtnState.get()!!) {
                val phoneNumber = mBinding.editPhoneNumber.text.toString().trim()
                sendSms(phoneNumber.replace(" ", ""), "2") {
                    jump(PATH_INPUT_SMS_CODE, "phoneNumber" to phoneNumber, "countDownTime" to 60)
                }
            }
        }



    }

}