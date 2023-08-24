package com.kissspace.setting.ui.activity

import android.os.Bundle
import by.kirich1409.viewbindingdelegate.viewBinding
import com.didi.drouter.annotation.Router
import com.kissspace.util.activityresult.registerForStartActivityResult
import com.kissspace.common.base.BaseActivity
import com.kissspace.common.callback.ActivityResult.BindPhone
import com.kissspace.common.ext.*
import com.kissspace.common.router.RouterPath
import com.kissspace.common.util.mmkv.MMKVProvider
import com.kissspace.common.widget.CommonHintDialog
import com.kissspace.module_setting.R
import com.kissspace.module_setting.databinding.SettingActivityAccountSafeBinding
import com.kissspace.common.http.getUserInfo
import com.kissspace.common.router.jump
import com.kissspace.util.logE

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/12/28 11:39
 * @Description: 身份认证activity
 *
 */
@Router(path = RouterPath.PATH_ACCOUNT)
class AccountSafeActivity : BaseActivity(R.layout.setting_activity_account_safe) {
    private val mBinding by viewBinding<SettingActivityAccountSafeBinding>()
    override fun initView(savedInstanceState: Bundle?) {
        setTitleBarListener(mBinding.titleBar)
        mBinding.tvAccount.text = MMKVProvider.userPhone
        mBinding.layoutPhoneNumber.safeClick {
            CommonHintDialog.newInstance(
                "手机号绑定",
                "已绑定手机号：${MMKVProvider.userPhone}",
                isShowButton = true
            ).apply {
                this.callBack = {
                    jump(
                        RouterPath.PATH_SEND_SMS_CODE,
                        "type" to BindPhone,
                        activity = activity,
                        resultLauncher = startActivityLauncher
                    )
                    dismiss()
                }
                this.show(supportFragmentManager)
            }
        }

        mBinding.layoutLogOff.safeClick {
            jump(RouterPath.PATH_LOG_OFF_ACCOUNT)
        }

        mBinding.layoutSettingPassword.safeClick {
            getUserInfo(onSuccess = {
                if (it.isSetPassword) {
                    jump(RouterPath.PATH_UPDATE_LOGIN_PASSWORD)
                } else {
                    jump(RouterPath.PATH_SETTING_LOGIN_PASSWORD)
                }
            })
        }

        mBinding.layoutResetPwd.safeClick {
            jump(RouterPath.PATH_FORGET_PASSWORD)
        }
    }

    private val startActivityLauncher = registerForStartActivityResult { result ->
        logE("result.resultCode" + result.resultCode)
        if (result.resultCode == RESULT_OK) {
            //重新请求用户信息，刷新用户
            getUserInfo(onSuccess = { userinfo ->
                mBinding.tvAccount.text = MMKVProvider.userPhone
            })
        }
    }
}