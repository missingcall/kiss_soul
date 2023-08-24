package com.kissspace.webview.widget

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.kissspace.pay.ui.PayDialogFragment
import com.kissspace.common.config.Constants
import com.kissspace.common.config.Constants.TypeFaceRecognition
import com.kissspace.common.router.jump
import com.kissspace.common.flowbus.Event
import com.kissspace.common.flowbus.FlowBus
import com.kissspace.common.http.getSelectPayChannelList
import com.kissspace.common.router.RouterPath
import com.kissspace.common.router.RouterPath.PATH_USER_IDENTITY_AUTH
import com.kissspace.common.util.mmkv.MMKVProvider
import com.kissspace.common.util.setApplicationValue
import com.kissspace.module_common.databinding.CommonDialogBrowserBinding
import com.kissspace.common.http.getUserInfo
import com.kissspace.common.model.wallet.WalletRechargeList
import com.kissspace.webview.init.WebViewCacheHolder
import com.kissspace.webview.jsbridge.BridgeWebView
import com.kissspace.webview.jsbridge.CommonJsBridge
import com.kissspace.webview.jsbridge.JSName
import com.kissspace.webview.jsbridge.*
import com.kissspace.util.logE
import java.text.DecimalFormat

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/12/13 17:49
 * @Description: 承载H5的弹窗
 *
 */
class BrowserDialog : DialogFragment() {
    private lateinit var mBinding: CommonDialogBrowserBinding
    private lateinit var mWebView: BridgeWebView
    private lateinit var mUrl: String

    companion object {
        fun newInstance(url: String) = BrowserDialog().apply {
            arguments = bundleOf("url" to url)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.Theme)
        arguments?.let {
            mUrl = it.getString("url")!!
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        mBinding = CommonDialogBrowserBinding.inflate(layoutInflater)
        dialog.setContentView(mBinding.root)
        dialog.window?.attributes?.apply {
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
            dimAmount = 0F
            flags = flags or WindowManager.LayoutParams.FLAG_DIM_BEHIND
        }
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        mWebView = WebViewCacheHolder.acquireWebViewInternal(requireContext())
        mWebView.setBackgroundColor(Color.TRANSPARENT)
        val layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        val commonJsBridge = CommonJsBridge { name, param ->
            when (name) {
                JSName.JSNAME_CLOSE_WEB -> {
                    dialog.dismiss()
                }

                JSName.JSNAME_IS_WATER -> {
                    MMKVProvider.isShowWaterAnimation = param as Boolean
                }

                JSName.JSNAME_GET_COIN -> {
                    getUserInfo(onSuccess = { userinfo ->
                        DecimalFormat("0")
                        refreshCoin(
                            com.kissspace.common.util.format.Format.O_OO.format(
                                userinfo.coin
                            )
                        )
                        logE(
                            "----getCoin------" + com.kissspace.common.util.format.Format.O_OO.format(
                                userinfo.coin
                            )
                        )
                    })
                }

                JSName.JSNAME_jumpH5UserIdentity -> {
                    jumpH5UserIdentity(param as String)
                }

                JSName.JSNAME_showPayDialogFragment -> {
                    getSelectPayChannelList { list ->
                        PayDialogFragment.newInstance(list as ArrayList<WalletRechargeList>)
                            .show(parentFragmentManager)
                    }
                }

                JSName.JSNAME_GoConversation -> {
                    logE("userId___$param")
                    jump(
                        RouterPath.PATH_CHAT,
                        "account" to ("djs${param}"),
                        "userId" to (param ?: "")
                    )
                }

                JSName.JSNAME_showGiftPopup -> {
                    FlowBus.post(Event.ShowLuckyGiftEvent)
                    dismiss()
                }
            }
        }
        mWebView.addJavascriptInterface(commonJsBridge, "android")
        mWebView.settings.javaScriptEnabled = true
        mBinding.container.addView(mWebView, layoutParams)
        mWebView.loadUrl(mUrl)
        logE("url$mUrl")
       // mWebView.loadUrl("http://192.168.8.49:8082/#/pages/game/wishPool")

        FlowBus.observerEvent<Event.RefreshCoin>(this) {
            getUserInfo(onSuccess = { userinfo ->
                refreshCoin(userinfo.coin.toString())
            })
        }

        FlowBus.observerEvent<Event.RefreshTree>(this) {
            refreshTree()
        }
        return dialog
    }


    //h5调用app，刷新金币
    private fun refreshCoin(coin: String) {
        mWebView.loadUrl("javascript:refreshCoin('$coin')")
    }


    //h5调用app，刷新树
    private fun refreshTree() {
        mWebView.loadUrl("javascript:refreshTree()")
    }


    //跳转到活体验证 实名认证
    private fun jumpH5UserIdentity(code: String) {
        logE("jumpH5UserIdentity")
        setApplicationValue(
            type = TypeFaceRecognition,
            value = Constants.FaceRecognitionType.CONSUMPTION.type.toString()
        )
        if (code == "51514") {
            jump(PATH_USER_IDENTITY_AUTH)
        } else if (code == "50138" || code == "50142" || code == "51516") {
            jump(RouterPath.PATH_USER_BIOMETRIC)
        }
    }

    override fun onStart() {
        super.onStart()
        val window = dialog?.window
        val width = ViewGroup.LayoutParams.MATCH_PARENT
        val height = ViewGroup.LayoutParams.MATCH_PARENT
        window?.setLayout(width, height)
    }

    override fun onDestroy() {
        super.onDestroy()
        WebViewCacheHolder.prepareWebView()
    }

}

