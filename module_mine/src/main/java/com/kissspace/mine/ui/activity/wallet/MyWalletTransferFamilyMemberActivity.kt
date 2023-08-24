package com.kissspace.mine.ui.activity.wallet

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.SimpleItemAnimator
import com.didi.drouter.annotation.Router
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.kissspace.common.base.BaseActivity
import com.kissspace.common.binding.dataBinding
import com.kissspace.common.config.Constants
import com.kissspace.common.ext.safeClick
import com.kissspace.common.ext.setTitleBarListener
import com.kissspace.common.model.family.FamilyMemberRecord
import com.kissspace.common.router.RouterPath
import com.kissspace.common.router.jump
import com.kissspace.common.util.getSearchData
import com.kissspace.mine.viewmodel.TransferDiamondViewModel
import com.kissspace.module_mine.R
import com.kissspace.module_mine.databinding.MineActivityWalletFamilyMemberUpdateListBinding
import com.kissspace.module_mine.databinding.MineWalletItemTransferUpdateBinding
import com.kissspace.util.logE
import com.kissspace.util.toast
import com.kissspace.util.trimString
import okhttp3.internal.filterList

/**
 * @Author gaohangbo
 * @Date 2023/4/7 17:33.
 * @Describe 转账家族页面
 */
@Router(path = RouterPath.PATH_USER_WALLET_UPDATE_FAMILY_MEMBER_LIST)
class MyWalletTransferFamilyMemberActivity : BaseActivity(R.layout.mine_activity_wallet_family_member_update_list) {
    private var familyMemberList = mutableListOf<FamilyMemberRecord>()
    //记录当前保存的列表
    private var selectedFamilyMemberList = mutableListOf<FamilyMemberRecord>()
    private val mBinding by dataBinding<MineActivityWalletFamilyMemberUpdateListBinding>()
    private val mViewModel by viewModels<TransferDiamondViewModel>()

    override fun initView(savedInstanceState: Bundle?) {
        mBinding.m = mViewModel
        mBinding.lifecycleOwner = this
        mBinding.titleBar.setTitleBarListener(
            onLeftClick = {
                finish()
            },
            onRightClick = {
                jump(
                    RouterPath.PATH_USER_WALLET_DIAMOND_TRANSFER_DETAIL
                )
            },
        )
        mBinding.pageRefreshLayout.apply {
            onLoadMore {
                logE("onLoadMore")
                searchContent(mBinding.commonSearchView.mBinding.etSearch.text.trimString(), false)
            }
        }
        getSearchData(mViewModel.viewModelScope, mBinding.commonSearchView.mBinding.etSearch) {
            logE("value$it")
            searchContent(it.toString(), true)
        }

        mBinding.commonSearchView.onEditorActionBlock = {
            searchContent(it, true)
        }

        mBinding.rvFamilyMember.linear().setup {
            addType<FamilyMemberRecord> { R.layout.mine_wallet_item_transfer_update }
            onBind {
                when (val viewBinding = getBinding<ViewDataBinding>()) {
                    is MineWalletItemTransferUpdateBinding -> {
                        val model = getModel<FamilyMemberRecord>()
                        if (modelPosition == mBinding.rvFamilyMember.bindingAdapter.models?.size) {
                            viewBinding.viewLine.visibility = View.GONE
                        } else {
                            viewBinding.viewLine.visibility = View.VISIBLE
                        }
                    }
                }
            }
            onFastClick(R.id.iv_choose) {
                //选中了
                val model = getModel<FamilyMemberRecord>()
                model.isSelected = !model.isSelected
                saveSelectItem(model)
                changeAllSelectedStatus()
            }
            onClick(R.id.iv_family) {
                val model = getModel<FamilyMemberRecord>()
                jump(RouterPath.PATH_USER_PROFILE, "userId" to model.userId.orEmpty())
            }

        }.models = mutableListOf()

        mBinding.cbSelectedAll.setOnCheckedChangeListener { compoundButton, isChecked ->
            if (!compoundButton.isPressed) {
                return@setOnCheckedChangeListener
            }
            if (isChecked) {
                mViewModel.isSelectedAll.value = true
                familyMemberList.forEach {
                    it.isSelected = true
                    saveSelectItem(it)
                }
            } else {
                mViewModel.isSelectedAll.value = false
                familyMemberList.forEach {
                    it.isSelected = false
                    saveSelectItem(it)
                }
            }
        }

        mBinding.tvTransfer.safeClick {
            when (selectedFamilyMemberList.size) {
                0 -> {
                    toast("请选择转账用户")
                }
                1 -> {
                    jump(
                        RouterPath.PATH_USER_WALLET_TRANSFER,
                        "transferUserId" to selectedFamilyMemberList[0].displayId.orEmpty(),
                        "walletType" to Constants.WalletType.DIAMOND.type
                    )
                }
                else -> {
                    jump(
                        RouterPath.PATH_USER_WALLET_TRANSFER,
                        "walletType" to Constants.WalletType.DIAMOND.type,
                        "transferFamilyMemberList" to selectedFamilyMemberList as ArrayList<FamilyMemberRecord>
                    )
                }
            }

        }
        searchContent("", true)
    }

    private fun changeAllSelectedStatus() {
        mViewModel.isSelectedAll.value =
            familyMemberList.filterList { this.isSelected }.size == familyMemberList.size
    }

    private fun searchContent(searchContent: String, isRefresh: Boolean) {
        if (isRefresh) {
            logE("isRefresh")
            mBinding.pageRefreshLayout.index = 1
        }else{
            //分页数据添加
            mBinding.pageRefreshLayout.index += 1
        }
        logE("mBinding.pageRefreshLayout.index"+mBinding.pageRefreshLayout.index)
        mViewModel.getFamilyUserListByPage(searchContent, mBinding.pageRefreshLayout.index) {
            val filterList= it?.familyMemberRecords?.filterList { this.isFamilyHeader != true }
            if (isRefresh) {
                familyMemberList.clear()
                if (filterList?.isEmpty() == true) {
                    mBinding.pageRefreshLayout.showEmpty()
                } else {
                    filterList?.let { it1 -> familyMemberList.addAll(it1) }
                    mBinding.pageRefreshLayout.showContent()
                }
                mBinding.pageRefreshLayout.finishRefresh()
            } else {
                filterList?.let { it1 -> familyMemberList.addAll(it1) }
                mBinding.pageRefreshLayout.finishLoadMore()
            }
            familyMemberList.forEach { familyMemberRecode ->
                selectedFamilyMemberList.forEach { it ->
                    if (familyMemberRecode.userId == it.userId) {
                        if(it.isSelected){
                            familyMemberRecode.isSelected=true
                        }
                    }
                }
            }
            mBinding.rvFamilyMember.bindingAdapter.models=familyMemberList
            if (mBinding.rvFamilyMember.bindingAdapter.models?.size == it?.total?.minus(1)) {
                mBinding.pageRefreshLayout.finishLoadMoreWithNoMoreData()
            } else {
                mBinding.pageRefreshLayout.setNoMoreData(false);
            }
        }
    }

    private fun saveSelectItem(item:FamilyMemberRecord){
        item.notifyChange()
        if(item.isSelected){
            if(!selectedFamilyMemberList.contains(item)){
                selectedFamilyMemberList.add(item)
            }
        }else{
            selectedFamilyMemberList.remove(item)
        }
    }

}