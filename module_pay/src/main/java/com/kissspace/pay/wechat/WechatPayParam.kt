package com.kissspace.pay.wechat

/**
 * @Author gaohangbo
 * @Date 2023/1/17 11:44.
 * @Describe
 */
data class WechatPayParam(
    val orderNo:String,
    val payResult: PayResult
)

data class PayResult(
    val appid: String,
    val nonceStr: String,
    val packageVal: String,
    val partnerId: String,
    val prepayId: String,
    val sign: String,
    val timestamp: String
)