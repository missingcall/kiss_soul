package com.kissspace.pay.alipay

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.os.Message
import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.alipay.sdk.app.PayTask
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.LogUtils
import com.kissspace.pay.utils.PayResultCallBack
import com.kissspace.util.logE

object AliPayTools {
    private const val SDK_PAY_FLAG = 1
    private var payResultCallBack: PayResultCallBack? = null

    private val lifecycleObserver = LifecycleEventObserver { source, event ->
        if (event == Lifecycle.Event.ON_DESTROY) {
            logE("messageOnDestroy")
            if (source is Activity) {
                mHandler.removeCallbacksAndMessages(null)
            } else if (source is Fragment) {
                mHandler.removeCallbacksAndMessages(null)
            }
            payResultCallBack = null
        }
    }

    private val mHandler: Handler = object : Handler(Looper.myLooper()!!) {
        override fun handleMessage(msg: Message) {
            LogUtils.e(GsonUtils.toJson(msg))
            when (msg.what) {
                SDK_PAY_FLAG -> {
                    val payResult =
                        AliPayResult(msg.obj as Map<String?, String?>)
                    // 对于支付结果，请商户依赖服务端的异步通知结果。同步通知结果，仅作为支付结束的通知。
                    // 同步返回需要验证的信息
                    val resultInfo = payResult.result
                    val resultStatus = payResult.resultStatus
                    if (resultStatus == "9000") {
                        payResultCallBack?.onSuccess()
                    } else {
                        payResultCallBack?.onError("支付失败")
                    }
                }
                else -> {}
            }
        }

    }

    /**
     * 支付 服务端返回orderInfo
     *
     * @param activity
     * @param orderInfo
     * @param payResultCallBack
     */
    fun aliPay(activity: Activity?, orderInfo: String?, onPayResultCallBack: PayResultCallBack?) {
        if (activity is ComponentActivity) {
            activity.lifecycle.addObserver(lifecycleObserver)
        }
        payResultCallBack = onPayResultCallBack
        val payRunnable = Runnable {
            val alipay = PayTask(activity)
            val result = alipay.payV2(orderInfo, true)
            val msg = Message()
            msg.what = SDK_PAY_FLAG
            msg.obj = result
            mHandler.sendMessage(msg)
        }
        val payThread = Thread(payRunnable)
        payThread.start()
    }

}