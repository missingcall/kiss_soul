package com.kissspace.setting.ui.activity

import android.os.Bundle
import android.view.View
import by.kirich1409.viewbindingdelegate.viewBinding
import com.didi.drouter.annotation.Router
import com.drake.brv.utils.*
import com.kissspace.common.base.BaseActivity
import com.kissspace.common.config.CommonApi
import com.kissspace.common.ext.setTitleBarListener
import com.kissspace.common.model.BlackListBean
import com.kissspace.common.router.RouterPath
import com.kissspace.common.util.customToast
import com.kissspace.module_setting.R
import com.kissspace.module_setting.databinding.SettingActivityBlackListBinding
import com.kissspace.network.net.Method
import com.kissspace.network.net.request
import com.kissspace.setting.http.SettingApi

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/12/28 18:15
 * @Description: 黑名单页面
 *
 */
@Router(path = RouterPath.PATH_BLACK_LIST)
class BlackListActivity : com.kissspace.common.base.BaseActivity(R.layout.setting_activity_black_list) {
    private val mBinding by viewBinding<SettingActivityBlackListBinding>()
    override fun initView(savedInstanceState: Bundle?) {
        setTitleBarListener(mBinding.titleBar)
        mBinding.recyclerView.linear().setup {
            addType<BlackListBean> { R.layout.setting_layout_black_list_item }
            onClick(R.id.tv_cancel) {
                val model = getModel<BlackListBean>()
                cancelBan(modelPosition, model.userId)
            }
            onBind {
                findView<View>(R.id.view_line).visibility =
                    if (modelPosition == modelCount - 1) View.GONE else View.VISIBLE
            }
        }.models = mutableListOf()
        mBinding.pageRefreshLayout.apply {
            setDisableContentWhenRefresh(true)
            onRefresh { initDa() }
        }
        mBinding.pageRefreshLayout.onRefresh {
            initDa()
        }.autoRefresh()

    }

    private fun initDa() {
        request<List<BlackListBean>>(com.kissspace.setting.http.SettingApi.API_BLACK_LIST, Method.GET, onSuccess = {
            mBinding.pageRefreshLayout.addData(it, isEmpty = {
                it.isEmpty()
            }, hasMore = {
                false
            })
        }, onError = {
            customToast(it.message)
        })
    }

    private fun cancelBan(index: Int, userId: String) {
        val params = mutableMapOf<String, Any?>()
        params["userId"] = userId
        request<Int>(CommonApi.API_CANCNEL_BAN_USER, Method.POST, params, onSuccess = {
            mBinding.recyclerView.mutable.removeAt(index)
            mBinding.recyclerView.bindingAdapter.notifyItemRemoved(index)
            if (mBinding.recyclerView.mutable.isEmpty()) {
                mBinding.pageRefreshLayout.showEmpty()
            }
        })
    }

}