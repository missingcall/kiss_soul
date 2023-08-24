package com.kissspace.setting.viewmodel

import android.text.Editable
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.kissspace.common.base.BaseViewModel
import com.kissspace.common.util.DJSLiveData
import com.kissspace.util.isNotEmptyBlank

class ForgetPasswordViewModel : BaseViewModel() {
    val phoneNumber = ObservableField<String>()

    val verificationCode = ObservableField<String>()

    val password = ObservableField<String>()

    val confirmPwd = ObservableField<String>()

    val sendSmsEnable = ObservableField(false)

    val btnEnable = ObservableField(false)


    fun onTextChange(editable: Editable) {
        sendSmsEnable.set(
            phoneNumber.get().toString().isNotEmptyBlank()
        )
        btnEnable.set(
            phoneNumber.get().toString().isNotEmptyBlank() && verificationCode.get()
                .isNotEmptyBlank() && password.get().isNotEmptyBlank() && confirmPwd.get()
                .isNotEmptyBlank()
        )
    }

}