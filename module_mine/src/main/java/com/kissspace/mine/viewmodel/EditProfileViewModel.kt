package com.kissspace.mine.viewmodel

import androidx.databinding.ObservableField
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.ToastUtils
import com.kissspace.common.base.BaseViewModel
import com.kissspace.common.model.UserProfileBean
import com.kissspace.common.util.mmkv.MMKVProvider
import com.kissspace.common.util.customToast
import com.kissspace.mine.http.MineApi
import com.kissspace.network.net.Method
import com.kissspace.network.net.request
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class EditProfileViewModel : BaseViewModel() {
    val userInfo = ObservableField<UserProfileBean>()
    val userInfoEvent = MutableSharedFlow<UserProfileBean>()

    fun requestUserInfo() {
        val param = mutableMapOf<String, Any?>("userId" to MMKVProvider.userId)
        request<UserProfileBean>(MineApi.API_QUERY_USER_PROFILE, Method.GET, param, onSuccess = {
            userInfo.set(it)
            viewModelScope.launch { userInfoEvent.emit(it) }
        })
    }

    fun editAvatar(url: String) {
        val param = mutableMapOf<String, Any?>("profilePath" to url)
        request<Int>(MineApi.API_EDIT_PROFILE, Method.POST, param, onSuccess = {
            val user = userInfo.get()
            user?.auditingProfilePath = url
            userInfo.notifyChange()
        }, onError = {
            ToastUtils.showShort("资料修改失败，请重试")
        })
    }

    fun editBirthday(birthday: String) {
        val param = mutableMapOf<String, Any?>("birthday" to birthday)
        request<Int>(MineApi.API_EDIT_PROFILE, Method.POST, param, onSuccess = {
            customToast("修改成功")
            val user = userInfo.get()
            user?.birthday = birthday
            userInfo.notifyChange()
        }, onError = {
            ToastUtils.showShort("资料修改失败，请重试")
        })
    }
}