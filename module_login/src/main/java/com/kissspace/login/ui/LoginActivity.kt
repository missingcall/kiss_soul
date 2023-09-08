package com.kissspace.login.ui

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.text.TextUtils
import androidx.activity.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.blankj.utilcode.util.ToastUtils
import com.didi.drouter.annotation.Router
import com.drake.net.NetConfig
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.SDKOptions
import com.netease.nimlib.sdk.auth.LoginInfo
import com.kissspace.common.base.BaseActivity
import com.kissspace.common.config.*
import com.kissspace.common.ext.safeClick
import com.kissspace.common.flowbus.Event
import com.kissspace.common.flowbus.FlowBus
import com.kissspace.common.router.RouterPath
import com.kissspace.common.router.jump
import com.kissspace.common.util.*
import com.kissspace.common.util.mmkv.MMKVProvider
import com.kissspace.common.util.mmkv.isLogin
import com.kissspace.login.umeng.QuickLoginManager
import com.kissspace.login.umeng.callback.QuickLoginCallback
import com.kissspace.login.viewmodel.LoginViewModel
import com.kissspace.login.widget.PrivacyDialog
import com.kissspace.module_login.R
import com.kissspace.module_login.databinding.LoginActivityLoginBinding
import com.kissspace.network.result.collectData
import com.kissspace.util.finishAllActivitiesExceptNewest
import com.kissspace.util.immersiveStatusBar
import com.kissspace.util.isAppDebug
import com.kissspace.util.logE
import com.kissspace.util.toJson
import com.qmuiteam.qmui.kotlin.onClick
import java.util.*


@Router(path = RouterPath.PATH_LOGIN)
class LoginActivity : BaseActivity(R.layout.login_activity_login),
    QuickLoginCallback {
    private val mBinding by viewBinding<LoginActivityLoginBinding>()
    private val mViewModel by viewModels<LoginViewModel>()
    private var sdkAvailable = true
    private var dialog: Dialog? = null
    private val mQuickLoginManager by lazy {
        QuickLoginManager()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NetConfig.host = BaseUrlConfig.BASEURL_RELEASE
        finishAllActivitiesExceptNewest()
    }


    @SuppressLint("ResourceAsColor")
    override fun initView(savedInstanceState: Bundle?) {
        immersiveStatusBar(true)
        mBinding.vm = mViewModel
        initAgreement()
        mBinding.tvLogin.setOnClickListener {
            if (mBinding.cbAgree.isChecked) {
                jump(RouterPath.PATH_LOGIN_PHONE_CODE)
            } else {
                customToast("请勾选同意后登录")
            }
        }
        mBinding.tvLoginPassword.onClick {
            if (mBinding.cbAgree.isChecked) {
                jump(RouterPath.PATH_LOGIN_PASSWORD)
            } else {
                customToast("请勾选同意后登录")
            }
        }
//        if (isAppDebug) {
//            mBinding.ivLogo.setOnLongClickListener {
//                jump(RouterPath.PATH_BASE_URL_SETTING)
//                true
//            }
//        }
        dispatchPath()
    }


    /**
     * 青少年、是否编辑资料、是否第一次登录
     */
    private fun dispatchPath() {
        if (!MMKVProvider.isAgreeProtocol) {
            showPrivacyDialog()
        } else if (MMKVProvider.adolescent) {
            jump(RouterPath.PATH_TEENAGER_MODE, "stepCount" to 2)
        } else if (isLogin()) {
            if (MMKVProvider.isEditProfile) {
                jump(RouterPath.PATH_MAIN)
            } else {
                jump(RouterPath.PATH_LOGIN_EDIT_PROFILE)
            }
        }
    }

    private fun showPrivacyDialog() {
        if (dialog == null) {
            dialog = PrivacyDialog(this@LoginActivity) { agree ->
                if (agree) {
                    FlowBus.post(Event.InitApplicationTaskEvent)
                    MMKVProvider.isAgreeProtocol = true
                    mBinding.cbAgree.isChecked = true
                    mViewModel.isAgree.set(true)
                } else {
                    MMKVProvider.isAgreeProtocol = false
                    exitApp()
                }
            }
        }
        if (!this@LoginActivity.isFinishing && dialog?.isShowing == false) {
            dialog?.show()
        }

    }


    private fun initAgreement() {
        mBinding.cbAgree.setOnCheckedChangeListener { _, isChecked ->
            mViewModel.isAgree.set(isChecked)
        }
        mBinding.tvUserProtocol.safeClick {
            jump(
                RouterPath.PATH_WEBVIEW,
                "url" to getH5Url(Constants.H5.userAgreementUrl),
                "showTitle" to true
            )
        }
        mBinding.tvPrivacyProtocol.safeClick {
            jump(
                RouterPath.PATH_WEBVIEW,
                "url" to getH5Url(Constants.H5.privacyUrl),
                "showTitle" to true
            )
        }
    }

    override fun createDataObserver() {
        super.createDataObserver()
        collectData(mViewModel.token, onSuccess = {
            mViewModel.loginIm(it, onSuccess = {
                finish()
            }, onError = {

            })
        }, onError = {
            hideLoading()
            ToastUtils.showLong("登录失败${it.errorMsg}")
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
                    "phone" to it[0].phone
                )
                finish()
            }
        }, onError = {
            hideLoading()
        })
    }


    override fun initializationSuccess() {
        sdkAvailable = true
    }

    override fun onQuickLoginSuccess(token: String) {
        mViewModel.requestUserListByUMeng(token)
        mQuickLoginManager.quitLogin()
    }

    override fun onQuickLoginFail() {
        hideLoading()
        sdkAvailable = false
        jump(RouterPath.PATH_LOGIN_PHONE_CODE)
    }

    override fun onDestroy() {
        super.onDestroy()
        mQuickLoginManager.release()
    }
}