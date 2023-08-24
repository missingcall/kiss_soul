package com.kissspace.network.result

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.drake.net.utils.scopeLife
import com.drake.net.utils.scopeNetLife
import com.kissspace.network.exception.AppException
import com.kissspace.network.exception.ExceptionHandle
import com.kissspace.util.logE
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/11/2 15:29
 * @Description:
 *
 */
sealed class ResultState<out T> {
    companion object {
        fun <T> onAppSuccess(data: T?): ResultState<T> = Success(data)
        fun <T> onAppError(error: AppException): ResultState<T> = Error(error)
    }

    data class Success<out T>(val data: T?) : ResultState<T>()
    data class Error(val error: AppException) : ResultState<Nothing>()
}


/**
 * 不处理返回值 直接返回请求结果
 * @param result 请求结果
 */
suspend fun <T> MutableSharedFlow<ResultState<T>>.parseResult(result: T?) {
    emit(ResultState.onAppSuccess(result))
}

suspend fun <T> MutableLiveData<ResultState<T>>.parseResult(result: T?) {
    postValue(ResultState.onAppSuccess(result))
}

fun <T> LifecycleOwner.collectData(
    data: SharedFlow<ResultState<T?>>,
    onSuccess: ((T) -> Unit) = {},
    onError: ((AppException) -> Unit) = {},
    onEmpty: (() -> Unit) = { },
) {
    scopeLife {
        data.collect {
            when (it) {
                is ResultState.Success -> {
                    val result = it.data
                    if (result == null || (result is List<*> && result.isEmpty())) {
                        onEmpty?.invoke()
                    } else {
                        onSuccess(it.data!!)
                    }
                }

                is ResultState.Error -> {
                    onError?.invoke(it.error)
                }
            }
        }
    }
}

suspend fun <T> MutableSharedFlow<ResultState<T>>.emitSuccess(data: T) {
    this.emit(ResultState.onAppSuccess(data))
}
