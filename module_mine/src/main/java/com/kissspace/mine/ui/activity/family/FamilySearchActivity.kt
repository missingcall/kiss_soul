package com.kissspace.mine.ui.activity.family

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.ToastUtils
import com.didi.drouter.annotation.Router
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.kissspace.common.base.BaseActivity
import com.kissspace.common.binding.dataBinding
import com.kissspace.common.config.Constants
import com.kissspace.common.router.jump
import com.kissspace.common.ext.safeClick
import com.kissspace.common.ext.setTitleBarListener
import com.kissspace.common.model.family.FamilyListModels
import com.kissspace.common.router.RouterPath
import com.kissspace.common.util.getSearchData
import com.kissspace.common.util.mmkv.MMKVProvider
import com.kissspace.common.widget.CommonConfirmDialog
import com.kissspace.common.widget.CommonHintDialog
import com.kissspace.common.widget.Header
import com.kissspace.mine.viewmodel.FamilyViewModel
import com.kissspace.module_mine.R
import com.kissspace.module_mine.databinding.MineActivitySearchFamilyBinding
import com.kissspace.module_mine.databinding.MineItemFamilyBinding
import com.kissspace.network.result.collectData
import com.kissspace.util.isNotEmptyBlank
import com.kissspace.util.logE
import com.kissspace.util.toast

/**
 * @Author gaohangbo
 * @Date 2022/12/26 18:16.
 * @Describe 搜索家族页面
 */
@Router(path = RouterPath.PATH_SEARCH_FAMILY)
class FamilySearchActivity : com.kissspace.common.base.BaseActivity(R.layout.mine_activity_search_family) {
    private val mBinding by dataBinding<MineActivitySearchFamilyBinding>()
    private val mViewModel by viewModels<FamilyViewModel>()
    private val familyList = mutableListOf<FamilyListModels>()
    private val header: Header = Header()

    override fun initView(savedInstanceState: Bundle?) {
        setTitleBarListener(mBinding.titleBar)
        mBinding.rvFamily.linear().setup {
            addType<FamilyListModels>(R.layout.mine_item_family)
            addType<Header>(R.layout.mine_activity_family_header)
            onFastClick(R.id.cl_root) {
                val model = getModel<FamilyListModels>()
                jump(
                    RouterPath.PATH_FAMILY_DETAIL,
                    "isFamilyNotMember" to true,
                    "familyId" to model.familyId
                )
            }
            onBind {
                when (val viewBinding = getBinding<ViewDataBinding>()) {
                    is MineItemFamilyBinding -> {
                        val model = getModel<FamilyListModels>()
                        viewBinding.m = model
                        viewBinding.tvAdd.safeClick {
                            CommonConfirmDialog(
                                this@FamilySearchActivity,
                                "是否确认加入家族${model.familyName}",
                                ""
                            ) {
                                if (this) {
                                    mViewModel.applyFamilyUser(model.familyId)
                                }
                            }.show()
                        }
                        //001 家族成员；002 已申请加入；003 未申请
                        when (model.userFamilyStatus) {
                            Constants.FAMILY_APPLY -> {
                                viewBinding.tvAdd.isSelected = true
                                viewBinding.tvAdd.setImageResource(R.mipmap.mine_family_applied)
                                viewBinding.tvAdd.visibility = View.VISIBLE
                                viewBinding.tvAdd.isEnabled = false
                            }
                            Constants.FAMILY_MEMBER -> {
                                viewBinding.tvAdd.visibility = View.GONE
                            }
                            else -> {
                                viewBinding.tvAdd.setImageResource(R.mipmap.mine_icon_family_join)
                                viewBinding.tvAdd.visibility = View.VISIBLE
                                viewBinding.tvAdd.isSelected = false
                                viewBinding.tvAdd.isEnabled = true
                            }
                        }
                    }
                }
            }
        }.models = familyList.filterIndexed { index, _ -> index < 5 }

        getSearchData(mViewModel.viewModelScope,mBinding.commonSearchView.mBinding.etSearch){
            logE("value$it")
            searchContent(it.toString())
        }

        mBinding.commonSearchView.onEditorActionBlock={
            searchContent(it)
        }

        mBinding.ivQuestion.safeClick {
            CommonHintDialog.newInstance("如需创建家族\n请关注微信公众号", MMKVProvider.wechatPublicAccount).show(supportFragmentManager)
        }
        searchContent("")
    }

    private fun searchContent(searchContent: String) {
        searchContent.let {
            if (it.isNotEmptyBlank()) {
                mViewModel.getFamilyListByParameter(it)
                mBinding.rvFamily.bindingAdapter.removeHeader(header, animation = false)
            } else {
                mViewModel.getFamilyListByParameterByHot(it)
                if (mBinding.rvFamily.bindingAdapter.headers.isEmpty()) {
                    mBinding.rvFamily.bindingAdapter.addHeader(header, animation = false)
                }
            }
        }
    }


    override fun createDataObserver() {
        super.createDataObserver()
        collectData(mViewModel.getFamilyModelListEvent, onSuccess = {
            mBinding.pageLayout.showContent()
            familyList.clear()
            familyList.addAll(it)
            mBinding.rvFamily.bindingAdapter.models = familyList
        }, onEmpty = {
            mBinding.pageLayout.showEmpty()
        })
        collectData(mViewModel.applyUserEvent, onSuccess = {
            ToastUtils.showShort("申请成功")
            //刷新数据
            searchContent(mBinding.commonSearchView.mBinding.etSearch.text.toString())
        }, onError = {
            toast(it.message)
        })
    }
}