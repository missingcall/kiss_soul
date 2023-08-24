package com.kissspace.setting.viewmodel

import androidx.lifecycle.MutableLiveData
import com.kissspace.common.base.BaseViewModel
import com.kissspace.common.util.customToast
import com.kissspace.network.net.Method
import com.kissspace.network.net.request
import com.kissspace.setting.http.SettingApi
import com.kissspace.util.logE

/**
 * @Author gaohangbo
 * @Date 2023/2/11 16:37.
 * @Describe
 */
class TeenagerViewModel : BaseViewModel() {

    var mStepCount = MutableLiveData(0)
    var stepOnePassword = MutableLiveData("")
    var stepTwoPassword = MutableLiveData("")
    var stepThirdPassword = MutableLiveData("")
    val btnEnable = MutableLiveData(false)
    val teenagerHint = MutableLiveData<String>()
    fun setAdolescentPassword(type:String,password:String,block: ((Boolean) -> Unit)) {
        val param = mutableMapOf<String, Any?>()
        logE("adolescent$type")
        param["adolescent"] = type
        param["password"] = password
        request<Boolean>(
            com.kissspace.setting.http.SettingApi.API_SET_ADOLESCENT_PASSWORD, Method.POST, param, onSuccess = {
            block.invoke(it)
        },
        onError = {
            customToast(it.message)
        })
    }

}