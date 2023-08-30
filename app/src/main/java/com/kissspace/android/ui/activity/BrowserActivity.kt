package com.kissspace.android.ui.activity

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.widget.LinearLayout
import by.kirich1409.viewbindingdelegate.viewBinding
import com.blankj.utilcode.util.LogUtils
import com.didi.drouter.annotation.Router
import com.hjq.bar.OnTitleBarListener
import com.hjq.bar.TitleBar
import com.kissspace.android.R
import com.kissspace.android.databinding.ActivityBrowserBinding
import com.kissspace.common.base.BaseActivity
import com.kissspace.common.config.Constants
import com.kissspace.common.flowbus.Event
import com.kissspace.common.flowbus.FlowBus
import com.kissspace.common.http.getSelectPayChannelList
import com.kissspace.common.model.wallet.WalletRechargeList
import com.kissspace.common.router.RouterPath
import com.kissspace.common.router.RouterPath.PATH_CHAT
import com.kissspace.common.router.RouterPath.PATH_USER_PROFILE
import com.kissspace.common.router.jump
import com.kissspace.common.router.parseIntent
import com.kissspace.common.util.getH5Url
import com.kissspace.common.util.jumpRoom
import com.kissspace.pay.ui.PayDialogFragment
import com.kissspace.util.immersiveStatusBar
import com.kissspace.util.logE
import com.kissspace.util.postRunnable
import com.kissspace.util.statusBarHeight
import com.kissspace.webview.init.WebViewCacheHolder
import com.kissspace.webview.jsbridge.BridgeWebView
import com.kissspace.webview.jsbridge.CommonJsBridge
import com.kissspace.webview.jsbridge.JSName
import com.tencent.smtt.export.external.interfaces.WebResourceError
import com.tencent.smtt.export.external.interfaces.WebResourceRequest
import com.tencent.smtt.sdk.WebChromeClient
import com.tencent.smtt.sdk.WebView
import com.tencent.smtt.sdk.WebViewClient


@Router(uri = RouterPath.PATH_WEBVIEW)
class BrowserActivity : BaseActivity(R.layout.activity_browser) {

    private val mBinding by viewBinding<ActivityBrowserBinding>()

    private lateinit var mWebView: BridgeWebView

    private val url by parseIntent<String>()

    private var showTitle by parseIntent<Boolean>(defaultValue = false)

    private var showTitleBarMargin by parseIntent<Boolean>(defaultValue = false)

    override fun initView(savedInstanceState: Bundle?) {
        initTitle()
        initWebView()
    }

    private fun initTitle() {
        if (showTitle) {
            mBinding.titleBar.visibility = View.VISIBLE
        } else {
            immersiveStatusBar(false)
            mBinding.titleBar.visibility = View.GONE
            if(showTitleBarMargin){
                val lp = mBinding.viewStatusBar.layoutParams
                lp.height = statusBarHeight
                mBinding.viewStatusBar.layoutParams = lp
            }
        }
        LogUtils.e("url---",url)
        mBinding.titleBar.setOnTitleBarListener(object : OnTitleBarListener {
            override fun onLeftClick(titleBar: TitleBar?) {
                super.onLeftClick(titleBar)
                mWebView.postRunnable {
                    if (mWebView.canGoBack()) {
                        mWebView.goBack()
                    } else {
                        finish()
                    }
                }
            }
        })
    }

    private fun initWebView() {
        mWebView = WebViewCacheHolder.acquireWebViewInternal(this)
        val layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        )
        mWebView.setBackgroundColor(resources.getColor(android.R.color.transparent))
        val webSettings: com.tencent.smtt.sdk.WebSettings? = mWebView.settings
        // 设置支持JS
        webSettings?.javaScriptEnabled = true
        // 设置可以访问文件
        webSettings?.allowFileAccess = true
        // 设置支持缩放
        webSettings?.builtInZoomControls = true
        webSettings?.cacheMode = WebSettings.LOAD_NO_CACHE
        // 使用localStorage则必须打开
        webSettings?.domStorageEnabled = true
        webSettings?.setGeolocationEnabled(true)
        mWebView.webViewClient= object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                logE("onPageFinished: ")
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                logE("onReceivedError: request= " + error.toString() + "onReceivedError: error= " + error.toString())
            }
        }

        mWebView.webChromeClient = object : WebChromeClient() {
            override fun onReceivedTitle(p0: WebView?, title: String?) {
                super.onReceivedTitle(p0, title)
                logE("onReceivedTitle$title")
                if (!isFinishing) {
                    if(title!="djcool"){
                        mBinding.titleBar.title = title
                    }
                }
            }
        }

        val commonJsBridge = CommonJsBridge { name, param1,param2 ->
            when (name) {
                JSName.JSNAME_CLOSE_WEB -> {
                    mWebView.postRunnable {
                        if (mWebView.canGoBack()) {
                            mWebView.goBack()
                        } else {
                            finish()
                        }
                    }
                }
                JSName.JSNAME_FINISH -> {
                    mWebView.postRunnable {
                        if (mWebView.canGoBack()) {
                            mWebView.goBack()
                        } else {
                            finish()
                        }
                    }
                }

                JSName.JSNAME_GoHomePage -> {
                    jump(PATH_USER_PROFILE, "userId" to (param1))
                }

                JSName.JSNAME_GoConversation -> {
                    jump(PATH_CHAT, "account" to ("djs${param1}"), "userId" to (param1 ?: ""))
                }

                JSName.JSNAME_followToRoom -> {
                    val listParam = (param1 as String).split(",")
                    if (listParam.size > 1) {
                        jumpRoom(crId = listParam[0], userId = listParam[1])
                    } else {
                        jumpRoom(crId = listParam[0])
                    }
                }

                JSName.JSNAME_goWearHeaddress -> {
                    jump(RouterPath.PATH_MY_DRESS_UP, "position" to 1)
                }

                JSName.JSNAME_showPayDialogFragment -> {
                    getSelectPayChannelList { list ->
                        PayDialogFragment.newInstance(list as ArrayList<WalletRechargeList>)
                            .show(supportFragmentManager)
                    }
                }

                JSName.JSNAME_navigateBackPage -> {
                    finish()
                }

                JSName.JSNAME_reportAFeed -> {
                    param2?.let {
                        jump(
                            RouterPath.PATH_REPORT,
                            "reportType" to Constants.ReportType.USER.type,
                            "userId" to it
                        )
                    }
                }
                JSName.JSNAME_jumpDynamicDetail -> {
                    jump(
                        RouterPath.PATH_WEBVIEW,
                        "url" to getH5Url(Constants.H5.dynamicDetail + "?id=${param1}"),
                        "showTitleBarMargin" to true
                    )
                }
            }
        }
        mWebView.addJavascriptInterface(commonJsBridge, "android")
        mBinding.container.addView(mWebView, layoutParams)
        mWebView.loadUrl(url)
    }

    override fun createDataObserver() {
        super.createDataObserver()
        FlowBus.observerEvent<Event.H5InterstellarEvent>(this) {
            val method = "playStarGame(${it.content})"
            mWebView.evaluateJavascript(method){
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        WebViewCacheHolder.prepareWebView()
    }

}