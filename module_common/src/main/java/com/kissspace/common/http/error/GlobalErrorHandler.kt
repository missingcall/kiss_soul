package com.kissspace.common.http.error

import com.drake.net.exception.HttpFailureException
import com.drake.net.exception.ResponseException
import com.drake.net.interfaces.NetErrorHandler
import com.kissspace.common.config.Constants
import com.kissspace.common.router.jump
import com.kissspace.common.util.loginOut
import com.kissspace.common.router.RouterPath
import com.kissspace.common.util.showAuthenticationDialog
import com.kissspace.network.exception.Error
import com.kissspace.util.logE
import com.kissspace.util.toast

/**
 *
 * @Author: nicko
 * @CreateDate: 2023/3/30 15:47
 * @Description: 全局错误处理
 *
 */

class GlobalErrorHandler : NetErrorHandler {
    override fun onError(e: Throwable) {
        //错误提示2次
        //super.onError(e)
        logE("request error------>${e}")
        if (e is ResponseException) {
            when (e.tag.toString()) {
                Error.LOGIN_EXPIRE.getKey() -> {
                    if (!Constants.isTokenExpired) {
                        Constants.isTokenExpired = true
                        loginOut(logoutRoom = false)
                    }
                }

                Error.SMS_CODE.getKey() -> {
                    jump(RouterPath.PATH_SEND_SMS_CODE)
                }

                Error.BIOMETRIC_CODE.getKey() -> {
                    jump(RouterPath.PATH_USER_BIOMETRIC)
                }

                Error.USER_IDENTITY_CODE.getKey(),

                Error.USER_IDENTITY_CODE_FIRST.getKey(),

                Error.USER_IDENTITY_CODE_SECOND.getKey() -> {
                    showAuthenticationDialog {
                        jump(RouterPath.PATH_USER_IDENTITY_AUTH)
                    }
                }

                Error.ERM_CODE.getKey() -> {
                    toast(Error.ERM_CODE.getValue())
                }

                Error.ACCOUNT_FREEZE_CODE.getKey() -> {
                    toast(Error.ACCOUNT_FREEZE_CODE.getValue())
                }

                Error.LOGIN_VERIFICATION_CODE.getKey() -> {
                    toast(Error.LOGIN_VERIFICATION_CODE.getValue())
                }

                else -> {

                }
            }
        }

    }
}