package com.kissspace.dynamic.ui.viewmodel

import com.blankj.utilcode.util.ToastUtils
import com.kissspace.dynamic.http.DynamicApi
import com.kissspace.common.base.BaseViewModel
import com.kissspace.network.net.Method
import com.kissspace.network.net.request
import kotlinx.coroutines.flow.MutableStateFlow
import org.json.JSONArray

/**
 * @Author gaohangbo
 * @Date 2023/8/2 14:45.
 * @Describe
 */
class TaskDetailFilterViewModel: BaseViewModel() {

    val isFilterOpen= MutableStateFlow(false)

    fun sendDynamic(
        dynamicText: String? = null,
        voiceDynamicContent: String? = null,
        feedbackImageList: List<String>? = null,
        block: ((Boolean) -> Unit)?
    ) {
        val param = mutableMapOf<String, Any?>()
        param["textDynamicContent"] = dynamicText
        param["voiceDynamicContent"] = voiceDynamicContent
        val jsonArray = JSONArray()
        feedbackImageList?.map { jsonArray.put(it) }
        param["pictureDynamicContent"] = jsonArray
        request<Boolean>(
            url = DynamicApi.API_INSERT_DYNAMICS,
            method = Method.POST,
            param = param,
            onSuccess = {
                block?.invoke(it)
            },
            onError = {
                ToastUtils.showLong(it.errorMsg)
            }
        )

    }



    fun getTaskDetailList(){

    }


}