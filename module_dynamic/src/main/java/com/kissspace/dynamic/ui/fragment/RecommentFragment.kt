package com.kissspace.dynamic.ui.fragment

import android.os.Bundle
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.blankj.utilcode.util.LogUtils
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.kissspace.dynamic.Dynamic
import com.kissspace.dynamic.ui.viewmodel.DynamicViewModel
import com.kissspace.common.base.BaseFragment
import com.kissspace.module_dynamic.R
import com.kissspace.module_dynamic.databinding.DynamicFragmentRecommendBinding

/**
 * @Author gaohangbo
 * @Date 2023/7/31 16:42.
 * @Describe
 */
class RecommendFragment(private val data: String?) : BaseFragment(R.layout.dynamic_fragment_recommend) {

    private val mBinding by viewBinding<DynamicFragmentRecommendBinding>()

    private val mViewModel by viewModels<DynamicViewModel>()

    override fun initView(savedInstanceState: Bundle?) {
        LogUtils.e("data$data")

        mBinding.pageRefreshLayout.apply {
            onRefresh {
                initData(isRefresh=false)
            }
            onLoadMore {
                initData(isRefresh=true)
            }
        }

//        mViewModel.getRecommendDynamicList {
//
//        }
        mBinding.rvDynamic.linear().setup {
            addType<Dynamic>(R.layout.dynamic_fragment_recommend)
        }
    }

    private fun initData(isRefresh: Boolean) {
        if (isRefresh) {
            mBinding.pageRefreshLayout.index = 1
        }
        mViewModel.getRecommendDynamicList(mBinding.pageRefreshLayout.index){
            if (isRefresh) {
                mBinding.rvDynamic.bindingAdapter.mutable.clear()
                if (it.dynamicInfoRecords.isEmpty()) {
                    mBinding.pageRefreshLayout.showEmpty()
                } else {
                    mBinding.pageRefreshLayout.addData(it.dynamicInfoRecords)
                    mBinding.pageRefreshLayout.showContent()
                }
                mBinding.pageRefreshLayout.finishRefresh()
            } else {
                mBinding.pageRefreshLayout.addData(it.dynamicInfoRecords)
                mBinding.pageRefreshLayout.finishLoadMore()
            }
            if (mBinding.rvDynamic.bindingAdapter.models?.size == it.total) {
                mBinding.pageRefreshLayout.finishLoadMoreWithNoMoreData()
            }else{
                mBinding.pageRefreshLayout.setNoMoreData(false);
            }
        }
//            val param = mutableMapOf<String, Any?>()
//            param["pageNum"] = mBinding.pageRefreshLayout.index
//            param["pageSize"] = 10
//            request<SystemMessageResponse>(
//                MessageApi.API_SYSTEM_MESSAGE,
//                Method.GET,
//                param,
//                onSuccess = {
//                    MMKVProvider.systemMessageLastReadCount = it.total
//                    mBinding.pageRefreshLayout.addData(it.records.asReversed(), isEmpty = {
//                        it.total == 0
//                    }, hasMore = {
//                        it.size * 10 < it.total
//                    })
//                    if (isFirstLoad) {
//                        mBinding.recyclerView.scrollToBottom()
//                    }
//                })
    }
}