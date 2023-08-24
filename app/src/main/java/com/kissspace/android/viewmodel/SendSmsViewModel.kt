package com.kissspace.android.viewmodel

import androidx.lifecycle.MutableLiveData
import com.kissspace.common.base.BaseViewModel
import com.kissspace.common.util.DJSLiveData
import com.kissspace.common.util.customToast
import com.kissspace.network.net.Method
import com.kissspace.network.net.request
import com.kissspace.util.isNotEmptyBlank

class SendSmsViewModel : BaseViewModel() {

    val phoneNumber= MutableLiveData<String>()

    val verificationCode = MutableLiveData<String>()

    val sendSmsEnable = MutableLiveData(true)

    val sendSmsBtnEnable = DJSLiveData<Boolean>().apply {
        addSources(phoneNumber,sendSmsEnable) {
            setValue(phoneNumber.value.isNotEmptyBlank() && sendSmsEnable.value == true)
        }
    }

    val btnEnable = DJSLiveData<Boolean>().apply {
        addSources(phoneNumber,verificationCode) {
            setValue(phoneNumber.value.isNotEmptyBlank() &&verificationCode.value.isNotEmptyBlank() )
        }
    }

    fun cancelAccount(phoneNumber:String,code:String,block: ((Boolean) -> Unit)) {
        val param = mutableMapOf<String, Any?>().apply {
            put("phone", phoneNumber)
        }
        request<Boolean>(com.kissspace.setting.http.SettingApi.API_CANCEL_ACCOUNT, Method.GET, param, onSuccess = {
          block.invoke(it)
        }, onError = {
            customToast(it.message)
        })
    }

    fun bindPhoneNumber(phone:String,code:String,block: ((Boolean) -> Unit)) {
        val param = mutableMapOf<String, Any?>().apply {
            put("phone", phone)
            put("code", code)
        }
        request<Boolean>(com.kissspace.setting.http.SettingApi.API_BIND_PHONE_NUMBER, Method.GET, param, onSuccess = {
            block.invoke(it)
        }, onError = {
            customToast(it.message)
        })
    }

    //转账认证
    fun transferVerification(phone:String,code:String,type:String,block:((Boolean) -> Unit)) {
        val param = mutableMapOf<String, Any?>().apply {
            put("mobile", phone)
            put("code", code)
            put("type", type)
        }
        request<Boolean>(com.kissspace.setting.http.SettingApi.API_TRANSFER_AUTHENTICATION, Method.GET, param, onSuccess = {
            block.invoke(it)
        }, onError = {
            customToast(it.message)
        })
    }

}