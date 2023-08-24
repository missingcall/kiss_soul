package com.kissspace.mine.ui.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.didi.drouter.annotation.Router
import com.kissspace.util.toast
import com.kissspace.common.base.BaseActivity
import com.kissspace.common.router.jump
import com.kissspace.common.router.parseIntent
import com.kissspace.common.ext.safeClick
import com.kissspace.common.ext.setTitleBarListener
import com.kissspace.common.router.RouterPath
import com.kissspace.util.addAfterTextChanged
import com.kissspace.common.util.countDown
import com.kissspace.mine.util.SoftKeyInputHidWidget
import com.kissspace.mine.viewmodel.BindAlipayViewModel
import com.kissspace.module_mine.R
import com.kissspace.module_mine.databinding.MineActivityBindAlipayBinding
import com.kissspace.common.http.getUserInfo
import com.kissspace.common.http.sendSms
import com.kissspace.common.http.verificationCode
import kotlinx.coroutines.Job
import com.kissspace.util.logE

/**
 * @Author gaohangbo
 * @Date 2023/1/2 17:22.
 * @Describe 支付宝绑定页面
 */
@Router(uri = RouterPath.PATH_BIND_ALIPAY)
class BindAliPayActivity : com.kissspace.common.base.BaseActivity(R.layout.mine_activity_bind_alipay) {
    private val mBinding by viewBinding<MineActivityBindAlipayBinding>()
    private val mViewModel by viewModels<BindAlipayViewModel>()
    private var mCountDown: Job? = null
    private var isH5BindAlipay by parseIntent<Boolean>()

    override fun initView(savedInstanceState: Bundle?) {
        SoftKeyInputHidWidget.assistActivity(this)
        setTitleBarListener(mBinding.titleBar) {
            if (isH5BindAlipay) {
                jump(RouterPath.PATH_MAIN, "index" to 0)
            } else {
                finish()
            }
        }
        mBinding.m = mViewModel
        mBinding.lifecycleOwner = this
        getUserInfo(onSuccess = {
            mViewModel.userName.value = it.fullName
            mViewModel.userIdCardNumber.value = it.idNumber
            mViewModel.userPhone.value = it.mobile
        })

        mBinding.etAlipayAccount.addAfterTextChanged {
            mViewModel.alipayAccount.value = it.toString()
        }
        mBinding.etVerifyCode.addAfterTextChanged {
            mViewModel.verificationCode.value = it.toString()
        }
        mBinding.tvBind.safeClick {
            verificationCode(
                mViewModel.userPhone.value.orEmpty(),
                mViewModel.verificationCode.value.orEmpty(),
                "15"
            ) {
                mViewModel.bindAlipay(
                    mViewModel.alipayAccount.value
                ) {
                    toast("绑定支付宝成功")
                    setResult(
                        RESULT_OK, intent.putExtra(
                            "result",
                            BindAliPayActivityResult
                        )
                    )
                    finish()
                }

            }
        }

        mBinding.tvGetCode.safeClick {
            sendSms(mViewModel.userPhone.value.orEmpty(), "15") {
                mCountDown = countDown(60, reverse = false, scope = lifecycleScope, onTick = {
                    mBinding.tvGetCode.text = "${it}s"
                    mViewModel.sendSmsEnable.value = false
                    logE("mBinding.tvGetCode.text$it")
                }, onFinish = {
                    mViewModel.sendSmsEnable.value = true
                    mBinding.tvGetCode.text = "获取验证码"
                    mCountDown?.cancel()
                })
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mCountDown?.cancel()
    }

    companion object {
        const val BindAliPayActivityResult = "BindAliPayActivityResult"
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (isH5BindAlipay) {
            jump(RouterPath.PATH_MAIN, "index" to 0)
        }
    }

}