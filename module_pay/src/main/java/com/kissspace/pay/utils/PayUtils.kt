package com.kissspace.pay.utils


import android.content.Context
import androidx.fragment.app.FragmentActivity
import com.blankj.utilcode.util.GsonUtils
import com.tencent.mm.opensdk.modelbiz.WXLaunchMiniProgram
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import com.kissspace.pay.alipay.AliPayTools
import com.kissspace.pay.wechat.WXPayUtils
import com.kissspace.pay.wechat.WechatPayParam
import com.kissspace.common.config.Constants
import com.kissspace.common.config.Constants.ALI_PAY
import com.kissspace.common.config.Constants.WECHAT_PAY
import com.kissspace.common.http.createPayOrder
import com.kissspace.common.http.getPayResult
import com.kissspace.common.model.pay.PayResult
import com.kissspace.common.util.customToast

/**
 * @Author gaohangbo
 * @Date 2023/2/7 17:22.
 * @Describe
 */
object PayUtils {
    fun pay(
        @Constants.PayChannelType payChannelType: String,
        payProductId: String,
        activity: FragmentActivity,
        onSuccess: (() -> Unit)? = null,
        onFailure:(()->Unit)?=null
    ) {
        if (payChannelType == ALI_PAY) {
            activity.createPayOrder(payProductId, payChannelType) { orderInfo ->
                val payResult = GsonUtils.fromJson(orderInfo, PayResult::class.java)
                AliPayTools.aliPay(
                    activity,
                    payResult.payResult,
                    object : PayResultCallBack() {
                        override fun onSuccess() {
                            activity.getPayResult(payResult.orderNo, onSuccess = {
                                onSuccess?.invoke()
                            }, onError = {
                                onFailure?.invoke()
                            })
                        }

                        override fun onError(errorContent: String) {
                            customToast(errorContent)
                            onFailure?.invoke()
                        }

                        override fun onCancel() {
                            onFailure?.invoke()
                        }
                    })
            }
        } else if (payChannelType == WECHAT_PAY) {
            activity.createPayOrder(payProductId, payChannelType) { orderInfo ->
                val wechatPayParam = GsonUtils.fromJson(orderInfo, WechatPayParam::class.java)
                //假装请求了服务端信息，并获取了appid、partnerId、prepayId
                WXPayUtils.WXPayBuilder().setAppId(wechatPayParam.payResult.appid)
                    .setPartnerId(wechatPayParam.payResult.partnerId)
                    .setPrepayId(wechatPayParam.payResult.prepayId)
                    .setSign(wechatPayParam.payResult.sign)
                    .setNonceStr(wechatPayParam.payResult.nonceStr)
                    .setTimeStamp(wechatPayParam.payResult.timestamp)
                    .build()
                    .toWXPayNotSign(activity, object :
                        PayResultCallBack() {
                        override fun onSuccess() {
                            activity.getPayResult(wechatPayParam.orderNo, onSuccess = {
                                onSuccess?.invoke()
                            })
                        }

                        override fun onError(errorContent: String) {
                            customToast(errorContent)
                            onFailure?.invoke()
                        }

                        override fun onCancel() {
                            onFailure?.invoke()
                        }
                    })
            }
        }
    }

    fun wechatMiniProgramPay(context:Context){


    }

}