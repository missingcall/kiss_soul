package com.kissspace.android.ui.fragment

import android.content.Context
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.kissspace.android.R
import com.kissspace.android.databinding.AppFragmentSearchBinding
import com.kissspace.android.ui.activity.SearchActivity
import com.kissspace.android.viewmodel.SearchViewModel
import com.kissspace.common.base.BaseFragment
import com.kissspace.common.router.jump
import com.kissspace.common.model.search.SearchRecord
import com.kissspace.common.router.RouterPath.PATH_USER_PROFILE
import com.kissspace.util.notEmptyOrNull
import com.kissspace.util.logE

/**
 * @Author gaohangbo
 * @Date 2022/12/31 20:31.
 * @Describe //房间和用户id搜索
 */
class SearchUserFragment : BaseFragment(R.layout.app_fragment_search) {

    private val mBinding by viewBinding<AppFragmentSearchBinding>()

    private val mViewModel by viewModels<SearchViewModel>()

    private var position: Int? = null

    private var searchActivity: SearchActivity? = null

    companion object {
        fun newInstance(position: Int)=SearchUserFragment().apply {
            arguments = bundleOf("position" to position)
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        position = arguments?.getInt("position", 0)
        mBinding.pageRefreshLayout.apply {
            onRefresh {
                initData(isRefresh=true,isEmptyStr=false)
            }
            onLoadMore {
                initData(isRefresh=false,isEmptyStr=true)
            }
        }
        mBinding.rvList.linear().setup {
            addType<SearchRecord> { R.layout.app_item_search_user }
            onFastClick(R.id.cl_root) {
                val model = getModel<SearchRecord>(modelPosition)
                //点击的时候加入到当前到搜索记录
                model.nickName?.let {
                    searchActivity?.saveHistory()
                }
                jump(PATH_USER_PROFILE, "userId" to model.userId.orEmpty())
            }
        }.models = mutableListOf()
    }

    private fun initData(isRefresh: Boolean,isEmptyStr: Boolean) {
        if(isEmptyStr){
            searchContent(searchActivity?.getSearchContent()?.orEmpty(),isRefresh)
        }else{
            searchContent(searchActivity?.getSearchContent()?.notEmptyOrNull(),isRefresh)
        }

    }

    override fun onResume() {
        super.onResume()
        initData(isRefresh=true,isEmptyStr=false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        searchActivity = context as SearchActivity
    }

    fun searchContent(content:String?,isRefresh: Boolean) {
        logE("content$content")
        if (isRefresh) {
            mBinding.pageRefreshLayout.index = 1
        }
        content?.let {
            searchContent->
            mViewModel.searchContent(searchContent, mBinding.pageRefreshLayout.index) {
                if (isRefresh) {
                    mBinding.pageRefreshLayout.index = 1
                    mBinding.rvList.bindingAdapter.mutable.clear()
                    if (it.userResponsePage.userRecords?.size == 0) {
                        mBinding.pageRefreshLayout.showEmpty()
                    } else {
                        mBinding.pageRefreshLayout.addData(
                            it.userResponsePage.userRecords
                        )
                        mBinding.pageRefreshLayout.showContent()
                    }
                    mBinding.pageRefreshLayout.finishRefresh()
                } else {
                    mBinding.pageRefreshLayout.addData(
                        it.userResponsePage.userRecords
                    )
                    mBinding.pageRefreshLayout.finishLoadMore()
                }
                if(mBinding.rvList.bindingAdapter.models?.size== it.userResponsePage.total){
                    mBinding.pageRefreshLayout.finishLoadMoreWithNoMoreData()
                }else{
                    mBinding.pageRefreshLayout.setNoMoreData(false)
                }
            }
        }

    }

}
