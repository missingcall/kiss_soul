package com.kissspace.mine.viewmodel
import androidx.databinding.ObservableField
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.ToastUtils
import com.kissspace.common.base.BaseViewModel
import com.kissspace.common.model.LevelModel
import com.kissspace.common.model.family.FamilyListModels
import com.kissspace.mine.http.MineApi
import com.kissspace.network.net.Method
import com.kissspace.network.net.request
import com.kissspace.network.result.ResultState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * @Author gaohangbo
 * @Date 2023/2/2 10:09.
 * @Describe
 */
class LevelViewModel: BaseViewModel() {

    //监听收到的事件数目
    private val levelCount = MutableSharedFlow<Int>()

    val getLevelEventCount = levelCount.asSharedFlow()

    //获取用户魅力等级列表
    fun getUserCharm( block: ((List<LevelModel>?) -> Unit)?){
        val param = mutableMapOf<String, Any?>()
        request<List<LevelModel>>(
            MineApi.API_QUERY_USER_CHARM, Method.GET, param, onSuccess =
        {
            block?.invoke(it)
            viewModelScope.launch {
                levelCount.emit(1)
            }
        }, onError = {
            ToastUtils.showShort(it.message)
        })
    }

    //获取用户财富等级列表
    fun queryUserConsume( block: ((List<LevelModel>?) -> Unit)?){
        val param = mutableMapOf<String, Any?>()
        request<List<LevelModel>>(MineApi.API_QUERY_USER_CONSUME, Method.GET, param, onSuccess =
        {
            block?.invoke(it)
            viewModelScope.launch {
                levelCount.emit(1)
            }

        }, onError = {
            ToastUtils.showShort(it.message)
        })
    }
}