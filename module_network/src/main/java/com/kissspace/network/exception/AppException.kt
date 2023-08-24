package com.kissspace.network.exception

import java.lang.Exception

class AppException : Exception {
    var errorMsg: String //错误消息
    var errCode: String = "" //错误码
    var throwable: Throwable? = null

    constructor(errCode: String, error: String?, throwable: Throwable? = null) : super(error) {
        this.errorMsg = error ?: "请求失败，请稍后再试"
        this.errCode = errCode
        this.throwable = throwable
    }

    constructor(error: Error, e: Throwable?) {
        errCode = error.getKey()
        errorMsg = error.getValue()
        throwable = e
    }
}