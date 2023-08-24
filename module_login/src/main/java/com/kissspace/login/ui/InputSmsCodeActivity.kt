package com.kissspace.login.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.ToastUtils
import com.didi.drouter.annotation.Router
import com.kissspace.common.binding.dataBinding
import com.kissspace.common.ext.*
import com.kissspace.common.http.sendSms
import com.kissspace.common.http.verificationCode
import com.kissspace.common.router.RouterPath
import com.kissspace.common.router.jump
import com.kissspace.common.router.parseIntent
import com.kissspace.common.util.countDown
import com.kissspace.common.util.hideLoading
import com.kissspace.common.util.showLoading
import com.kissspace.common.util.customToast
import com.kissspace.login.viewmodel.LoginViewModel
import com.kissspace.module_login.R
import com.kissspace.module_login.databinding.LoginActivityInputCodeBinding
import com.kissspace.network.result.collectData
import kotlinx.coroutines.Job
import com.kissspace.util.toJson

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/11/17
 * @Description: 输入验证码界面
 *
 */
@Router(path = RouterPath.PATH_INPUT_SMS_CODE)
class InputSmsCodeActivity : com.kissspace.common.base.BaseActivity(R.layout.login_activity_input_code) {
    private val phoneNumber: String by parseIntent()
    private val countDownTime: Int by parseIntent()
    private val mBinding by dataBinding<LoginActivityInputCodeBinding>()
    private val mViewModel by viewModels<LoginViewModel>()
    var mCountDown: Job? = null
    override fun initView(savedInstanceState: Bundle?) {
        setTitleBarListener(mBinding.titleBar)
        mBinding.vm = mViewModel
        initCode()
        mBinding.tvTips.text =
            StringUtils.getString(R.string.login_input_sms_code_tips, phoneNumber)
        mBinding.textSubmit.safeClick {
            if(mBinding.phoneCode.editText.text.toString().isEmpty()){
                customToast("请输入验证码")
                return@safeClick
            }
            showLoading("正在登录")
            verificationCode(phoneNumber.replace(" ", ""),mBinding.phoneCode.editText.text.toString(),"2", onError = {
                hideLoading()
            }){
                mViewModel.requestUserListByPhone(phoneNumber.replace(" ", ""))
            }
        }
        startCountDown(countDownTime.toLong())
        mBinding.tvGetCode.safeClick {
            sendSms(phoneNumber.replace(" ", ""), "2") {
                //倒计时60秒
                startCountDown(60)
            }
        }
    }

    private fun startCountDown(countDownTime: Long) {
        mCountDown = countDown(countDownTime, reverse = false, scope = lifecycleScope, onTick = {
            mBinding.tvGetCode.text = "重新发送 (${it}s)"
            mViewModel.sendSmsEnable.set(false)
        }, onFinish = {
            mViewModel.sendSmsEnable.set(true)
            mBinding.tvGetCode.text = "重新获取"
            mCountDown?.cancel()
        })
    }

    private fun initCode() {
        mBinding.phoneCode.setOnVCodeCompleteListener(object : com.kissspace.common.widget.phonecode.IPhoneCode.OnVCodeInputListener {
            override fun vCodeComplete(verificationCode: String?) {
                mViewModel.btnEnable.set(verificationCode?.length == 4)
            }

            override fun vCodeIncomplete(verificationCode: String?) {
                mViewModel.btnEnable.set(verificationCode?.length == 4)
            }
        })
    }

    override fun onResume() {
        super.onResume()
        mBinding.phoneCode.editText.showSoftInput()
    }

    override fun createDataObserver() {
        super.createDataObserver()
        collectData(mViewModel.token, onSuccess = {
            hideLoading()
            mViewModel.loginIm(it,onSuccess = {
                finish()
            })
        }, onError = {
            hideLoading()
            ToastUtils.showLong("登录失败${it.errorMsg}")
        })

        collectData(mViewModel.accounts, onSuccess = {
            hideLoading()
            if (it.size == 1){
                val userAccountBean = it[0]
                mViewModel.loginByUserId(userAccountBean.userId,userAccountBean.tokenHead,userAccountBean.token)
            }else {
                jump(RouterPath.PATH_CHOOSE_ACCOUNT,"accounts" to toJson(it),"phone" to phoneNumber.replace(" ", ""))
            }
        }, onError = {
            hideLoading()
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        mCountDown?.cancel()
    }

}



