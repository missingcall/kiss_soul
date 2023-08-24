package com.kissspace.mine.ui.activity

import android.os.Bundle
import by.kirich1409.viewbindingdelegate.viewBinding
import com.didi.drouter.annotation.Router
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.hjq.bar.OnTitleBarListener
import com.hjq.bar.TitleBar
import com.kissspace.common.base.BaseActivity
import com.kissspace.common.config.CommonApi
import com.kissspace.common.router.jump
import com.kissspace.common.model.*
import com.kissspace.common.router.RouterPath
import com.kissspace.common.util.mmkv.MMKVProvider
import com.kissspace.common.util.customToast
import com.kissspace.mine.http.MineApi
import com.kissspace.module_mine.R
import com.kissspace.module_mine.databinding.MineActivityFansBinding
import com.kissspace.network.net.Method
import com.kissspace.network.net.request

/**
 *
 * @Author: nicko
 * @CreateDate: 2023/1/6 17:03
 * @Description: 我的粉丝列表
 *
 */
@Router(path = RouterPath.PATH_MY_FANS)
class MyFansActivity : com.kissspace.common.base.BaseActivity(R.layout.mine_activity_fans) {
    private val mBinding by viewBinding<MineActivityFansBinding>()

    override fun initView(savedInstanceState: Bundle?) {


        mBinding.titleBar.setOnTitleBarListener(object : OnTitleBarListener {
            override fun onLeftClick(titleBar: TitleBar?) {
                finish()
            }
        })
        mBinding.pageRefreshLayout.apply {
            onRefresh {
                initData(true)
            }
            onLoadMore {
                initData(false)
            }
        }
        initRecyclerView()
        initData(true)
    }

    private fun initRecyclerView() {
        mBinding.recyclerView.linear().setup {
            addType<FansListBean> { R.layout.mine_layout_fans_item }
            onClick(R.id.iv_avatar) {
                val model = getModel<FansListBean>()
                jump(RouterPath.PATH_USER_PROFILE, "userId" to model.userId)
            }
            onFastClick(R.id.tv_follow) {
                val model = getModel<FansListBean>()
                if (model.followState) {
                    cancelFollow(model)
                } else {
                    follow(model)
                }

            }
        }.models = mutableListOf()
    }


    private fun initData(isRefresh: Boolean) {
        val param = mutableMapOf<String, Any?>()
        param["pageNum"] = mBinding.pageRefreshLayout.index
        param["pageSize"] = 20
        request<MyFansResponse>(MineApi.API_QUERY_MY_FANS, Method.GET, param, onSuccess = {
            if (isRefresh) {
                mBinding.recyclerView.bindingAdapter.mutable.clear()
            }
            MMKVProvider.currentFansCount = it.total
            mBinding.pageRefreshLayout.addData(it.records, hasMore = {
                mBinding.pageRefreshLayout.index * 20 < it.total
            }, isEmpty = {
                it.records.isEmpty()
            })
        })
    }


    private fun cancelFollow(model: FansListBean) {
        val param = mutableMapOf<String, Any?>("userId" to model.userId)
        request<Int>(CommonApi.API_CANCEL_FOLLOW_USER, Method.GET, param, onSuccess = {
            model.followState = false
            model.notifyChange()
        })
    }

    private fun follow(model: FansListBean) {
        val param = mutableMapOf<String, Any?>("userId" to model.userId)
        request<Int>(CommonApi.API_FOLLOW_USER, Method.POST, param, onSuccess = {
            model.followState = true
            model.notifyChange()
            customToast("已关注", true)
        })
    }

}