package com.kissspace.setting.http

object SettingApi {

    //添加打招呼信息
    const val API_ADD_SAY_HI_INFO = "/djsoul-user/message/insertMessage"

    //查询打招呼信息
    const val API_REQUEST_SAY_HI_INFO = "/djsoul-user/message/getMessageList"

    //黑名单
    const val API_BLACK_LIST = "/djsoul-user/user/queryUserBlacklistList"

    //注销账号
    const val API_CANCEL_ACCOUNT = "/djsoul-user/user/logOffUser"

    //绑定手机号
    const val API_BIND_PHONE_NUMBER = "/djsoul-user/user/updateMobile"

    //短信验证码校验
    const val API_VERIFICATION_CODE = "/djsoul-user/sms/verificationCode"

    //转账认证
    const val API_TRANSFER_AUTHENTICATION = "/djsoul-user/userNum/transferVerification"

    const val API_SET_ADOLESCENT_PASSWORD = "/djsoul-user/userNum/setAdolescentPassword"

    const val API_BIND_BANKCARD = "/djsoul-user/authentication/bindBankCard"


    //检查版本
    const val API_UPGRADE = "/djsoul-user/configVersion/checkVersion"

    //我的钱包
    const val API_MY_WALLET = "/djsoul-user/userNum/getMyMoneyBag"

    //设置密码
    const val API_SETTING_LOGIN_PASSWORD = "/djsoul-user/user/setUserLoginPassword"

    //修改密码
    const val API_UPDATE_LOGIN_PASSWORD = "/djsoul-user/user/updateUserLoginPassword"

    //重置密码
    const val API_RESET_LOGIN_PASSWORD = "/djsoul-user/user/resetUserLoginPassword"
}