package com.kissspace.pay.alipay

import android.text.TextUtils

class AliPayResult(rawResult: Map<String?, String?>?) {
    /**
     * @return the resultStatus
     */
    var resultStatus: String? = null

    /**
     * @return the result
     */
    var result: String? = null

    /**
     * @return the memo
     */
    var memo: String? = null

    init {
        if (rawResult != null) {
            for (key in rawResult.keys) {
                if (TextUtils.equals(key, "resultStatus")) {
                    resultStatus = rawResult[key]
                } else if (TextUtils.equals(key, "result")) {
                    result = rawResult[key]
                } else if (TextUtils.equals(key, "memo")) {
                    memo = rawResult[key]
                }
            }
        }

    }

    override fun toString(): String {
        return ("resultStatus={" + resultStatus + "};memo={" + memo
                + "};result={" + result + "}")
    }
}