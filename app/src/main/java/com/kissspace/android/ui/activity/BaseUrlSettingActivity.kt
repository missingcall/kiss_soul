package com.kissspace.android.ui.activity

import android.content.Intent
import android.os.Bundle
import android.os.Process
import androidx.databinding.BaseObservable
import by.kirich1409.viewbindingdelegate.viewBinding
import com.didi.drouter.annotation.Router
import com.drake.brv.utils.addModels
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.kissspace.android.R
import com.kissspace.android.databinding.ActivitySettingBaseUrlBinding
import com.kissspace.common.config.BaseUrlConfig
import com.kissspace.common.config.isPreServer
import com.kissspace.common.config.isReleaseServer
import com.kissspace.common.config.isTestServer
import com.kissspace.common.ext.safeClick
import com.kissspace.common.ext.setTitleBarListener
import com.kissspace.common.router.RouterPath
import com.kissspace.common.util.*
import com.kissspace.common.util.mmkv.MMKVProvider
import com.kissspace.login.ui.SplashActivity
import com.kissspace.util.logE
import kotlin.system.exitProcess

/**
 *
 * @Author: nicko
 * @CreateDate: 2023/3/9 15:34
 * @Description: 环境切换设置
 *
 */

@Router(path = RouterPath.PATH_BASE_URL_SETTING)
class BaseUrlSettingActivity : com.kissspace.common.base.BaseActivity(R.layout.activity_setting_base_url) {
    private val mBinding by viewBinding<ActivitySettingBaseUrlBinding>()

    override fun initView(savedInstanceState: Bundle?) {
        setTitleBarListener(mBinding.titleBar)
        val urlList = mutableListOf<ServerListBean>()
        urlList.add(
            ServerListBean(
                BaseUrlConfig.BASEURL_TEST,
                isTestServer
            )
        )
        urlList.add(
            ServerListBean(
                BaseUrlConfig.BASEURL_PRE,
                isPreServer
            )
        )
        urlList.add(
            ServerListBean(
                BaseUrlConfig.BASEURL_RELEASE,
                isReleaseServer
            )
        )
        mBinding.recyclerView.linear().setup {
            addType<ServerListBean> { R.layout.app_layout_setting_url_item }
            onFastClick(R.id.root) {
                val model = getModel<ServerListBean>()
                urlList.forEach {
                    it.checked = false
                    it.notifyChange()
                }
                model.checked = true
                model.notifyChange()
            }

        }
        mBinding.recyclerView.addModels(urlList)

        mBinding.tvSubmit.safeClick {
            val checkedList =
                mBinding.recyclerView.getMutable<ServerListBean>().filter { it.checked }
            if (checkedList.isNullOrEmpty()) {
                customToast("请选择环境")
            } else {
                val tempUrl = checkedList[0].url
                MMKVProvider.baseUrl = tempUrl
                logE("MMKVProvider.baseUrl"+MMKVProvider.baseUrl)
                restartApp()
            }
        }
    }

    data class ServerListBean(var url: String, var checked: Boolean = false) : BaseObservable()

    private fun restartApp(){
        val intent = Intent(this, SplashActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent);
        Process.killProcess(Process.myPid());
        exitProcess(0)
    }
}