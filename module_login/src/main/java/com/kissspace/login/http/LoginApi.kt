package com.kissspace.login.http

object LoginApi {

    //友盟一键登录
    val API_QUICK_LOGIN = "/djsoul-user/user/register"

    //完善用户信息
    val API_EDIT_USER_INFO = "/djsoul-user/user/updateUser"

    //手机验证码登录
    val API_SMS_CODE_LOGIN = "/djsoul-user/user/login"

    //友盟手机获取用户列表
    val API_USER_LIST_UMENG = "/djsoul-user/user/queryUserListByUMeng"

    //获取启动页广告
    val API_GET_AD = "/djsoul-user/splashAd/queryOne"

    val API_PASSWORD_LOGIN = "/djsoul-user/user/passwordLogin"
}