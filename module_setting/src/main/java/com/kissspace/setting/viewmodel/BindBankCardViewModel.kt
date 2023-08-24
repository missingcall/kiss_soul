package com.kissspace.setting.viewmodel

import androidx.lifecycle.MutableLiveData
import com.kissspace.common.base.BaseViewModel
import com.kissspace.common.util.DJSLiveData
import com.kissspace.common.util.customToast
import com.kissspace.common.util.mmkv.MMKVProvider
import com.kissspace.network.net.Method
import com.kissspace.network.net.request
import com.kissspace.setting.http.SettingApi
import com.kissspace.util.isNotEmptyBlank

/**
 * @Author gaohangbo
 * @Date 2023/2/11 16:37.
 * @Describe
 */
class BindBankCardViewModel : BaseViewModel() {

     val userBankCardNumber= MutableLiveData<String>()

    val btnEnable = DJSLiveData<Boolean>().apply {
        addSources(userBankCardNumber) {
            setValue(userBankCardNumber.value.isNotEmptyBlank())
        }
    }

    fun bindBankCardViewModel(bankCardNumber:String,block: ((Boolean) -> Unit)) {
        val param = mutableMapOf<String, Any?>()
        param["cardNo"] = bankCardNumber
        request<Boolean>(
            com.kissspace.setting.http.SettingApi.API_BIND_BANKCARD, Method.POST, param, onSuccess = {
            block.invoke(it)
        },
        onError = {
            customToast(it.message)
        })
    }

}