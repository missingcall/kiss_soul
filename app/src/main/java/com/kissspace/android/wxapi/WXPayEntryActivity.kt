package com.kissspace.android.wxapi

import com.kissspace.pay.wechat.WechatPay.onResp
import android.app.Activity
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler
import com.tencent.mm.opensdk.openapi.IWXAPI
import android.os.Bundle
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import android.content.Intent
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.kissspace.common.config.ConstantsKey
import com.kissspace.util.logE

class WXPayEntryActivity : Activity(), IWXAPIEventHandler {
    private var api: IWXAPI? = null
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        api = WXAPIFactory.createWXAPI(this, ConstantsKey.WECHAT_APPID)
        api?.handleIntent(intent, this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        api?.handleIntent(intent, this)
    }

    override fun onReq(req: BaseReq) {

    }
    override fun onResp(resp: BaseResp) {
        onResp(resp.errCode)
        logE("onPayFinish, errCode = " + resp.errCode)
        finish()
    }
}