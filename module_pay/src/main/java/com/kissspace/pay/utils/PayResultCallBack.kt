package com.kissspace.pay.utils

import com.kissspace.common.util.customToast

/**
 * @Author gaohangbo
 * @Date 2023/2/6 15:22.
 * @Describe
 */

abstract class PayResultCallBack {

    /**
     * 支付成功
     */
    abstract fun onSuccess()


    abstract fun onError(errorContent: String)


    abstract fun onCancel()


//    /**
//     * 支付失败
//     *
//     * @param errorCode
//     */
//    fun onError(errorContent: String) {
//        customToast(errorContent)
//    }
//
//    /**
//     * 支付取消
//     */
//    fun onCancel() {
//        //customToast("支付取消")
//    }
}
