package com.kissspace.common.umeng

import android.content.Context
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ResourceUtils
import com.blankj.utilcode.util.ToastUtils
import com.umeng.umverify.UMVerifyHelper
import com.umeng.umverify.listener.UMTokenResultListener
import com.umeng.umverify.view.UMAuthUIConfig
import com.kissspace.util.resToColor
import com.kissspace.module_common.R
import org.json.JSONObject

 class QuickLoginHelper {
    private val TAG = "QuickLoginHelper"
    private lateinit var mVerifyHelper: UMVerifyHelper
    fun initUmengAuth(context: Context, block: String.() -> Unit) {
        val verifyListener = object : UMTokenResultListener {
            override fun onTokenSuccess(token: String) {
                LogUtils.e(TAG, "onTokenSuccess $token")
                val json = JSONObject(token)
                if (json.getString("code") == "600000") {
                    block(json.getString("token"))
                }
            }

            override fun onTokenFailed(s: String?) {
                LogUtils.e(TAG, "onTokenFailed $s")
                ToastUtils.showShort(s)
            }
        }
        mVerifyHelper = UMVerifyHelper.getInstance(context, verifyListener)
        mVerifyHelper.setAuthSDKInfo("ey0unUFzP+N37BvjyRl8f7GLB59zY2p/2GTrAMOyAS+HWvCMBxIKr9uFjRMXKK59JcV4YL1q6WxpJoHPe6myHs3EEXfUxAKWHCTj/1ekHkuSC2epg6aCnZr8LM5LIp/PxYXEK2wElIcwdzvtwXxsujAq6V9QJV+wglXscbGE8OY+mfw+eSZRpl267ZODCEoE3xkyLCkTwQYy0BgqYtdf/2EOYzoNdpDzi03PQ2sTElW91eIkzz2mB7ZBT5K15nKaDpnhK5HSUjqrQpqeGlz8Zeup1ciZQytPEOfYhs2k0XcJ5ZTD0rAw9B6ZR6fSrnc1")
        mVerifyHelper.checkEnvAvailable(2)
        mVerifyHelper.setUIClickListener { code, context, jsonString ->

        }
        mVerifyHelper.setAuthUIConfig(
            UMAuthUIConfig.Builder()
                .setStatusBarHidden(true)
                .setNavHidden(true)
                .setLogoHidden(false)
                .setSloganHidden(true)
                .setCheckboxHidden(false)
                .setHiddenLoading(true)
                .setLogBtnText("本机号码一键登录")
                .setLogBtnHeight(44)
                .setLogBtnWidth(311)
                .setLogBtnBackgroundDrawable(ResourceUtils.getDrawable(R.drawable.common_drawable_login_btn))
                .setLogBtnTextColor(R.color.common_white.resToColor())
                .setLogBtnWidth(300)
                .setPrivacyState(true)
                .setUncheckedImgDrawable(ResourceUtils.getDrawable(R.mipmap.common_icon_agreement_normal))
                .setCheckedImgDrawable(ResourceUtils.getDrawable(R.mipmap.common_icon_agreement_selected))
                .setLogoHeight(50)
                .create()
        )
        mVerifyHelper.getLoginToken(context, 1000)
    }

    fun quitLogin() {
        mVerifyHelper.quitLoginPage()
    }
}