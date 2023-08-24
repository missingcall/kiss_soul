package com.kissspace.login.umeng

import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Build
import android.view.Gravity
import android.view.View
import com.blankj.utilcode.util.ResourceUtils
import com.umeng.umverify.UMResultCode
import com.umeng.umverify.UMVerifyHelper
import com.umeng.umverify.listener.UMPreLoginResultListener
import com.umeng.umverify.listener.UMTokenResultListener
import com.umeng.umverify.model.UMTokenRet
import com.umeng.umverify.view.UMAuthUIConfig
import com.kissspace.common.config.ConstantsKey
import com.kissspace.common.config.isReleaseServer
import com.kissspace.login.umeng.callback.QuickLoginCallback
import com.kissspace.module_login.R
import com.kissspace.util.application

/**
 *@author: adan
 *@date: 2023-04-01
 *@Description: 快速登录管理类
 */
class QuickLoginManager {

    private lateinit var mVerifyHelper: UMVerifyHelper

    private var mCallback : QuickLoginCallback? = null

    /**
     * 初始化
     * @param callback 结果
     */
    fun init(callback: QuickLoginCallback){
        mCallback = callback
        mVerifyHelper = UMVerifyHelper.getInstance(application,object :UMTokenResultListener{
            override fun onTokenSuccess(p0: String) {
                mCallback?.initializationSuccess()
                try {
                    val pTokenRet = UMTokenRet.fromJson(p0)
                    if (UMResultCode.CODE_ERROR_ENV_CHECK_SUCCESS == pTokenRet.code) {
                        accelerateLoginPage(5000)
                    }
                }catch (e:Exception){

                }
            }
            override fun onTokenFailed(p0: String) {
            }
        })
        mVerifyHelper.setAuthSDKInfo( if (isReleaseServer) {
            ConstantsKey.UMENGKEY_AUTH_SDK_INFO_RELEASE
        } else {
            ConstantsKey.UMENGKEY_AUTH_SDK_INFO_DEBUG
        })
        mVerifyHelper.checkEnvAvailable(UMVerifyHelper.SERVICE_TYPE_LOGIN)
    }

    /**
     * 拉起授权页 登录
     */
    fun quickLogin(){
        if (!this::mVerifyHelper.isInitialized){
            return
        }
        configAuthPage()
        mVerifyHelper.setAuthListener(object :UMTokenResultListener{
            override fun onTokenSuccess(token: String) {
                try {
                    val tokenRet = UMTokenRet.fromJson(token)
                    if (UMResultCode.CODE_GET_TOKEN_SUCCESS == tokenRet.code) {
                        mCallback?.onQuickLoginSuccess(tokenRet.token)
                    }
                }catch (e:Exception){
                    mCallback?.onQuickLoginFail()
                }
            }

            override fun onTokenFailed(p0: String) {
                mCallback?.onQuickLoginFail()
            }

        })
        mVerifyHelper.getLoginToken(application,5000)
    }

    fun quitLogin() {
        if (!this::mVerifyHelper.isInitialized){
            return
        }
        mVerifyHelper.quitLoginPage()
    }

    /**
     * 在不是一进app就需要登录的场景 建议调用此接口 加速拉起一键登录页面
     * 等到用户点击登录的时候 授权页可以秒拉
     * 预取号的成功与否不影响一键登录功能，所以不需要等待预取号的返回。
     * @param timeout
     */
    private fun accelerateLoginPage(timeout: Int) {
        mVerifyHelper.accelerateLoginPage(timeout, object : UMPreLoginResultListener {
            override fun onTokenSuccess(s: String) {
                mVerifyHelper?.releasePreLoginResultListener()
            }

            override fun onTokenFailed(s: String, s1: String) {
                mVerifyHelper?.releasePreLoginResultListener()
            }
        })
    }

    /**
     * UI配置
     */
    private fun configAuthPage(){
        var authPageOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
        if (Build.VERSION.SDK_INT == 26) {
            authPageOrientation = ActivityInfo.SCREEN_ORIENTATION_BEHIND
        }
        mVerifyHelper.setAuthUIConfig(
            UMAuthUIConfig.Builder()
                .setLightColor(true)
                .setAppPrivacyColor(
                    application.resources.getColor(com.kissspace.module_common.R.color.common_white),
                    application.resources.getColor(com.kissspace.module_common.R.color.color_FFFD62)
                )
                .setNavHidden(true)
                .setLogBtnToastHidden(true)
                .setNavReturnHidden(true)
                .setLogoHidden(false)
                .setSloganHidden(true)
                .setSwitchAccHidden(false)
                .setPrivacyState(true)
                .setCheckboxHidden(false)
                .setStatusBarColor(Color.TRANSPARENT)
                .setStatusBarUIFlag(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
                .setWebNavTextSizeDp(20)
                .setNumberSizeDp(20)
                .setNumberLayoutGravity(Gravity.CENTER)
                .setNumFieldOffsetY(263)
                .setNumberColor(Color.BLACK)
                .setAuthPageActIn("in_activity", "out_activity")
                .setAuthPageActOut("in_activity", "out_activity")
                .setVendorPrivacyPrefix("《")
                .setVendorPrivacySuffix("》")
                .setLogoOffsetY(150)
                .setLogoWidth(90)
                .setLogoHeight(90)
                .setPageBackgroundDrawable(application.resources.getDrawable(com.kissspace.module_common.R.mipmap.common_page_background))
                .setLogoImgDrawable(application.resources.getDrawable(com.kissspace.module_common.R.mipmap.common_app_logo))
                .setNumberColor(application.resources.getColor(com.kissspace.module_common.R.color.common_white))
                .setNumberSize(28)
                .setLogBtnText("本机号码一键登录")
                .setLogBtnTextColor(application.resources.getColor(com.kissspace.module_common.R.color.color_FFFD62))
                .setHiddenLoading(false)
                .setLogBtnBackgroundDrawable(application.resources.getDrawable(com.kissspace.module_common.R.mipmap.common_button_bg))
                .setLogBtnOffsetY(315)
                .setSwitchOffsetY(385)
                .setSwitchAccTextColor(application.resources.getColor(com.kissspace.module_common.R.color.common_white80))
                .setSwitchAccTextSizeDp(13)
                .setUncheckedImgDrawable(application.resources.getDrawable(R.mipmap.login_icon_not_agree))
                .setCheckedImgDrawable(application.resources.getDrawable(R.mipmap.login_icon_agree))
                .setScreenOrientation(authPageOrientation)
                .create()
        )
    }

    fun release() {
        if (!this::mVerifyHelper.isInitialized){
            return
        }
        mCallback = null
        mVerifyHelper.releasePreLoginResultListener()
        mVerifyHelper.setAuthListener(null)
    }
}