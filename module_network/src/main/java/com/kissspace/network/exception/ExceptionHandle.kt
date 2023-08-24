package com.kissspace.network.exception

import com.drake.net.exception.ConvertException
import com.drake.net.exception.NetConnectException
import com.drake.net.exception.RequestParamsException
import com.drake.net.exception.ResponseException
import com.drake.net.exception.ServerResponseException
import java.net.ConnectException


object ExceptionHandle {

    fun parseException(throwable: Throwable): AppException {
        return when (throwable) {
            is ServerResponseException -> {
                val code: String = throwable.tag.toString()
                val msg = throwable.message
                AppException(errCode = code, error = msg)
            }

            is ResponseException -> {
                val code: String = throwable.tag.toString()
                val msg = throwable.message
                AppException(errCode = code, error = msg)
            }

            is RequestParamsException -> {
                val code = throwable.tag.toString()
                val msg = if (code == "404") "请求地址不存在" else "请求参数非法"
                AppException(errCode = code, error = msg)
            }

            is ConnectException -> {
                AppException(Error.NETWORK_ERROR, throwable)
            }

            is NetConnectException -> {
                AppException(Error.NETWORK_ERROR, throwable)
            }

            is ConvertException -> {
                AppException(Error.PARSE_ERROR, throwable)
            }

            else -> {
                AppException(Error.UNKNOWN, throwable)
            }
        }
    }
}