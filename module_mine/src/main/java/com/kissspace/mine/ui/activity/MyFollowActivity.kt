package com.kissspace.mine.ui.activity

import android.os.Bundle
import by.kirich1409.viewbindingdelegate.viewBinding
import com.didi.drouter.annotation.Router
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.linear
import com.drake.brv.utils.mutable
import com.drake.brv.utils.setup
import com.hjq.bar.OnTitleBarListener
import com.hjq.bar.TitleBar
import com.kissspace.common.base.BaseActivity
import com.kissspace.common.config.CommonApi
import com.kissspace.common.router.jump
import com.kissspace.common.model.FollowListBean
import com.kissspace.common.model.MyFollowResponse
import com.kissspace.common.router.RouterPath
import com.kissspace.common.widget.CommonConfirmDialog
import com.kissspace.mine.http.MineApi
import com.kissspace.module_mine.R
import com.kissspace.module_mine.databinding.MineActivityFollowBinding
import com.kissspace.network.net.Method
import com.kissspace.network.net.request

/**
 *
 * @Author: nicko
 * @CreateDate: 2023/1/6 17:03
 * @Description: 我的关注列表
 *
 */
@Router(path = RouterPath.PATH_MY_FOLLOW)
class MyFollowActivity : com.kissspace.common.base.BaseActivity(R.layout.mine_activity_follow) {
    private val mBinding by viewBinding<MineActivityFollowBinding>()

    override fun initView(savedInstanceState: Bundle?) {


        mBinding.titleBar.setOnTitleBarListener(object : OnTitleBarListener {
            override fun onLeftClick(titleBar: TitleBar?) {
                finish()
            }
        })
        mBinding.pageRefreshLayout.apply {
            onRefresh {
                initData()
            }
            onLoadMore {
                initData()
            }
        }.autoRefresh()
        initRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        mBinding.pageRefreshLayout.refresh()
    }

    private fun initRecyclerView() {
        mBinding.recyclerView.linear().setup {
            addType<FollowListBean> { R.layout.mine_layout_follow_item }
            onClick(R.id.iv_avatar) {
                val model = getModel<FollowListBean>()
                jump(RouterPath.PATH_USER_PROFILE, "userId" to model.userId)
            }
            onClick(R.id.tv_cancel) {
                val model = getModel<FollowListBean>()
                CommonConfirmDialog(this@MyFollowActivity, "确定要取消关注吗") {
                    if (this) {
                        cancelFollow(modelPosition, model.userId)
                    }

                }.show()
            }
        }.models = mutableListOf()
    }


    private fun initData() {
        val param = mutableMapOf<String, Any?>()
        param["pageNum"] = mBinding.pageRefreshLayout.index
        param["pageSize"] = 20
        request<MyFollowResponse>(MineApi.API_QUERY_MY_FOLLOW, Method.GET, param, onSuccess = {
            mBinding.pageRefreshLayout.addData(it.records, hasMore = {
                mBinding.pageRefreshLayout.index * 20 < it.total
            }, isEmpty = {
                it.records.isEmpty()
            })
        })
    }




    private fun cancelFollow(position: Int, userId: String) {
        val param = mutableMapOf<String, Any?>("userId" to userId)
        request<Int>(CommonApi.API_CANCEL_FOLLOW_USER, Method.GET, param, onSuccess = {
            mBinding.recyclerView.mutable.removeAt(position)
            mBinding.recyclerView.bindingAdapter.notifyItemRemoved(position)
            if (mBinding.recyclerView.mutable.isEmpty()) {
                mBinding.pageRefreshLayout.showEmpty()
            }
        })
    }

}