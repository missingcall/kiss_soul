package com.kissspace.setting.viewmodel

import com.blankj.utilcode.util.DeviceUtils
import com.blankj.utilcode.util.LogUtils
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.RequestCallback
import com.netease.nimlib.sdk.auth.AuthService
import com.netease.nimlib.sdk.auth.LoginInfo
import com.kissspace.common.base.BaseViewModel
import com.kissspace.common.config.CommonApi
import com.kissspace.common.config.UserConfig
import com.kissspace.common.http.getUserInfo
import com.kissspace.common.model.CreateUserAccountBean
import com.kissspace.common.model.LoginResultBean
import com.kissspace.common.model.UserAccountBean
import com.kissspace.common.model.UserInfoBean
import com.kissspace.common.router.RouterPath
import com.kissspace.common.router.jump
import com.kissspace.common.util.customToast
import com.kissspace.common.util.hideLoading
import com.kissspace.common.util.loginOut
import com.kissspace.common.util.mmkv.MMKVProvider
import com.kissspace.common.util.mmkv.clearMMKV
import com.kissspace.network.net.Method
import com.kissspace.network.net.request
import com.kissspace.network.result.ResultState
import com.kissspace.util.finishAllActivitiesExceptNewest
import com.kissspace.util.logE
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class ChangeAccountViewModel : BaseViewModel() {

    private val _token = MutableSharedFlow<ResultState<LoginResultBean>>()
    val token = _token.asSharedFlow()
    private val _userInfo = MutableSharedFlow<ResultState<UserInfoBean>>()
    val userInfo = _userInfo.asSharedFlow()
    private val _accouts = MutableSharedFlow<ResultState<List<UserAccountBean>>>()
    val accounts = _accouts.asSharedFlow()
    private val _createAccouts = MutableSharedFlow<ResultState<CreateUserAccountBean>>()
    val createAccounts = _createAccouts.asSharedFlow()


    fun requestUserListByPhone(phoneNumber: String){
        val params = mutableMapOf<String,Any?>()
        params["phone"] = phoneNumber
        params["machineCode"] = DeviceUtils.getUniqueDeviceId()
        params["accessFlags"] = 0
        params["switchAccounts"] = true
        request(CommonApi.API_USER_LIST_BY_PHONE, Method.POST,param = params,state = _accouts)
    }


    fun createAccount(userPhone:String){
        val params = mutableMapOf<String,Any?>()
        params["phone"] = userPhone
        params["machineCode"] = DeviceUtils.getUniqueDeviceId()
        params["accessFlags"] = 0
        params["appleId"] = ""
        request(CommonApi.API_USER_ACCOUNT_CREATE, Method.POST,param = params,state = _createAccouts)
    }

    fun loginByUserId(userId: String){
        val params = mutableMapOf<String,Any?>()
        params["userId"] = userId
        params["loginType"] = "002"
        params["machineCode"] = DeviceUtils.getUniqueDeviceId()
        request(CommonApi.API_LOGIN_BY_USERID, Method.POST,param = params,state = _token)
    }


    fun loginIm(
        loginResult: LoginResultBean,
        onSuccess: (() -> Unit?)? = null,
        onError: (() -> Unit?)? = null
    ) {
        MMKVProvider.accId=loginResult.accId
        val info = LoginInfo(loginResult.accId, loginResult.netEaseToken)
        NIMClient.getService(AuthService::class.java).login(info)
            .setCallback(object : RequestCallback<LoginInfo?> {
                override fun onSuccess(param: LoginInfo?) {
                    clearMMKV()
                    MMKVProvider.loginResult = loginResult
                    getUserInfo(onSuccess = {
                        UserConfig.saveUserConfig(
                            userInfoBean = it,
                            loginResult = loginResult
                        )
                        if (loginResult.newUser || !loginResult.information) {
                            jump(RouterPath.PATH_LOGIN_EDIT_PROFILE){
                                finishAllActivitiesExceptNewest()
                            }
                        }
                        hideLoading()
                        onSuccess?.invoke()
                    }, onError = {
                        hideLoading()
                        customToast("获取用户信息失败,请重试")
                        onError?.invoke()
                    })
                }

                override fun onFailed(code: Int) {
                    hideLoading()
                    customToast("登录失败,请重试$code")
                    onError?.invoke()
                }

                override fun onException(exception: Throwable) {
                    hideLoading()
                    customToast("登录失败${exception.message}")
                    onError?.invoke()
                }
            })
    }
}