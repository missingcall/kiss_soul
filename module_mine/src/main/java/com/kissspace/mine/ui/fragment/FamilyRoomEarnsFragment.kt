package com.kissspace.mine.ui.fragment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.viewModels
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.kissspace.common.base.BaseFragment
import com.kissspace.common.binding.dataBinding
import com.kissspace.common.config.Constants
import com.kissspace.common.model.family.FamilyFlowModel
import com.kissspace.common.model.family.FamilyFlowRecord
import com.kissspace.common.model.family.FamilyListModel
import com.kissspace.mine.ui.activity.family.FamilyOutProfitActivity
import com.kissspace.mine.viewmodel.FamilyViewModel
import com.kissspace.module_mine.BuildConfig
import com.kissspace.module_mine.R
import com.kissspace.module_mine.databinding.MineFragmentWalletDetailBinding

/**
 * @Author gaohangbo
 * @Date 2022/12/29 08:48.
 * @Describe 家族收益页面
 */
class FamilyRoomEarnsFragment : BaseFragment(R.layout.mine_fragment_wallet_detail) {

    private val mBinding by dataBinding<MineFragmentWalletDetailBinding>()
    private val mViewModel by viewModels<FamilyViewModel>()
    private var familyId: String? = null
    private var type: String? = null
    private var isInRoom: Int = 0
    private var isToday: Int = 0
    private var familyOutProfitActivity: FamilyOutProfitActivity?=null
    companion object {
        fun newInstance(type: String,familyId: String?): FamilyRoomEarnsFragment {
            val fragment = FamilyRoomEarnsFragment()
            val args = Bundle()
            args.putString("type", type)
            args.putString("familyId", familyId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        familyId = arguments?.getString("familyId")
        type = arguments?.getString("type")
        mBinding.rvCoin.linear().setup {
            addType<FamilyFlowRecord>(R.layout.mine_list_item_profit)
        }.models = mutableListOf()

        //牌照房0 成员1 今日0 七日1
        when (type) {
            Constants.FamilyEarnsType.FamilyInToady.type -> {
                isInRoom = 0
                isToday = 0
            }
            Constants.FamilyEarnsType.FamilyInSevenToady.type -> {
                isInRoom = 0
                isToday = 1
            }
            Constants.FamilyEarnsType.FamilyOutToady.type -> {
                isInRoom = 1
                isToday = 0
            }
            Constants.FamilyEarnsType.FamilyOutSevenDay.type -> {
                isInRoom = 1
                isToday = 1
            }
            else -> {

            }
        }
        mBinding.pageRefreshLayout.apply {
            onRefresh {
                initData(familyOutProfitActivity?.getSearchContent().orEmpty(),true)
            }
            onLoadMore {
                initData(familyOutProfitActivity?.getSearchContent().orEmpty(),false)
            }
        }
        initData(familyOutProfitActivity?.getSearchContent().orEmpty(),true)
    }

    private fun initData(content:String,isRefresh: Boolean) {
        if (isRefresh) {
            mBinding.pageRefreshLayout.index = 1
        }
        mViewModel.queryFamilyFlowList(
            isInRoom = isInRoom,
            isToday = isToday,
            keyword = content,
            pageNum = mBinding.pageRefreshLayout.index
        ) {
            if (isRefresh) {
                mBinding.pageRefreshLayout.index = 1
                mBinding.rvCoin.bindingAdapter.mutable.clear()
                if (it==null|| it.familyFlowRecords.isEmpty()) {
                  mBinding.pageRefreshLayout.showEmpty()
                } else {
                    mBinding.pageRefreshLayout.addData(
                        it.familyFlowRecords
                    )
                    mBinding.pageRefreshLayout.showContent()
                }
                mBinding.pageRefreshLayout.finishRefresh()
            } else {
                mBinding.pageRefreshLayout.addData(
                    it?.familyFlowRecords
                )
                mBinding.pageRefreshLayout.finishLoadMore()
            }
            if (mBinding.rvCoin.bindingAdapter.mutable.size == it?.total) {
                mBinding.pageRefreshLayout.finishLoadMoreWithNoMoreData()
            }else{
                mBinding.pageRefreshLayout.setNoMoreData(false)
            }
        }
    }


    fun searchContent(content:String,isRefresh: Boolean){
        initData(content,isRefresh)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context is FamilyOutProfitActivity){
            familyOutProfitActivity = context
        }

    }
}