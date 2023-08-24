package com.kissspace.android.viewmodel

import androidx.lifecycle.MutableLiveData
import com.kissspace.common.base.BaseViewModel
import com.kissspace.common.util.DJSLiveData
import com.kissspace.common.util.customToast
import com.kissspace.network.net.Method
import com.kissspace.network.net.request
import com.kissspace.util.isNotEmptyBlank
import com.kissspace.android.http.Api

/**
 * @Author gaohangbo
 * @Date 2023/2/3 15:19.
 * @Describe
 */
class IdentityAuthViewModel : BaseViewModel() {

    //用户名称
    var userName = MutableLiveData<String>()

    //身份证号码
    var idNumber = MutableLiveData<String>()

    val isIdentityAuthEnable = DJSLiveData<Boolean>().apply {
        addSources(userName,idNumber) {
            setValue(userName.value.isNotEmptyBlank()&&idNumber.value.isNotEmptyBlank())
        }
    }

    fun identityAuth(name: String,cardNo: String,onSuccess: ((Boolean) -> Unit) = {}) {
        val param = mutableMapOf<String, Any?>()
        param["name"] = name
        param["cardNo"] = cardNo
        request<Boolean>(Api.API_IDENTITY_AUTH_VIEW, Method.POST, param, onSuccess = {
            onSuccess.invoke(it)
        }, onError = {
            customToast(it.message,true)
        })
    }
    //活体认证
    fun faceRecognition(token: String,type: Int,onSuccess: ((Boolean) -> Unit)?,onError: ((String?) -> Unit)?) {
        val param = mutableMapOf<String, Any?>()
        param["token"] = token
        param["type"] = type
        request<Boolean>(Api.API_FACE_RECOGNITION, Method.POST, param, onSuccess = {
            onSuccess?.invoke(it)
        }, onError = {
            onError?.invoke(it.message)
        })
    }
}