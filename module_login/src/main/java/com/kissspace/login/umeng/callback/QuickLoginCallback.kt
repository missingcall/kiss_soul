package com.kissspace.login.umeng.callback

/**
 *@author: adan
 *@date: 2023/4/1
 *@Description:
 */
interface QuickLoginCallback {

    /**
     * 是否初始化成功
     */
    fun initializationSuccess()

    /**
     * 一键登录成功
     */
    fun onQuickLoginSuccess(token:String)

    /**
     * 一键登录失败
     */
    fun onQuickLoginFail()

}