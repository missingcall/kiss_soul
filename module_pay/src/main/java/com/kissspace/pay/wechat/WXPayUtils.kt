package com.kissspace.pay.wechat

import android.content.Context
import com.tencent.mm.opensdk.constants.Build
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import com.tencent.mm.opensdk.modelpay.PayReq
import com.kissspace.util.logE
import com.kissspace.common.config.Constants.NO_OR_LOW_WX
import com.kissspace.util.orZero
import com.kissspace.pay.utils.PayResultCallBack
import com.kissspace.common.config.ConstantsKey

/**
 * Created by chaohx on 2017/7/5.
 */
class WXPayUtils private constructor(private val builder: WXPayBuilder) {
    private var iwxapi //微信支付api
            : IWXAPI? = null

    /**
     * 调起微信支付的方法,不需要在客户端签名
     */
    fun toWXPayNotSign(context: Context?,callBack: PayResultCallBack) {
        WechatPay.mCallback =callBack
        iwxapi = WXAPIFactory.createWXAPI(context, null) //初始化微信api
        iwxapi?.registerApp(ConstantsKey.WECHAT_APPID)
        if(check()){
          //注册appid  appid可以在开发平台获取
           //这里注意要放在子线程
           val payRunnable = Runnable {
               val request = PayReq() //调起微信APP的对象
               //下面是设置必要的参数，也就是前面说的参数,这几个参数从何而来请看上面说明
               request.appId = builder.appId
               request.partnerId = builder.partnerId
               request.prepayId = builder.prepayId
               request.packageValue = "Sign=WXPay"
               request.nonceStr = builder.nonceStr
               request.timeStamp = builder.timeStamp
               request.sign = builder.sign
               logE("run: " + request.appId + request.nonceStr + request.sign)
               iwxapi?.sendReq(request) //发送调起微信的请求
           }
           val payThread = Thread(payRunnable)
           payThread.start()
       }else{
            WechatPay.onResp(NO_OR_LOW_WX)
       }

    }
    private fun check(): Boolean {
        return iwxapi?.isWXAppInstalled == true && iwxapi?.wxAppSupportAPI.orZero() >= Build.PAY_SUPPORTED_SDK_INT
    }

    class WXPayBuilder {
        var appId: String? = null
        var partnerId: String? = null
        var prepayId: String? = null
        var packageValue: String? = null
        var nonceStr: String? = null
        var timeStamp: String? = null
        var sign: String? = null
        fun build(): WXPayUtils {
            return WXPayUtils(this)
        }

        fun setAppId(appId: String?): WXPayBuilder {
            this.appId = appId
            return this
        }

        fun setPartnerId(partnerId: String?): WXPayBuilder {
            this.partnerId = partnerId
            return this
        }

        fun setPrepayId(prepayId: String?): WXPayBuilder {
            this.prepayId = prepayId
            return this
        }

        fun setPackageValue(packageValue: String?): WXPayBuilder {
            this.packageValue = packageValue
            return this
        }

        fun setNonceStr(nonceStr: String?): WXPayBuilder {
            this.nonceStr = nonceStr
            return this
        }

        fun setTimeStamp(timeStamp: String?): WXPayBuilder {
            this.timeStamp = timeStamp
            return this
        }

        fun setSign(sign: String?): WXPayBuilder {
            this.sign = sign
            return this
        }
    }
}