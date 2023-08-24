package com.kissspace.setting.ui.activity

import android.os.Bundle
import androidx.activity.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.blankj.utilcode.util.AppUtils
import com.didi.drouter.annotation.Router
import com.didi.drouter.api.DRouter
import com.kissspace.common.base.BaseActivity
import com.kissspace.common.config.Constants
import com.kissspace.common.router.jump
import com.kissspace.common.ext.safeClick
import com.kissspace.common.ext.setTitleBarListener
import com.kissspace.common.provider.IAppProvider
import com.kissspace.common.router.RouterPath
import com.kissspace.common.util.getH5Url
import com.kissspace.module_setting.R
import com.kissspace.module_setting.databinding.SettingActivityHelpBinding
import com.kissspace.network.result.collectData
import com.kissspace.setting.viewmodel.HelpViewModel
import com.kissspace.util.appVersionName
import com.kissspace.util.toast

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/12/28 18:15
 * @Description: 帮助页面
 *
 */
@Router(path = RouterPath.PATH_HELP)
class HelpActivity : com.kissspace.common.base.BaseActivity(R.layout.setting_activity_help) {
    private val mBinding by viewBinding<SettingActivityHelpBinding>()
    private val mViewModel by viewModels<HelpViewModel>()
    override fun initView(savedInstanceState: Bundle?) {
        setTitleBarListener(mBinding.titleBar)
        mBinding.tvCode.text = "v${appVersionName}"
        mBinding.layoutPersonalInformation.safeClick {
            jump(
                RouterPath.PATH_WEBVIEW,
                "url" to getH5Url(Constants.H5.personalInformationUrl),
                "showTitle" to true
            )
        }
        mBinding.layoutThreePartyResources.safeClick {
            jump(
                RouterPath.PATH_WEBVIEW,
                "url" to getH5Url(Constants.H5.threePartyResourcesUrl),
                "showTitle" to true
            )
        }
        mBinding.layoutVersionUpgrade.safeClick {
            mViewModel.checkVersion()
        }
    }

    override fun createDataObserver() {
        super.createDataObserver()

        collectData(mViewModel.checkVersionEvent, onSuccess = {
            if (it.intVersion > AppUtils.getAppVersionCode()) {
                val service = DRouter.build(IAppProvider::class.java).getService()
                service.showUpgradeDialog(supportFragmentManager, it)
            } else {
                toast("暂无更新")
            }
        })
    }

}