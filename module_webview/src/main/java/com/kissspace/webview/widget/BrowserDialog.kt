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
import com.didi.drouter.api.DRouter
import com.kissspace.common.config.Constants
import com.kissspace.common.config.Constants.TypeFaceRecognition
import com.kissspace.common.flowbus.Event
import com.kissspace.common.flowbus.FlowBus
import com.kissspace.common.http.getSelectPayChannelList
import com.kissspace.common.http.getUserInfo
import com.kissspace.common.provider.IPayProvider
import com.kissspace.common.router.RouterPath
import com.kissspace.common.router.RouterPath.PATH_USER_IDENTITY_AUTH
import com.kissspace.common.router.jump
import com.kissspace.common.util.format.Format
import com.kissspace.common.util.mmkv.MMKVProvider
import com.kissspace.common.util.setApplicationValue
import com.kissspace.util.logE
import com.kissspace.webview.init.WebViewCacheHolder
import com.kissspace.webview.jsbridge.BridgeWebView
import com.kissspace.webview.jsbridge.CommonJsBridge
import com.kissspace.webview.jsbridge.JSName
import java.text.DecimalFormat
import com.kissspace.module_common.databinding.CommonDialogBrowserBinding

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
    private var isHideStatusBar: Boolean=false

    companion object {
        fun newInstance(url: String,isHideStatusBar:Boolean?=false) = BrowserDialog().apply {
            arguments = bundleOf("url" to url,"isHideStatusBar" to isHideStatusBar)
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.Theme)
        arguments?.let {
            mUrl = it.getString("url")!!
            isHideStatusBar =it.getBoolean("isHideStatusBar")
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        mBinding = CommonDialogBrowserBinding.inflate(layoutInflater)
        dialog.setContentView(mBinding.root)
        if(isHideStatusBar){
            dialog.window?.decorView?.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_FULLSCREEN  // 隐藏状态栏
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    )
        }
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

        val commonJsBridge = CommonJsBridge { name, param1,param2 ->
            when (name) {
                JSName.JSNAME_CLOSE_WEB -> {
                    dialog.dismiss()
                }

                JSName.JSNAME_IS_WATER -> {
                    MMKVProvider.isShowWaterAnimation = param1 as Boolean
                }

                JSName.JSNAME_GET_COIN -> {
                    getUserInfo(onSuccess = { userinfo ->
                        DecimalFormat("0")
                        refreshCoin(
                            Format.O_OO.format(
                                userinfo.coin
                            )
                        )
                        logE(
                            "----getCoin------" + Format.O_OO.format(
                                userinfo.coin
                            )
                        )
                    })
                }

                JSName.JSNAME_jumpH5UserIdentity -> {
                    jumpH5UserIdentity(param1 as String)
                }

                JSName.JSNAME_showPayDialogFragment -> {
                    getSelectPayChannelList { list ->
                        DRouter.build(IPayProvider::class.java).getService()
                            .showPayDialogFragment(parentFragmentManager, list)
                    }
                }

                JSName.JSNAME_GoConversation -> {
                    logE("userId___$param1")
                    jump(
                        RouterPath.PATH_CHAT,
                        "account" to ("djs${param1}"),
                        "userId" to (param1 ?: "")
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

        FlowBus.observerEvent<Event.H5InterstellarEvent>(this) {
            val method = "javascript:playStarGame('" + it.content + "')"
            mWebView.evaluateJavascript(method,null)
        }
        return dialog
    }


    //在 Android 调用 H5 中的 JavaScript 函数：
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

