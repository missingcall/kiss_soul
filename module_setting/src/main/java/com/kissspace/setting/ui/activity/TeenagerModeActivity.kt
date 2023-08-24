package com.kissspace.setting.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.activity.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.didi.drouter.annotation.Router
import com.kissspace.util.toast
import com.kissspace.common.base.BaseActivity
import com.kissspace.common.router.jump
import com.kissspace.common.router.parseIntent
import com.kissspace.common.ext.safeClick
import com.kissspace.common.ext.setTitleBarListener
import com.kissspace.common.ext.showSoftInput
import com.kissspace.common.router.RouterPath
import com.kissspace.common.util.mmkv.MMKVProvider
import com.kissspace.common.widget.phonecode.IPhoneCode
import com.kissspace.module_setting.R
import com.kissspace.module_setting.databinding.SettingActivityTeenagerBinding
import com.kissspace.common.http.getUserInfo
import com.kissspace.setting.viewmodel.TeenagerViewModel
import com.kissspace.util.logE
import com.kissspace.util.orZero

/**
 * @Author gaohangbo
 * @Date 2023/2/11 16:14.
 * @Describe 青少年模式 输入密码 确认密码
 */
@Router(path = RouterPath.PATH_TEENAGER_MODE)
class TeenagerModeActivity : com.kissspace.common.base.BaseActivity(R.layout.setting_activity_teenager) {
    private val mBinding by viewBinding<SettingActivityTeenagerBinding>()
    private val mViewModel by viewModels<TeenagerViewModel>()
    private var stepCount by parseIntent<Int>(defaultValue = 0)
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        initView(null)
    }

    override fun initView(savedInstanceState: Bundle?) {
        setTitleBarListener(mBinding.titleBar)
        mBinding.m = mViewModel
        mBinding.lifecycleOwner = this
        mViewModel.mStepCount.value = stepCount
        stepCount = intent?.getIntExtra("stepCount", 0).orZero()
        initCode()
        mViewModel.teenagerHint.value = String.format(resources.getString(R.string.setting_teenager_hint),MMKVProvider.wechatPublicAccount)
        when (mViewModel.mStepCount.value) {
            0 -> {
                mBinding.title.text = "输入密码"
                mBinding.textSubmit.text = "下一步"
                mBinding.tvTips.text = "启动青少年模式，需先设置独立密码"
                mBinding.tvHint.visibility= View.GONE
            }
            1 -> {
                mBinding.title.text = "确认密码"
                mBinding.textSubmit.text = "进入青少年模式"
                mBinding.tvTips.text = "启动青少年模式，需先设置独立密码"
                mBinding.phoneCode.setText("")
                mBinding.tvHint.visibility= View.GONE
            }
            2 -> {
                mBinding.title.text = "已开启青少年模式"
                mBinding.tvTips.text = "关闭青少年模式，请输入独立密码"
                mBinding.textSubmit.text = "关闭青少年模式"
                mBinding.phoneCode.setText("")
                mBinding.titleBar.leftIcon = null
                mBinding.titleBar.setOnTitleBarListener(null)
                mBinding.tvHint.visibility= View.VISIBLE
            }
        }

        mBinding.textSubmit.safeClick {
            if (mViewModel.mStepCount.value == 0) {
                jump(RouterPath.PATH_TEENAGER_MODE, "stepCount" to 1)
            } else if (mViewModel.mStepCount.value == 1) {
                logE("mBinding.phoneCode.editText" + mBinding.phoneCode.editText.text.toString())
                if (mViewModel.stepOnePassword.value == mViewModel.stepTwoPassword.value) {
                    mViewModel.setAdolescentPassword(
                        openAdolescent,
                        mBinding.phoneCode.editText.text.toString()
                    ) { result ->
                        if (result) {
                            getUserInfo(onSuccess = {
                                MMKVProvider.adolescent = it.adolescent
                                jump(RouterPath.PATH_TEENAGER_MODE, "stepCount" to 2)
                            })
                        }
                        logE("青少年模式开启成功")
                    }
                } else {
                    toast("密码不一致")
                }
            } else {
                logE("mBinding.phoneCode.editText" + mBinding.phoneCode.editText.text.toString())
                mViewModel.setAdolescentPassword(
                    closeAdolescent,
                    mBinding.phoneCode.editText.text.toString()
                ) { result ->
                    if (result) {
                        //toast("关闭青少年模式成功")
                        logE("青少年模式关闭成功")
                        getUserInfo(onSuccess = {
                            logE("adolescent"+it.adolescent)
                            MMKVProvider.adolescent = it.adolescent
                            jump(RouterPath.PATH_MAIN, "index" to 0)
                            finish()
                        })
                    }

                }
            }
        }
    }


    override fun onResume() {
        super.onResume()
        mBinding.phoneCode.editText.showSoftInput()
    }

    private fun initCode() {
        mBinding.phoneCode.setOnVCodeCompleteListener(object : com.kissspace.common.widget.phonecode.IPhoneCode.OnVCodeInputListener {
            override fun vCodeComplete(verificationCode: String?) {
                mViewModel.btnEnable.value = verificationCode?.length == 4
                logE("mViewModel.mStepCount.value"+mViewModel.mStepCount.value)
                logE("verificationCode$verificationCode")
                when (mViewModel.mStepCount.value) {
                    0 -> {
                        mViewModel.stepOnePassword.value = verificationCode
                    }
                    1 -> {
                        mViewModel.stepTwoPassword.value = verificationCode
                    }
                    2 -> {
                        mViewModel.stepThirdPassword.value = verificationCode
                    }
                }

            }

            override fun vCodeIncomplete(verificationCode: String?) {
                mViewModel.btnEnable.value = verificationCode?.length == 4
            }
        })
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        return if (event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_DEL || event.keyCode == KeyEvent.KEYCODE_BACK && mViewModel.mStepCount.value != 2) {
            super.dispatchKeyEvent(event)
        } else {
            true
        }
    }

    companion object {
        const val openAdolescent = "001"
        const val closeAdolescent = "002"
    }
}