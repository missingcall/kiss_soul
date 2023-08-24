package com.kissspace.login.viewmodel

import android.text.Editable
import androidx.databinding.ObservableField
import com.blankj.utilcode.util.DeviceUtils
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
import com.kissspace.common.util.mmkv.MMKVProvider
import com.kissspace.login.http.LoginApi
import com.kissspace.network.net.Method
import com.kissspace.network.net.request
import com.kissspace.network.result.ResultState
import com.kissspace.util.isNotEmpty
import com.kissspace.util.logE
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow


class LoginViewModel : BaseViewModel() {
    val phoneIconState = ObservableField(false)
    val getCodeBtnState = ObservableField(false)
    var btnEnable = ObservableField(false)
    var sendSmsEnable = ObservableField(true)

    private val _token = MutableSharedFlow<ResultState<LoginResultBean>>()
    val token = _token.asSharedFlow()
    var isAgree = ObservableField(false)
    private val _userInfo = MutableSharedFlow<ResultState<UserInfoBean>>()
    val userInfo = _userInfo.asSharedFlow()
    private val _accouts = MutableSharedFlow<ResultState<List<UserAccountBean>>>()
    val accounts = _accouts.asSharedFlow()
    private val _createaccouts = MutableSharedFlow<ResultState<CreateUserAccountBean>>()
    val createAccounts = _createaccouts.asSharedFlow()

    fun quickLogin(token: String) {
        val map = mutableMapOf<String, Any?>()
        map["token"] = token
        map["accessFlags"] = "0"
        map["deviceId"] = MMKVProvider.sm_deviceId
        map["machineCode"] = DeviceUtils.getUniqueDeviceId()
        request(LoginApi.API_QUICK_LOGIN, Method.POST, map, state = _token)
    }

    fun phoneCodeLogin(phoneNumber: String, verifyCode: String) {
        val map = mutableMapOf<String, Any?>()
        map["mobile"] = phoneNumber
        map["smsCode"] = verifyCode
        map["accessFlags"] = "0"
        map["deviceId"] = MMKVProvider.sm_deviceId
        map["machineCode"] = DeviceUtils.getUniqueDeviceId()
        request(LoginApi.API_SMS_CODE_LOGIN, Method.POST, map, state = _token)
    }

    fun requestUserListByPhone(phoneNumber: String){
        val params = mutableMapOf<String,Any?>()
        params["phone"] = phoneNumber
        params["machineCode"] = DeviceUtils.getUniqueDeviceId()
        params["switchAccounts"] = false
        params["accessFlags"] = 0
        request(CommonApi.API_USER_LIST_BY_PHONE, Method.POST,param = params,state = _accouts)
    }

    fun requestUserListByUMeng(umToken: String){
        val params = mutableMapOf<String,Any?>()
        params["token"] = umToken
        params["machineCode"] = DeviceUtils.getUniqueDeviceId()
        params["accessFlags"] = 0
        request(LoginApi.API_USER_LIST_UMENG, Method.POST,param = params,state = _accouts)
    }

    fun createAccount(phone:String){
        val params = mutableMapOf<String,Any?>()
        params["phone"] = phone
        params["machineCode"] = DeviceUtils.getUniqueDeviceId()
        params["accessFlags"] = 0
        params["appleId"] = ""
        request(CommonApi.API_USER_ACCOUNT_CREATE, Method.POST,param = params,state = _createaccouts)
    }

    fun loginByUserId(userId: String,tokenHead:String?,token:String?){
        if (tokenHead != null && token != null){
            MMKVProvider.loginResult = LoginResultBean(token,"","",tokenHead,0,false,false,"")
        }
        val params = mutableMapOf<String,Any?>()
        params["userId"] = userId
        params["loginType"] = "002"
        params["machineCode"] = DeviceUtils.getUniqueDeviceId()
        request(CommonApi.API_LOGIN_BY_USERID, Method.POST,param = params,state = _token)
    }

    fun onTextChange(editable: Editable) {
        phoneIconState.set(editable.toString().isNotEmpty())
        getCodeBtnState.set(editable.toString().length == 13)
    }

    fun loginIm(
        loginResult: LoginResultBean,
        onSuccess: (() -> Unit?)? = null,
        onError: (() -> Unit?)? = null
    ) {
        val info = LoginInfo(loginResult.accId, loginResult.netEaseToken)
        NIMClient.getService(AuthService::class.java).login(info)
            .setCallback(object : RequestCallback<LoginInfo?> {
                override fun onSuccess(param: LoginInfo?) {
                    MMKVProvider.loginResult = loginResult
                    getUserInfo(onSuccess = {
                        UserConfig.saveUserConfig(
                            userInfoBean = it,
                            loginResult = loginResult
                        )
                        if (loginResult.newUser || !loginResult.information) {
                            jump(RouterPath.PATH_LOGIN_EDIT_PROFILE)
                        } else if (it.adolescent) {
                            jump(RouterPath.PATH_TEENAGER_MODE, "stepCount" to 2)
                        } else {
                            jump(RouterPath.PATH_MAIN)
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