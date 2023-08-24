package com.kissspace.mine.ui.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import com.didi.drouter.annotation.Router
import com.hjq.bar.OnTitleBarListener
import com.hjq.bar.TitleBar
import com.kissspace.common.router.RouterPath
import com.kissspace.common.router.parseIntent
import com.kissspace.module_mine.R

@Router(uri = RouterPath.PATH_H5WebActivity)
class H5WebActivity : Activity() {
    private var tvTip: TextView? = null
    private var webview: WebView? = null
    private val url by parseIntent<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web)
        findViewById<TitleBar>(R.id.title_bar).setOnTitleBarListener(object : OnTitleBarListener {
            override fun onLeftClick(titleBar: TitleBar?) {
                super.onLeftClick(titleBar)
                finish()
            }
        })
        tvTip = findViewById<View>(R.id.tv_tip) as TextView
        // 应用过程中将其隐藏掉效果更佳
        webview = findViewById<View>(R.id.webview) as WebView
        val webSettings = webview!!.settings
        // 设置支持JS
        webSettings.javaScriptEnabled = true
        // 设置可以访问文件
        webSettings.allowFileAccess = true
        // 设置支持缩放
        webSettings.builtInZoomControls = true
        webSettings.cacheMode = WebSettings.LOAD_NO_CACHE
        // 使用localStorage则必须打开
        webSettings.domStorageEnabled = true
        webSettings.setGeolocationEnabled(true)
        webview?.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                tvTip?.visibility = View.GONE
                webview?.visibility = View.VISIBLE
            }

            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                if (url != null) {
                    return if (!url.startsWith("http") && intent.resolveActivity(packageManager) != null) {
                        startActivity(intent)
                        true
                    } else {
                        false
                    }
                }
                return false
            }

            override fun onReceivedError(
                view: WebView,
                errorCode: Int,
                description: String,
                failingUrl: String
            ) {
                super.onReceivedError(view, errorCode, description, failingUrl)
                Log.e("aaaaaaaaa", "dsdddd" + errorCode + "description" + failingUrl)
            }
        }
        webview?.loadUrl(url)
    }

    override fun onRestart() {
        super.onRestart()
        finish()
    }
}