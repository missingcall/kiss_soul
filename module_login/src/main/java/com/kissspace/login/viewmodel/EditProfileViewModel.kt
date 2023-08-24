package com.kissspace.login.viewmodel

import androidx.databinding.ObservableField
import androidx.lifecycle.viewModelScope
import com.kissspace.common.base.BaseViewModel
import com.kissspace.common.http.getHtprotectTokenAsync
import com.kissspace.common.http.getUserInfo
import com.kissspace.common.model.UserInfoBean
import com.kissspace.common.util.mmkv.MMKVProvider
import com.kissspace.common.util.customToast
import com.kissspace.login.http.LoginApi
import com.kissspace.network.net.Method
import com.kissspace.network.net.request
import com.kissspace.network.net.*
import com.kissspace.util.logE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditProfileViewModel : BaseViewModel() {
    val submitBtnState = ObservableField(true)
    val userInfoBean = ObservableField<UserInfoBean>()

    fun requestUserInfo() {
        getUserInfo(onSuccess = {
            userInfoBean.set(it)
        })

    }

    fun editUserInfo( onSuccess:(Int) -> Unit,onError:() -> Unit) {
        viewModelScope.launch(Dispatchers.IO){
            val token = getHtprotectTokenAsync()
            withContext(Dispatchers.Main){
                userInfoBean.get()?.let {
                    val map = mutableMapOf<String, Any?>()
                    map["userId"] = it.userId
                    map["nickname"] = it.nickname
                    map["sex"] = it.sex
                    map["profilePath"] = it.profilePath
                    map["deviceId"] = MMKVProvider.sm_deviceId
                    map["accessFlags"] = "0"
                    map["token"] = token
                    request<Int>(LoginApi.API_EDIT_USER_INFO, Method.POST, map, onSuccess = {
                            it->
                        onSuccess.invoke(it)
                    }, onError = { e ->
                        onError.invoke()
                        customToast(e.message)
                    })
                }
            }
        }
    }
}

