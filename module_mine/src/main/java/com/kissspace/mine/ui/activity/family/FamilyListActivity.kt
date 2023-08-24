package com.kissspace.mine.ui.activity.family

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.databinding.ViewDataBinding
import com.blankj.utilcode.util.ToastUtils
import com.didi.drouter.annotation.Router
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.kissspace.util.activityresult.registerForStartActivityResult
import com.kissspace.util.toast
import com.kissspace.common.binding.dataBinding
import com.kissspace.common.config.Constants
import com.kissspace.common.ext.*
import com.kissspace.common.flowbus.Event
import com.kissspace.common.flowbus.FlowBus
import com.kissspace.common.model.family.FamilyListModels
import com.kissspace.common.router.RouterPath
import com.kissspace.common.router.jump
import com.kissspace.common.util.mmkv.MMKVProvider
import com.kissspace.common.widget.CommonConfirmDialog
import com.kissspace.common.widget.CommonHintDialog
import com.kissspace.mine.viewmodel.FamilyViewModel
import com.kissspace.module_mine.R
import com.kissspace.module_mine.databinding.MineActivityFamilyBinding
import com.kissspace.module_mine.databinding.MineItemFamilyBinding
import com.kissspace.network.result.collectData
import com.kissspace.util.activity
import com.kissspace.util.logE

/**
 * @Author gaohangbo
 * @Date 2022/12/26 16:00.
 * @Describe (未加入的用户）才能看到显示家族列表页面
 */
@Router(path = RouterPath.PATH_FAMILY_LIST)
class FamilyListActivity : com.kissspace.common.base.BaseActivity(R.layout.mine_activity_family) {

    private val mBinding by dataBinding<MineActivityFamilyBinding>()

    private val mViewModel by viewModels<FamilyViewModel>()

    private val familyList = mutableListOf<FamilyListModels>()

    override fun initView(savedInstanceState: Bundle?) {
        setTitleBarListener(mBinding.titleBar)
        initRefresh()
        mBinding.refreshLayout.apply {
            setEnableLoadMore(false)
            setOnRefreshListener {
                initRefresh()
            }
        }

        mBinding.rvFamily.linear().setup {
            addType<FamilyListModels>(R.layout.mine_item_family)
            onFastClick(R.id.cl_root) {
                val model = getModel<FamilyListModels>()
                jump(
                    RouterPath.PATH_FAMILY_DETAIL,
                    "isFamilyNotMember" to true,
                    "familyId" to model.familyId,
                    activity = activity,
                    resultLauncher = startActivityLauncher
                )
            }
            onBind {
                val model = getModel<FamilyListModels>()
                when (val viewBinding = getBinding<ViewDataBinding>()) {
                    is MineItemFamilyBinding -> {
                        //001 家族成员；002 已申请加入；003 未申请
                        when (model.userFamilyStatus) {
                            Constants.FAMILY_APPLY -> {
                                viewBinding.tvAdd.isSelected = true
                                viewBinding.tvAdd.visibility = View.VISIBLE
                                viewBinding.tvAdd.isEnabled = false
                                viewBinding.tvAdd.setImageResource(R.mipmap.mine_family_applied)
                            }

                            Constants.FAMILY_MEMBER -> {
                                viewBinding.tvAdd.visibility = View.GONE
                            }

                            else -> {
                                viewBinding.tvAdd.visibility = View.VISIBLE
                                viewBinding.tvAdd.isSelected = false
                                viewBinding.tvAdd.setImageResource(R.mipmap.mine_icon_family_join)
                                viewBinding.tvAdd.isEnabled = true
                            }
                        }
                    }
                }
            }
            onFastClick(R.id.tv_add) {
                val model = getModel<FamilyListModels>()
                CommonConfirmDialog(
                    this@FamilyListActivity,
                    "是否确认加入家族${model.familyName}",
                    ""
                ) {
                    if (this) {
                        mViewModel.applyFamilyUser(model.familyId)
                    }
                }.show()
            }

        }.models = familyList

        mBinding.ivQuestion.safeClick {
            CommonHintDialog.newInstance("如需创建家族\n请在公众号内联系客服", MMKVProvider.wechatPublicAccount)
                .show(supportFragmentManager)
        }

        mBinding.tvSearch.safeClick {
            jump(
                RouterPath.PATH_SEARCH_FAMILY
            )
        }


        mBinding.refreshLayout.setOnRefreshListener {
            mViewModel.getFamilyList()
        }

        FlowBus.observerEvent<Event.MsgFamilyPassEvent>(this){
              jump(RouterPath.PATH_FAMILY_DETAIL)
               finish()
        }


    }

    private fun initRefresh() {
        mViewModel.getFamilyList()
    }


//    override fun observerGlobalNotificationMessage(type: String, data: String?) {
//        if (type == CustomNotificationObserver.MESSAGE_FAMILY_PASS) {
//            jump(RouterPath.PATH_FAMILY_DETAIL)
//            finish()
//        }
//    }

    override fun createDataObserver() {
        super.createDataObserver()
        collectData(mViewModel.getFamilyModelListEvent, onSuccess = {
            mBinding.refreshLayout.showContent()
            familyList.clear()
            familyList.addAll(it)
            mBinding.rvFamily.bindingAdapter.models = familyList
            mBinding.refreshLayout.finishRefresh()
        }, onEmpty = {
            mBinding.refreshLayout.showEmpty()
        })
        collectData(mViewModel.applyUserEvent, onSuccess = {
            ToastUtils.showShort("申请成功")
            //刷新数据
            mViewModel.getFamilyList()
        }, onError = {
            toast(it.message)
        })
    }

    private val startActivityLauncher = registerForStartActivityResult { result ->
        logE("result.resultCode" + result.resultCode)
        if (result.resultCode == RESULT_OK) {
            mViewModel.getFamilyList()
        }
    }
}