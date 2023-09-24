package com.kissspace.dynamic.ui.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import by.kirich1409.viewbindingdelegate.viewBinding
import com.kissspace.dynamic.ui.activity.SendDynamicActivity.Companion.SendDynamicActivityResult
import com.kissspace.common.base.BaseFragment
import com.kissspace.common.config.Constants
import com.kissspace.common.ext.previewPicture
import com.kissspace.common.flowbus.Event
import com.kissspace.common.flowbus.FlowBus
import com.kissspace.common.router.RouterPath
import com.kissspace.common.router.jump
import com.kissspace.common.util.getH5Url
import com.kissspace.common.util.jumpRoom
import com.kissspace.module_dynamic.R
import com.kissspace.module_dynamic.databinding.DynamicWebviewBinding
import com.kissspace.util.logE
import com.kissspace.webview.init.WebViewCacheHolder
import com.kissspace.webview.jsbridge.BridgeWebView
import com.kissspace.webview.jsbridge.CommonJsBridge
import com.kissspace.webview.jsbridge.JSName
import com.tencent.smtt.export.external.interfaces.WebResourceError
import com.tencent.smtt.export.external.interfaces.WebResourceRequest
import com.tencent.smtt.sdk.WebChromeClient
import com.tencent.smtt.sdk.WebView
import com.tencent.smtt.sdk.WebViewClient


/**
 * @Author gaohangbo
 * @Date 2023/7/20 10:25.
 * @Describe 动态主页
 */
class DynamicFragment : BaseFragment(R.layout.dynamic_webview) {

    private val mBinding by viewBinding<DynamicWebviewBinding>()

    private var mWebView: BridgeWebView? = null

    var activityResult: ActivityResultLauncher<Intent>? = null

    override fun initView(savedInstanceState: Bundle?) {

        mWebView = context?.let { WebViewCacheHolder.acquireWebViewInternal(it) }

        mWebView?.setBackgroundColor(resources.getColor(android.R.color.transparent))

        val webSettings: com.tencent.smtt.sdk.WebSettings? = mWebView?.settings
        // 设置支持JS
        webSettings?.javaScriptEnabled = true
        // 设置可以访问文件
        webSettings?.allowFileAccess = true
        // 设置支持缩放
        webSettings?.builtInZoomControls = false
        // 使用localStorage则必须打开
        webSettings?.setGeolocationEnabled(true)
        //设置对应的浏览器
        mWebView?.webChromeClient = object : WebChromeClient() {
            override fun onReceivedTitle(p0: WebView?, title: String?) {
                super.onReceivedTitle(p0, title)
                logE("onReceivedTitle$title")
            }
        }
        mWebView?.webViewClient = object : WebViewClient() {
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


        val commonJsBridge = CommonJsBridge { name, param1, param2 ->
            when (name) {
                JSName.JSNAME_GoHomePage -> {
                    jump(RouterPath.PATH_USER_PROFILE, "userId" to (param1 ?: ""))
                }

                JSName.JSNAME_GoConversation -> {
                    jump(
                        RouterPath.PATH_CHAT,
                        "account" to ("djs${param1}"),
                        "userId" to (param1 ?: "")
                    )
                }

                JSName.JSNAME_followToRoom -> {
                    val listParam = param1.toString().split(",")
                    if (listParam.size > 1) {
                        jumpRoom(crId = listParam[0], userId = listParam[1])
                    } else {
                        jumpRoom(crId = listParam[0])
                    }
                }

                JSName.JSNAME_goWearHeaddress -> {
                    jump(RouterPath.PATH_MY_DRESS_UP, "position" to 1)
                }

                JSName.JSNAME_sendDynamic -> {
                    jump(
                        RouterPath.PATH_DYNAMIC_SEND_FRIEND, activity = activity,
                        resultLauncher = activityResult
                    )
                }

                JSName.JSNAME_openPreviewImage -> {
                    val list = (param2 as Array<*>).toList()
                    previewPicture(
                        requireActivity(), (param1 as String).toInt(), null,
                        list as MutableList<String>
                    )
                }

                JSName.JSNAME_jumpDynamicDetail -> {
                    jump(
                        RouterPath.PATH_WEBVIEW,
                        "url" to getH5Url(Constants.H5.dynamicDetail + "?id=${param1}"),
                        "showTitleBarMargin" to true
                    )
                }

                JSName.JSNAME_jumpInteractiveMessages -> {
                    jump(
                        RouterPath.PATH_WEBVIEW,
                        "url" to getH5Url(Constants.H5.dynamicInteractiveMessages),
                        "showTitleBarMargin" to true
                    )
                }

                JSName.JSNAME_navigateBackPage -> {
                    if (mWebView?.canGoBack() == true) {
                        mWebView?.goBack()
                    }
                }
            }
        }
        mWebView?.addJavascriptInterface(commonJsBridge, "android")
        val layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        )
        mBinding.frameLayout.addView(mWebView, layoutParams)

        mWebView?.loadUrl(getH5Url(Constants.H5.dynamicUrl, true))

        // 设置返回键监听
        mWebView?.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_BACK && mWebView?.canGoBack() == true) {
                    // 如果 WebView 可以返回，则执行返回操作
                    mWebView?.goBack()
                    return@OnKeyListener true
                }
            }
            false
        })
        activityResult =
            registerForActivityResult(object : ActivityResultContract<Intent, String?>() {
                override fun createIntent(context: Context, input: Intent): Intent {
                    return input
                }

                override fun parseResult(resultCode: Int, intent: Intent?): String? {
                    return intent?.getStringExtra("result")
                }
            }) {
                if (it.equals(SendDynamicActivityResult)) {
                    mWebView?.loadUrl("javascript:refreshList()")
                }
            }
        //更新右上角通知
        FlowBus.observerEvent<Event.MsgRefreshDynamicNoticeEvent>(this) {
            mWebView?.loadUrl("javascript:refreshDynamicNotice()")
        }

        FlowBus.observerEvent<Event.RefreshChangeAccountEvent>(this) {
            mWebView?.loadUrl(getH5Url(Constants.H5.dynamicUrl, true))
        }


    }

    fun onBackPressed(): Boolean {
        if (mWebView?.canGoBack() == true) {
            mWebView?.goBack()
            return true
        }
        return false
    }


//    override fun onResume() {
//        super.onResume()
//        if(Constants.isNeedRefreshWebView){
//            val webUrl=getH5Url(Constants.H5.dynamicUrl, true)
//            logE("webUrl$webUrl")
//            mWebView?.loadUrl(getH5Url(Constants.H5.dynamicUrl, true))
//            Constants.isNeedRefreshWebView=false
//        }
//    }
}