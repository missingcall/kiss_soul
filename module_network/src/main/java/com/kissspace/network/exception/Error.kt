package com.kissspace.network.exception

/**
 *
 * @Author: nicko
 * @CreateDate: 2021/11/11 5:35 下午
 * @Description: 错误类
 *
 */
enum class Error(private val code: String, private val err: String) {

    /**
     * 未知错误
     */
    UNKNOWN("1000", "请求失败，请稍后再试"),

    /**
     * 解析错误
     */
    PARSE_ERROR("1001", "解析错误，请稍后再试"),

    /**
     * 网络错误
     */
    NETWORK_ERROR("1002", "网络连接错误，请稍后重试"),

    /**
     * 证书出错
     */
    SSL_ERROR("1004", "证书出错，请稍后再试"),

    /**
     * 连接超时
     */
    TIMEOUT_ERROR("1006", "网络连接超时，请稍后重试"),


    /**
     * 请求参数出错
     */
    PARAM_ERROR("1007", "请求失败，请稍后再试"),

    /**
     * 服务器异常
     */
    RESPONSE_ERROR("1008", "请求失败，请稍后再试"),


    /**
     *  麦位已满，进入排麦列表
     */
    UP_MIC_FULL_ERROR("50038", "麦位已满"),


    /**
     * 登录失效
     */
    LOGIN_EXPIRE("401", "token失效"),

    /**
     * 跳转到验证码页面
     */
    SMS_CODE("50150", ""),

    /**
     * 网络异常，请检查网络是否通畅
     */
    ERM_CODE("51513", "网络异常，请检查网络是否通畅"),

    /**
     * 活体认证
     */
    BIOMETRIC_CODE("51514", ""),

    /**
     * 账号冻结
     */
    ACCOUNT_FREEZE_CODE("51515", "账户已被冻结，请联系客服处理"),

    /**
     * 实名认证
     */
    USER_IDENTITY_CODE("50138", ""),

    USER_IDENTITY_CODE_FIRST("50142", ""),

    USER_IDENTITY_CODE_SECOND("51516", ""),

    LOGIN_VERIFICATION_CODE("50133", "验证码错误，请重新输入");

    fun getValue(): String {
        return err
    }

    fun getKey(): String {
        return code
    }

}