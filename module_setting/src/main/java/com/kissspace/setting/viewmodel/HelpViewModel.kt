package com.kissspace.setting.viewmodel

import com.kissspace.common.base.BaseViewModel
import com.kissspace.common.model.UpgradeBean
import com.kissspace.network.net.Method
import com.kissspace.network.net.request
import com.kissspace.network.result.ResultState
import com.kissspace.setting.http.SettingApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class HelpViewModel : BaseViewModel() {

    private val _checkVersionEvent = MutableSharedFlow<ResultState<UpgradeBean>>()
    val checkVersionEvent = _checkVersionEvent.asSharedFlow()

    fun checkVersion() {
        val params = mutableMapOf<String, Any?>("os" to 1)
        request(com.kissspace.setting.http.SettingApi.API_UPGRADE, Method.GET, params, state = _checkVersionEvent)
    }
}