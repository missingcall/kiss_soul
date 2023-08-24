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
import com.kissspace.common.router.jump
import com.kissspace.common.model.*
import com.kissspace.common.router.RouterPath
import com.kissspace.common.util.mmkv.MMKVProvider
import com.kissspace.mine.http.MineApi
import com.kissspace.module_mine.R
import com.kissspace.module_mine.databinding.MineActivityVisitorBinding
import com.kissspace.network.net.Method
import com.kissspace.network.net.request

/**
 *
 * @Author: nicko
 * @CreateDate: 2023/1/6 17:03
 * @Description: 我的访客列表
 *
 */
@Router(path = RouterPath.PATH_MY_VISITOR)
class MyVisitorActivity : com.kissspace.common.base.BaseActivity(R.layout.mine_activity_visitor) {
    private val mBinding by viewBinding<MineActivityVisitorBinding>()

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
            addType<VisitorListBean> { R.layout.mine_layout_visitor_item }
            onClick(R.id.iv_avatar) {
                val model = getModel<VisitorListBean>()
                jump(RouterPath.PATH_USER_PROFILE, "userId" to model.userId)
            }
            onClick(R.id.iv_chat) {
                val model = getModel<VisitorListBean>()
                jump(RouterPath.PATH_CHAT, "account" to model.accid, "userId" to model.userId)
            }
        }.models = mutableListOf()
    }


    private fun initData(isRefresh: Boolean) {
        val param = mutableMapOf<String, Any?>()
        param["pageNum"] = mBinding.pageRefreshLayout.index
        param["pageSize"] = 20
        request<MyVisitorResponse>(MineApi.API_QUERY_MY_VISITOR, Method.GET, param, onSuccess = {
            if (isRefresh) {
                mBinding.recyclerView.bindingAdapter.mutable.clear()
            }
            MMKVProvider.currentVisitorCount = it.total
            mBinding.pageRefreshLayout.addData(it.records, hasMore = {
                mBinding.pageRefreshLayout.index * 20 < it.total
            }, isEmpty = {
                it.records.isEmpty()
            })
        })
    }

}