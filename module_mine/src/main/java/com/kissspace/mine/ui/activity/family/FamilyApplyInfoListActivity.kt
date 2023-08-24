package com.kissspace.mine.ui.activity.family

import android.os.Bundle

import androidx.activity.viewModels
import androidx.databinding.ViewDataBinding
import by.kirich1409.viewbindingdelegate.viewBinding
import com.didi.drouter.annotation.Router
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.kissspace.common.base.BaseActivity
import com.kissspace.common.router.jump
import com.kissspace.common.ext.safeClick
import com.kissspace.common.ext.setTitleBarListener
import com.kissspace.common.model.family.FamilyMemberModel
import com.kissspace.common.model.family.FamilyMemberRecord
import com.kissspace.common.router.RouterPath
import com.kissspace.mine.viewmodel.FamilyViewModel
import com.kissspace.module_mine.R
import com.kissspace.module_mine.databinding.MineActivityFamilyApplyInfoListBinding
import com.kissspace.module_mine.databinding.MineListItemFamilyBinding
import com.kissspace.network.result.collectData

/**
 * @Author gaohangbo
 * @Date 2023/1/3 21:26.
 * @Describe 家族申请列表
 */

@Router(uri = RouterPath.PATH_FAMILY_APPLY_LIST)
class FamilyApplyInfoListActivity : BaseActivity(R.layout.mine_activity_family_apply_info_list) {
    private val mBinding by viewBinding<MineActivityFamilyApplyInfoListBinding>()
    private val mViewModel by viewModels<FamilyViewModel>()

    override fun initView(savedInstanceState: Bundle?) {
        setTitleBarListener(mBinding.titleBar)
        mBinding.rvFamily.linear().setup {
            addType<FamilyMemberRecord> {
                R.layout.mine_list_item_family
            }
            onBind {
                val model = getModel<FamilyMemberRecord>()
                when (val viewBinding = getBinding<ViewDataBinding>()) {
                    //model.familyStatus
                    is MineListItemFamilyBinding -> {
                        viewBinding.tvPass.safeClick {
                            //同意
                            mViewModel.checkUserApply(model.userId, "001")
                        }
                        viewBinding.ivClose.safeClick {
                            //拒绝
                            mViewModel.checkUserApply(model.userId, "002")
                        }
                    }
                }
            }
            onFastClick(R.id.iv_family) {
                val model = getModel<FamilyMemberRecord>()
                jump(RouterPath.PATH_USER_PROFILE, "userId" to model.userId.orEmpty())
            }
        }.models = mutableListOf()

        initData(true)

        mBinding.pageRefreshLayout.setOnRefreshListener {
            initData(true)
        }

    }

    private fun initData(isRefresh: Boolean) {
        if(isRefresh){
            mBinding.pageRefreshLayout.index = 1
        }
        mViewModel.getFamilyUserApplyList {
            setData(isRefresh, it)
        }
    }

    private fun setData(
        isRefresh: Boolean,
        it: FamilyMemberModel?
    ) {
        if (isRefresh) {
            mBinding.rvFamily.bindingAdapter.mutable.clear()
            if (it?.familyMemberRecords?.size == 0) {
                mBinding.pageRefreshLayout.showEmpty()
            } else {
                mBinding.pageRefreshLayout.addData(
                    it?.familyMemberRecords
                )
                mBinding.pageRefreshLayout.showContent()
            }
            mBinding.pageRefreshLayout.finishRefresh()
        } else {
            mBinding.pageRefreshLayout.addData(
                it?.familyMemberRecords
            )
            mBinding.pageRefreshLayout.finishLoadMore()
        }
        if (mBinding.rvFamily.bindingAdapter.models?.size == it?.total) {
            mBinding.pageRefreshLayout.finishLoadMoreWithNoMoreData()
        }else{
            mBinding.pageRefreshLayout.setNoMoreData(false)
        }
    }

    override fun createDataObserver() {
        super.createDataObserver()
        collectData(mViewModel.checkoutUserEvent, onSuccess = {
            initData(true)
            setResult(RESULT_OK, intent.putExtra("result", FamilyApplyInfoListResult))
        })
    }

    companion object {
        const val FamilyApplyInfoListResult = "FamilyApplyInfoListResult"
    }

}