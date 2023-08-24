package com.kissspace.mine.viewmodel

import androidx.lifecycle.MutableLiveData
import com.kissspace.common.base.BaseViewModel
import com.kissspace.common.util.DJSLiveData
import com.kissspace.common.util.customToast
import com.kissspace.mine.http.MineApi
import com.kissspace.network.net.Method
import com.kissspace.network.net.request
import com.kissspace.util.isNotEmptyBlank

/**
 * @Author gaohangbo
 * @Date 2023/2/12 14:33.
 * @Describe
 */
class BindAlipayViewModel : BaseViewModel() {

    val userName= MutableLiveData<String>()
    val userPhone= MutableLiveData<String>()

    //用户身份证
    val userIdCardNumber= MutableLiveData<String>()

    val alipayAccount= MutableLiveData<String>()

    val verificationCode = MutableLiveData<String>()

    val sendSmsEnable = MutableLiveData(true)

    val btnEnable = DJSLiveData<Boolean>().apply {
        addSources(alipayAccount,verificationCode) {
            setValue(alipayAccount.value.isNotEmptyBlank() &&verificationCode.value.isNotEmptyBlank() )
        }
    }


    fun bindAlipay(alipayAccount:String?,block:(Boolean)->Unit) {
        val param = mutableMapOf<String, Any?>()
        param["account"] = alipayAccount
        request<Boolean>(
            MineApi.API_BIND_ALIPAY,
            Method.POST,
            param,
            onSuccess = {
                block.invoke(it)
            }, onError = {
                customToast(it.message)
            }
        )
    }

}