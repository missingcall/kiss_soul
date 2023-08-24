package com.kissspace.mine.ui.fragment

import android.os.Bundle
import androidx.core.os.bundleOf
import by.kirich1409.viewbindingdelegate.viewBinding
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.linear
import com.drake.brv.utils.mutable
import com.drake.brv.utils.setup
import com.kissspace.common.base.BaseFragment
import com.kissspace.common.config.CommonApi
import com.kissspace.common.model.CollectListBean
import com.kissspace.common.model.MyCollectResponse
import com.kissspace.common.util.mmkv.MMKVProvider
import com.kissspace.common.util.jumpRoom
import com.kissspace.common.widget.CommonConfirmDialog
import com.kissspace.mine.http.MineApi
import com.kissspace.module_mine.R
import com.kissspace.module_mine.databinding.MineFragmentCollectBinding
import com.kissspace.network.net.Method
import com.kissspace.network.net.request

/**
 *
 * @Author: nicko
 * @CreateDate: 2023/1/6 15:40
 * @Description: 我的收藏fragment
 *
 */
class MyCollectFragment : BaseFragment(R.layout.mine_fragment_collect) {
    private val mBinding by viewBinding<MineFragmentCollectBinding>()
    private lateinit var type: String

    companion object {
        fun newInstance(type: String) = MyCollectFragment().apply {
            arguments = bundleOf("type" to type)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        type = arguments?.getString("type")!!
    }

    override fun initView(savedInstanceState: Bundle?) {
        initRecyclerView()
        mBinding.pageRefreshLayout.onRefresh {
            initData()
        }
        mBinding.pageRefreshLayout.onLoadMore {
            initData()
        }.autoRefresh()
    }

    private fun initRecyclerView() {
        mBinding.recyclerView.linear().setup {
            addType<CollectListBean> { R.layout.mine_layout_collect_item }
            onClick(R.id.tv_enter_room) {
                val model = getModel<CollectListBean>()
                jumpRoom(model.chatRoomId)
            }
            onLongClick(R.id.root) {
                val model = getModel<CollectListBean>()
                CommonConfirmDialog(requireContext(), "确定要取消收藏此房间吗") {
                    if (this) {
                        cancelCollect(modelPosition, model.chatRoomId)
                    }
                }.show()
            }
        }.models = mutableListOf()
    }

    private fun initData() {
        val param = mutableMapOf<String, Any?>()
        param["pageNum"] = mBinding.pageRefreshLayout.index
        param["pageSize"] = 20
        param["type"] = type
        param["userId"] = MMKVProvider.userId
        request<MyCollectResponse>(CommonApi.API_QUERY_MY_COLLECT, Method.GET, param, onSuccess = {
            mBinding.pageRefreshLayout.addData(it.records, hasMore = {
                mBinding.pageRefreshLayout.index * 20 < it.total
            }, isEmpty = {
                it.records.isEmpty()
            })
        })
    }

    private fun cancelCollect(position: Int, crId: String) {
        val param = mutableMapOf<String, Any?>()
        param["chatRoomId"] = crId
        param["type"] = "002"
        request<Int>(MineApi.API_CANCEL_COLLECT, Method.POST, param, onSuccess = {
            mBinding.recyclerView.mutable.removeAt(position)
            mBinding.recyclerView.bindingAdapter.notifyItemRemoved(position)
            if (mBinding.recyclerView.mutable.isEmpty()) {
                mBinding.pageRefreshLayout.showEmpty()
            }
        })
    }
}