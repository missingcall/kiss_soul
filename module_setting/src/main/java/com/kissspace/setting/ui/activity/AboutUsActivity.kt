package com.kissspace.setting.ui.activity

import android.annotation.SuppressLint
import android.os.Bundle
import by.kirich1409.viewbindingdelegate.viewBinding
import com.didi.drouter.annotation.Router
import com.kissspace.common.base.BaseActivity
import com.kissspace.common.config.BaseUrlConfig
import com.kissspace.common.config.Constants
import com.kissspace.common.router.jump
import com.kissspace.common.ext.safeClick
import com.kissspace.common.ext.setTitleBarListener
import com.kissspace.common.router.RouterPath
import com.kissspace.common.util.getH5Url
import com.kissspace.common.util.mmkv.MMKVProvider
import com.kissspace.module_setting.R
import com.kissspace.module_setting.databinding.SettingActivityAboutUsBinding
import com.kissspace.util.appVersionName
import com.kissspace.util.isAppDebug
import com.kissspace.util.logE
import java.io.IOException
import java.util.*

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/12/28 18:15
 * @Description: 帮助页面
 *
 */
@Router(path = RouterPath.PATH_ABOUT_US)
class AboutUsActivity : com.kissspace.common.base.BaseActivity(R.layout.setting_activity_about_us) {
    private val mBinding by viewBinding<SettingActivityAboutUsBinding>()

    @SuppressLint("SetTextI18n")
    override fun initView(savedInstanceState: Bundle?) {
        setTitleBarListener(mBinding.titleBar)
        if (isAppDebug) {
            val props = Properties();
            try {
                props.load(applicationContext.assets.open("version.properties"))
                val version = props.getProperty("VERSION_TEST_CODE")
                logE("version$version")
                mBinding.tvVersions.text = "测试版本号(${version})"
                when (BaseUrlConfig.BASEURL_RELEASE) {
                    BaseUrlConfig.BASEURL_RELEASE -> {
                        mBinding.tvVersions.text =
                            java.lang.StringBuilder(mBinding.tvVersions.text).append("正式")
                    }

//                    BaseUrlConfig.BASEURL_PRE -> {
//                        mBinding.tvVersions.text =
//                            java.lang.StringBuilder(mBinding.tvVersions.text).append("预发")
//                    }

//                    BaseUrlConfig.BASEURL_TEST -> {
//                        mBinding.tvVersions.text =
//                            java.lang.StringBuilder(mBinding.tvVersions.text).append("测试")
//                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                logE("---" + e.message)
            }
        } else {
            mBinding.tvVersions.text = "当前版本：${appVersionName}"
        }

        mBinding.layoutPrivacyUrl.safeClick {
            jump(
                RouterPath.PATH_WEBVIEW,
                "url" to getH5Url(Constants.H5.privacyUrl),
                "showTitle" to true
            )
        }
        mBinding.layoutUserAgreement.safeClick {
            jump(
                RouterPath.PATH_WEBVIEW,
                "url" to getH5Url(Constants.H5.userAgreementUrl),
                "showTitle" to true
            )
        }
        mBinding.layoutUserActionAgreement.safeClick {
            jump(
                RouterPath.PATH_WEBVIEW,
                "url" to getH5Url(Constants.H5.userActionAgreementUrl),
                "showTitle" to true
            )
        }
        mBinding.layoutCommunityRule.safeClick {
            jump(
                RouterPath.PATH_WEBVIEW,
                "url" to getH5Url(Constants.H5.anchorRulUrl),
                "showTitle" to true
            )
        }

        mBinding.layoutPartyRule.safeClick {
            jump(
                RouterPath.PATH_WEBVIEW,
                "url" to getH5Url(Constants.H5.liveRoomRulUrl),
                "showTitle" to true
            )
        }

        mBinding.layoutBroadRule.safeClick {
            jump(
                RouterPath.PATH_WEBVIEW,
                "url" to getH5Url(Constants.H5.applicationRulUrl),
                "showTitle" to true
            )
        }

    }

}