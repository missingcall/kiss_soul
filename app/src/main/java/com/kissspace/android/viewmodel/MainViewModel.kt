package com.kissspace.android.viewmodel

import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.auth.AuthService
import com.netease.nimlib.sdk.auth.LoginInfo
import com.kissspace.android.http.Api
import com.kissspace.common.base.BaseViewModel
import com.kissspace.common.config.CommonApi
import com.kissspace.common.model.inviationcode.InvitationModel
import com.kissspace.common.model.MyCollectResponse
import com.kissspace.common.model.UpgradeBean
import com.kissspace.common.model.inviationcode.InvitationSubmit
import com.kissspace.common.util.mmkv.MMKVProvider
import com.kissspace.network.net.Method
import com.kissspace.network.net.request
import com.kissspace.network.result.ResultState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class MainViewModel : BaseViewModel() {
    private val _checkVersionEvent = MutableSharedFlow<ResultState<UpgradeBean?>>()
    val checkVersionEvent = _checkVersionEvent.asSharedFlow()

    private val _collectListEvent = MutableSharedFlow<ResultState<MyCollectResponse>>()
    val collectListEvent = _collectListEvent.asSharedFlow()

    fun requestCollectList() {
        val param = mutableMapOf<String, Any?>()
        param["pageNum"] = 1
        param["pageSize"] = 1
        param["type"] = "003"
        param["userId"] = MMKVProvider.userId
        request(CommonApi.API_QUERY_MY_COLLECT, Method.GET, param, state = _collectListEvent)
    }


    fun checkVersion() {
        val params = mutableMapOf<String, Any?>("os" to 1)
        request(Api.API_UPGRADE, Method.GET, params, state = _checkVersionEvent)
    }

    fun sayHi() {
        request<String?>(Api.API_SAY_HI, Method.POST)
    }


    fun submitInvitationCode(
        onSuccess: ((InvitationSubmit?) -> Unit)?, onError: ((String) -> Unit)?
    ) {
        request<InvitationSubmit?>(Api.API_SUBMIT_INVITATION_CODE, Method.POST, onSuccess = {
            onSuccess?.invoke(it)
        }, onError = {
            onError?.invoke(it.message.orEmpty())
        })
    }

    fun getInvitationCode(block: ((InvitationModel?) -> Unit)?) {
        request<InvitationModel?>(Api.API_GET_BOOLEAN_IS_INVITATION, Method.GET, onSuccess = {
            block?.invoke(it)
        })
    }

    fun loginNim() {
        val info = LoginInfo(MMKVProvider.loginResult?.accId, MMKVProvider.loginResult?.netEaseToken)
        NIMClient.getService(AuthService::class.java).login(info)
    }

}