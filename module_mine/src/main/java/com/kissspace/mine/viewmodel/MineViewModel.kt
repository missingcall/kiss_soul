package com.kissspace.mine.viewmodel

import androidx.lifecycle.MediatorLiveData
import com.kissspace.common.base.BaseViewModel
import com.kissspace.common.model.NewMessageModel
import com.kissspace.common.model.UserInfoBean
import com.kissspace.common.util.customToast
import com.kissspace.mine.http.MineApi
import com.kissspace.network.net.Method
import com.kissspace.network.net.request

class MineViewModel : BaseViewModel() {

    val userInfo = MediatorLiveData<UserInfoBean>()

    //我的钱包新消息
    val walletNewMessage = MediatorLiveData<Boolean>()

    //我的家族新消息
    val familyNewMessage = MediatorLiveData<Boolean>()

    //任务中心
    val taskNewMessage = MediatorLiveData<Boolean>()

    //意见反馈
    val feedBackNewMessage = MediatorLiveData<Boolean>()

    //活动中心
    val taskCenterMessage = MediatorLiveData<Boolean>()

    //显示首充
    val isShowFirstRecharge = MediatorLiveData<Boolean>()

    fun queryNewMessageStatus(block: (NewMessageModel) -> Unit) {
        request<NewMessageModel>(
            MineApi.API_QUERY_MESSAGE_STATUS,
            Method.GET,
            onSuccess = {
                block.invoke(it)
            }, onError = {
                customToast(it.message)
            }
        )
    }
}