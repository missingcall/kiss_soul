package com.kissspace.mine.viewmodel

import androidx.databinding.ObservableField
import com.kissspace.common.base.BaseViewModel
import com.kissspace.common.model.UserProfileBean
import com.kissspace.common.model.task.TaskCenterListModel
import com.kissspace.mine.http.MineApi
import com.kissspace.network.net.Method
import com.kissspace.network.net.request

/**
 * @Author gaohangbo
 * @Date 2023/1/9 19:44.
 * @Describe
 */
class TaskCenterViewModel : BaseViewModel() {
    val userInfo = ObservableField<UserProfileBean>()

    fun requestTaskList(onSuccess: ((TaskCenterListModel) -> Unit)?,onError: (() -> Unit)?=null) {
        request<TaskCenterListModel>(MineApi.API_TASK_CENTER, Method.GET, onSuccess = {
            onSuccess?.invoke(it)
        }, onError = {
            onError?.invoke()
        })
    }
}