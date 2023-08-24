package com.kissspace.setting.ui.activity

import android.os.Bundle
import android.view.View
import by.kirich1409.viewbindingdelegate.viewBinding
import com.didi.drouter.annotation.Router
import com.kissspace.common.base.BaseActivity
import com.kissspace.common.config.Constants
import com.kissspace.common.router.jump
import com.kissspace.common.ext.safeClick
import com.kissspace.common.ext.setTitleBarListener
import com.kissspace.common.flowbus.Event
import com.kissspace.common.flowbus.FlowBus
import com.kissspace.common.http.checkUserPermission
import com.kissspace.common.model.wallet.WalletModel
import com.kissspace.common.router.RouterPath
import com.kissspace.common.util.*
import com.kissspace.common.util.mmkv.MMKVProvider
import com.kissspace.common.widget.CommonConfirmDialog
import com.kissspace.module_setting.R
import com.kissspace.module_setting.databinding.SettingActivitySettingBinding
import com.kissspace.network.net.Method
import com.kissspace.network.net.request
import com.kissspace.setting.http.SettingApi
import com.kissspace.util.clearAllCache
import com.kissspace.util.getTotalCacheSize

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/12/28 11:00
 * @Description: 设置页
 *
 */
@Router(uri = RouterPath.PATH_SETTING)
class SettingActivity : BaseActivity(R.layout.setting_activity_setting) {
    private val mBinding by viewBinding<SettingActivitySettingBinding>()

    override fun initView(savedInstanceState: Bundle?) {
        mBinding.cacheSize.text = getTotalCacheSize()
        initClickEvents()
        initUserPermission()
    }

    private fun initUserPermission() {
        checkUserPermission(Constants.UserPermission.PERMISSION_SAY_HI) {
            mBinding.layoutSayHi.visibility = if (it) View.VISIBLE else View.GONE
            mBinding.viewLineSayHi.visibility = if (it) View.GONE else View.VISIBLE
        }

        request<WalletModel>(
            SettingApi.API_MY_WALLET,
            Method.GET,
            mutableMapOf<String, Any?>(),
            onSuccess = {
                mBinding.layoutPayAccountBind.visibility =
                    if (it.identity == "001") View.GONE else View.VISIBLE
            })
    }


    private fun initClickEvents() {
        setTitleBarListener(mBinding.titleBar)
        //身份认证
        mBinding.layoutRoleVerify.safeClick {
            if (!MMKVProvider.authentication) {
                jump(RouterPath.PATH_USER_IDENTITY_AUTH)
            } else {
                jump(RouterPath.PATH_USER_IDENTITY_AUTH_SUCCESS)
            }
        }

        mBinding.layoutBindAlipay.safeClick {
            jump(RouterPath.PATH_BIND_ALIPAY)
        }

        mBinding.tvCertified.text = if (MMKVProvider.authentication) "已认证" else "未认证"


        //账号安全
        mBinding.layoutAccountSafe.safeClick {
            jump(RouterPath.PATH_ACCOUNT)
        }

        //绑定银行卡
        mBinding.layoutBindBankcard.safeClick {
            jump(RouterPath.PATH_BIND_BANKCARD)
        }


        //打招呼设置
        mBinding.layoutSayHi.safeClick {
            jump(RouterPath.PATH_SAY_HI_SETTING)
        }

        //打招呼设置
        mBinding.layoutBlackList.safeClick {
            jump(RouterPath.PATH_BLACK_LIST)
        }

        //帮助
        mBinding.layoutHelp.safeClick {
            jump(RouterPath.PATH_HELP)
        }
        //关于我们
        mBinding.layoutAboutUs.safeClick {
            jump(RouterPath.PATH_ABOUT_US)
        }
//        //青少年模式
//        mBinding.layoutTeenagerMode.safeClick {
//            jump(RouterPath.PATH_TEENAGER_DESCRIBE)
//        }

        mBinding.layoutCleanCache.setOnClickListener {
            CommonConfirmDialog(this, title = "是否清除缓存?") {
                if (this) {
                    clearAllCache()
                    customToast("清除成功")
                    mBinding.cacheSize.text = getTotalCacheSize()
                }
            }.show()
        }

        mBinding.tvLoginOut.setOnClickListener {
            CommonConfirmDialog(this, title = "确定要退出登录？") {
                if (this) loginOut()
            }.show()
        }
        mBinding.tvChangeAccount.safeClick {
            jump(RouterPath.PATH_CHANGE_ACCOUNT)
        }
    }

    override fun onResume() {
        super.onResume()
        mBinding.tvCertified.text = if (MMKVProvider.authentication) "已认证" else "未认证"

    }

    override fun createDataObserver() {
        super.createDataObserver()
        FlowBus.observerEvent<Event.RefreshChangeAccountEvent>(this) {
            initUserPermission()
        }
    }
}