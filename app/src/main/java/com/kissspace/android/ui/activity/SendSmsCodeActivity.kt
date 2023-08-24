package com.kissspace.android.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.didi.drouter.annotation.Router
import com.kissspace.util.toast
import com.kissspace.android.R
import com.kissspace.android.databinding.AppLayoutBindPhoneNumberBinding
import com.kissspace.common.ext.*
import com.kissspace.common.router.RouterPath
import com.kissspace.common.util.*
import com.kissspace.android.viewmodel.SendSmsViewModel
import com.kissspace.common.callback.ActivityResult.BindPhone
import com.kissspace.common.callback.ActivityResult.CancelAccount
import com.kissspace.common.callback.ActivityResult.TransferAccount
import com.kissspace.common.config.Constants
import com.kissspace.common.http.sendSms
import com.kissspace.common.http.verificationCode
import com.kissspace.common.router.jump
import com.kissspace.common.router.parseIntent
import com.kissspace.common.util.mmkv.MMKVProvider
import com.kissspace.util.addAfterTextChanged
import kotlinx.coroutines.Job
import com.kissspace.util.logE
import com.kissspace.util.orZero

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/12/28 13:48
 * @Description: 发送验证码页面
 *
 */
@Router(path = RouterPath.PATH_SEND_SMS_CODE)
class SendSmsCodeActivity :
    com.kissspace.common.base.BaseActivity(R.layout.app_layout_bind_phone_number) {

    private val mBinding by viewBinding<AppLayoutBindPhoneNumberBinding>()

    private val mViewModel by viewModels<SendSmsViewModel>()

    var mCountDown: Job? = null

    private var type: String by parseIntent(defaultValue = Constants.sendSmsType?.second.orEmpty())
    private val transferCoin by parseIntent<Double>(defaultValue = 0.0)

    private var smsType: String? = null

    private var bindPhoneStepCount: Int? = null

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        initView(null)
    }

    override fun initView(savedInstanceState: Bundle?) {
        setTitleBarListener(mBinding.titleBar)
        mBinding.m = mViewModel
        mBinding.lifecycleOwner = this
        bindPhoneStepCount = intent?.getIntExtra("bindPhoneStepCount", 0).orZero()
        mBinding.etPhoneNumber.addAfterTextChanged {
            mViewModel.phoneNumber.value = it.toString()
        }
        mBinding.etCode.addAfterTextChanged {
            mViewModel.verificationCode.value = it.toString()
        }
        when (type) {
            CancelAccount -> {
                smsType = "4"
                mBinding.tvConfirm.text = "注销"
                mBinding.etPhoneNumber.isEnabled = false
                mBinding.etPhoneNumber.setText(MMKVProvider.userPhone)
                mBinding.titleBar.setTitleIcon(com.kissspace.module_common.R.mipmap.common_icon_title_logoff)
            }

            BindPhone -> {
                if (bindPhoneStepCount == 0) {
                    smsType = "11"
                    mBinding.etPhoneNumber.setText(MMKVProvider.userPhone)
                    mBinding.etPhoneNumber.isEnabled = false
                } else {
                    smsType = "6"
                    mBinding.etPhoneNumber.setText("")
                    mBinding.etCode.setText("")
                    mBinding.etPhoneNumber.isEnabled = true
                    mBinding.llRoot.requestFocus()
                    finishCountDown()
                }
                mBinding.titleBar.setTitleIcon(R.mipmap.app_icon_title_bind_phone)
                mBinding.tvConfirm.text = "确定"
            }

            TransferAccount -> {
                smsType = "14"
                mBinding.etPhoneNumber.setText(MMKVProvider.userPhone)
                mBinding.etPhoneNumber.isEnabled = false
                mBinding.titleBar.setTitleIcon(R.mipmap.app_icon_title_transfer_coin)
            }

            else -> {
                mBinding.tvConfirm.text = "确定"
            }
        }
        mBinding.tvGetCode.safeClick {
            sendSms(mViewModel.phoneNumber.value.toString(), smsType.orEmpty()) {
                //倒计时60秒
                mCountDown = countDown(60, reverse = false, scope = lifecycleScope, onTick = {
                    mBinding.tvGetCode.text = "${it}s"
                    mViewModel.sendSmsEnable.value = false
                    logE("mBinding.tvGetCode.text$it")
                }, onFinish = {
                    finishCountDown()
                })
            }
        }

        mBinding.tvConfirm.safeClick {
            when (type) {
                CancelAccount -> {
                    verificationCode(
                        mBinding.etPhoneNumber.text.toString(),
                        mBinding.etCode.text.toString(),
                        smsType.orEmpty()
                    ) {
                        mViewModel.cancelAccount(
                            mBinding.etPhoneNumber.text.toString(),
                            mBinding.etCode.text.toString()
                        ) {
                            toast("注销成功")
                            finish()
                            loginOut()
                        }
                    }
                }

                BindPhone -> {
                    if (bindPhoneStepCount == 0) {
                        verificationCode(
                            mBinding.etPhoneNumber.text.toString(),
                            mBinding.etCode.text.toString(),
                            smsType.orEmpty()
                        ) {
                            jump(
                                RouterPath.PATH_SEND_SMS_CODE,
                                "bindPhoneStepCount" to 1,
                                "type" to BindPhone
                            )
                        }
                    } else {
                        mViewModel.bindPhoneNumber(
                            mBinding.etPhoneNumber.text.toString(),
                            mBinding.etCode.text.toString()
                        ) {
                            toast("绑定成功")
                            setResult(Activity.RESULT_OK)
                            finish()
                        }
                    }
                }

                TransferAccount -> {
                    mViewModel.transferVerification(
                        mBinding.etPhoneNumber.text.toString(),
                        mBinding.etCode.text.toString(),
                        smsType.orEmpty()
                    ) {
                        setResult(Activity.RESULT_OK)
                        finish()
                    }
                }
            }
        }
    }

    private fun finishCountDown() {
        mViewModel.sendSmsEnable.value = true
        mBinding.tvGetCode.text = "获取验证码"
        mCountDown?.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        mCountDown?.cancel()
    }
}