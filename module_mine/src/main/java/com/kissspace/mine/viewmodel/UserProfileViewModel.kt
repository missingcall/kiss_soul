package com.kissspace.mine.viewmodel

import androidx.databinding.ObservableField
import androidx.lifecycle.viewModelScope
import com.kissspace.common.base.BaseViewModel
import com.kissspace.common.config.CommonApi
import com.kissspace.common.router.jump
import com.kissspace.common.model.CommonGiftInfo
import com.kissspace.common.model.UserProfileBean
import com.kissspace.common.router.RouterPath
import com.kissspace.mine.http.MineApi
import com.kissspace.network.net.Method
import com.kissspace.network.net.request
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class UserProfileViewModel : BaseViewModel() {
    val userInfo = ObservableField<UserProfileBean>()
    val userProfileEvent = MutableSharedFlow<UserProfileBean>()
    val giftListEvent = MutableSharedFlow<MutableList<CommonGiftInfo>>()

    fun requestUserInfo(userId: String) {
        val params = mutableMapOf<String, Any?>("userId" to userId)
        request<UserProfileBean>(MineApi.API_QUERY_USER_PROFILE, Method.GET, params, onSuccess = {
            userInfo.set(it)
            viewModelScope.launch {
                userProfileEvent.emit(it)
            }
            requestAllGift(it.giftWall)
        })
    }

    private fun requestAllGift(gifts:List<CommonGiftInfo>) {
        val receiveGiftIds = gifts.map { it.giftId }
        request<MutableList<CommonGiftInfo>>(MineApi.API_QUERY_ALL_GIFT, Method.GET, onSuccess = {
            it.removeAll {t-> receiveGiftIds.contains(t.giftId) }
            viewModelScope.launch { giftListEvent.emit(it) }
        })
    }


    fun jumpStore() {
        jump(RouterPath.PATH_STORE)
    }

    fun editProfile() {
        jump(RouterPath.PATH_EDIT_PROFILE)
    }

    fun follow() {
        userInfo.get()?.let {
            if (it.attention) {
                cancelFollow()
            } else {
                followUser()
            }

        }

    }

    private fun followUser() {
        val param = mutableMapOf<String, Any?>("userId" to userInfo.get()?.userId)
        request<Int>(CommonApi.API_FOLLOW_USER, Method.POST, param, onSuccess = {
            val user = userInfo.get()
            user?.attention = true
            userInfo.set(user)
            userInfo.notifyChange()
        })
    }

    private fun cancelFollow() {
        val param = mutableMapOf<String, Any?>("userId" to userInfo.get()?.userId)
        request<Int>(CommonApi.API_CANCEL_FOLLOW_USER, Method.GET, param, onSuccess = {
            val user = userInfo.get()
            user?.attention = false
            userInfo.set(user)
            userInfo.notifyChange()
        })
    }
}