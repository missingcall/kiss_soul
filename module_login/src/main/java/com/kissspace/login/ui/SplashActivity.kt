package com.kissspace.login.ui

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.kissspace.common.base.BaseActivity
import com.kissspace.common.ext.safeClick
import com.kissspace.common.flowbus.Event
import com.kissspace.common.flowbus.FlowBus
import com.kissspace.common.model.AdBean
import com.kissspace.common.router.RouterPath
import com.kissspace.common.router.jump
import com.kissspace.common.util.countDown
import com.kissspace.common.util.mmkv.MMKVProvider
import com.kissspace.login.http.LoginApi
import com.kissspace.module_login.R
import com.kissspace.module_login.databinding.LoginActivitySplashBinding
import com.kissspace.network.net.Method
import com.kissspace.network.net.request
import com.kissspace.util.*
import kotlinx.coroutines.Job


/**
 * @Author gaohangbo
 * @Date 2023/3/17 09:41.
 * @Describe 广告页
 */
class SplashActivity : BaseActivity(R.layout.login_activity_splash) {
    private val mBinding by viewBinding<LoginActivitySplashBinding>()
    private var job: Job? = null
    private var adBean: AdBean? = null
    private var mPlayingPos = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }
    override fun initView(savedInstanceState: Bundle?) {
        //先判断是否后台重启
        if (!isTaskRoot) {
            finish()
            hideKeyboard()
            return
        }

        mBinding.tvLeftTime.safeClick {
            goLogin()
        }

        mBinding.videoView.postRunnable {
            val uri = Uri.parse("android.resource://" + packageName + "/" + R.raw.app_splash_video)
            mBinding.videoView.setFixedSize(1200, 2500)
            mBinding.videoView.setVideoURI(uri)
            mBinding.videoView.start()
        }
        mBinding.videoView.setOnCompletionListener {
            goNext()
        }
        mBinding.videoView.setOnErrorListener { _, _, _ ->
            goNext()
            true
        }
    }

    private fun goNext(){
        if (MMKVProvider.isAgreeProtocol) {
            getAd()
        }else {
            goLogin()
        }
    }

    private fun initData() {
        if (adBean == null) {
            goLogin()
        } else {
            mBinding.videoView.visibility = View.GONE
            mBinding.ivAd.visibility = View.VISIBLE
            mBinding.ivAd.loadImage(adBean?.imgUrl)
            job = countDown(
                adBean?.displayTime?.toLong().orZero(),
                scope = lifecycleScope,
                onTick = { left ->
                    if (left == 0L) {
                        goLogin()
                    } else {
                        mBinding.tvLeftTime.text = "跳过 $left"
                    }
                })
            mBinding.ivAd.safeClick {
                adBean?.link?.let {
                    goLogin()
                    jump(RouterPath.PATH_WEBVIEW, "url" to it, "showTitle" to true)
                }
            }
        }
    }

    private fun getAd() {
        val params = mutableMapOf<String, Any?>()
        params["osType"] = "001"
        request<AdBean?>(LoginApi.API_GET_AD, Method.GET, params, onSuccess = {
            adBean = it
            initData()
        }, onError = {
            goLogin()
        })
    }

    private fun goLogin() {
        job?.cancel()
        removeRunnable(this)
        jump(RouterPath.PATH_LOGIN)
        finish()
    }

    override fun onDestroy() {
        kotlin.runCatching {
            mBinding.videoView.stopPlayback()
        }
        super.onDestroy()
    }

    override fun onPause() {
        mPlayingPos = mBinding.videoView.currentPosition
        mBinding.videoView.pause()
        super.onPause()

    }

    override fun onResume() {
        super.onResume()
        if (mPlayingPos > 0) {
            mBinding.videoView.start()
            mBinding.videoView.seekTo(mPlayingPos)
            mPlayingPos = 0
        } else {
            mBinding.videoView.start()
        }
    }
}