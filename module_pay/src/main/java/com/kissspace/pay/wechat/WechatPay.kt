package com.kissspace.pay.wechat

import com.kissspace.common.config.Constants.CANCEL_PAY
import com.kissspace.common.config.Constants.ERROR_PAY
import com.kissspace.common.config.Constants.NO_OR_LOW_WX
import com.kissspace.common.config.Constants.SUCCESS_PAY
import com.kissspace.pay.utils.PayResultCallBack


/**
 * 描述：微信支付
 *
 *
 * 外部调起支付请使用WechatPayTools中的相关方法
 *
 *
 * 请在WXPayEntryActivity中调用 WechatPay.getInstance().onResp(resp.errCode);
 *
 * @author 张钦
 * @date 2018/1/25
 */
object WechatPay {

    var mCallback: PayResultCallBack? = null

    /**
     * 支付回调响应
     *
     * @param errorCode
     */
    fun onResp(errorCode: Int) {
        if (mCallback == null) {
            return
        }
        // 成功
        when (errorCode) {
            SUCCESS_PAY -> mCallback!!.onSuccess()
            ERROR_PAY -> mCallback!!.onError("支付失败")
            CANCEL_PAY -> mCallback!!.onCancel()
            NO_OR_LOW_WX -> mCallback!!.onError("未安装微信或微信版本过低")
            else -> {}

        }
        mCallback = null
    }

//    /**
//     * 销毁方法
//     */
//    fun detach() {
//        if (instance != null) {
//            instance!!.wXApi.detach()
//        }
//        instance = null
//    }
}