package com.kissspace.common.http.error

import com.drake.net.exception.ConvertException
import com.drake.net.exception.NetConnectException
import com.drake.net.exception.RequestParamsException
import com.drake.net.exception.ResponseException
import com.drake.net.exception.ServerResponseException
import com.kissspace.network.exception.AppException
import com.kissspace.network.result.ResultState
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import com.kissspace.util.logE
import com.kissspace.util.toast
import com.kissspace.network.exception.Error
import com.kissspace.util.ifNullOrEmpty
import java.net.ConnectException

suspend fun <T> handleNetException(throwable: Throwable, state: MutableSharedFlow<ResultState<T>>) {
    coroutineScope {
        when (throwable) {
            is ServerResponseException -> {
                val code: String = throwable.tag.toString()
                val msg = throwable.message
                state.emit(ResultState.onAppError(AppException(errCode = code, error = msg)))
            }

            is ResponseException -> {
                val code = throwable.tag?.toString().ifNullOrEmpty()
                val msg = throwable.message
                state.emit(ResultState.onAppError(AppException(errCode = code, error = msg)))
            }

            is RequestParamsException -> {
                val code = throwable.tag.toString()
                val msg = if (code == "404") "请求地址不存在" else "请求参数非法"
                state.emit(ResultState.onAppError(AppException(errCode = code, error = msg)))
            }

            is ConvertException -> {
                state.emit(ResultState.onAppError(AppException(Error.PARSE_ERROR, throwable)))
            }

            else -> {
                state.emit(ResultState.onAppError(AppException(Error.UNKNOWN, throwable)))
            }
        }
    }
}


fun handleNetException(throwable: Throwable): AppException {
    return when (throwable) {
        is ServerResponseException -> {
            val code: String = throwable.tag.toString()
            val msg = throwable.message
            AppException(errCode = code, error = msg)
        }

        is ResponseException -> {
            val code = throwable.tag.toString()
            val msg = throwable.message
            AppException(errCode = code, error = msg)
        }

        is RequestParamsException -> {
            val code = throwable.tag.toString()
            val msg = if (code == "404") "请求地址不存在" else "请求参数非法"
            AppException(errCode = code, error = msg)
        }

        is ConnectException -> {
            val code = throwable.toString()
            val msg = "连接服务器失败"
            AppException(errCode = code, error = msg)
        }

        is NetConnectException -> {
            val code = throwable.toString()
            val msg = "请检查网络，稍后再试"
            toast(msg)
            AppException(errCode = code, error = msg)
        }

        is ConvertException -> {
            AppException(Error.PARSE_ERROR, throwable)
        }

        else -> {
            logE("ExceptionHandler----${throwable.message.toString()}")
            AppException(Error.UNKNOWN, throwable)
        }
    }
}

