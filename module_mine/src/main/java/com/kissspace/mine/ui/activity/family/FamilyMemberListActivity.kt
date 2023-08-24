package com.kissspace.mine.ui.activity.family

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.databinding.ViewDataBinding
import com.didi.drouter.annotation.Router
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.hjq.bar.OnTitleBarListener
import com.hjq.bar.TitleBar
import com.kissspace.util.activity
import com.kissspace.common.base.BaseActivity
import com.kissspace.common.binding.dataBinding
import com.kissspace.common.ext.setTitleBarListener
import com.kissspace.common.router.jump
import com.kissspace.common.model.family.FamilyMemberRecord
import com.kissspace.common.router.RouterPath
import com.kissspace.common.router.RouterPath.PATH_USER_PROFILE
import com.kissspace.common.router.parseIntent
import com.kissspace.common.util.customToast
import com.kissspace.common.util.getMutable
import com.kissspace.common.util.hideLoading
import com.kissspace.common.util.showLoading
import com.kissspace.common.widget.CommonConfirmDialog
import com.kissspace.mine.http.MineApi
import com.kissspace.mine.viewmodel.FamilyViewModel
import com.kissspace.module_mine.R
import com.kissspace.module_mine.databinding.MineActivityFamilyUpdateListBinding
import com.kissspace.module_mine.databinding.MineListItemFamilyBinding
import com.kissspace.network.net.Method
import com.kissspace.network.net.request
import com.kissspace.network.result.collectData
import com.kissspace.util.logE
import com.kissspace.util.toast

/**
 * @Author gaohangbo
 * @Date 2022/12/28 12:18.
 * @Describe 家族成员列表页面
 */
@Router(path = RouterPath.PATH_FAMILY_MEMBER_LIST)
class FamilyMemberListActivity : BaseActivity(R.layout.mine_activity_family_update_list) {
    private val familyHeaderList = mutableListOf<FamilyMemberRecord>()
    private val familyMemberList = mutableListOf<FamilyMemberRecord>()
    private val mBinding by dataBinding<MineActivityFamilyUpdateListBinding>()
    private val mViewModel by viewModels<FamilyViewModel>()
    private val pageSize = 20

    //是否是家族长
    private var headOfFamily by parseIntent<Boolean>()
    private var isVisitor by parseIntent<Boolean>()
    private var familyId by parseIntent<String>()
    override fun initView(savedInstanceState: Bundle?) {
        headOfFamily = intent.getBooleanExtra("headOfFamily", false)
        isVisitor = intent.getBooleanExtra("isVisitor", false)
        familyId = intent.getStringExtra("familyId").orEmpty()
        initTitle()
        initRefresh()
        initRecyclerView()


//        mViewModel.getFamilyUserListById(familyId, pageSize, mBinding.pageRefreshLayout.index)


//        mBinding.rvFamilyHeader.isNestedScrollingEnabled = false
//        mBinding.rvFamilyHeader.linear().setup {
//            addType<FamilyMemberRecord> {
//                R.layout.mine_list_item_family_member
//            }
//        }.models = familyHeaderList
//
//        mBinding.rvFamilyMember.isNestedScrollingEnabled = false
//        mBinding.rvFamilyMember.linear().setup {
//            addType<FamilyMemberRecord> {
//                R.layout.mine_list_item_family_member
//            }
//
//            onFastClick(R.id.tv_setting_manager) {
//                showLoading()
//                settingManager(getModel())
//            }
//            onFastClick(R.id.tv_pass) {
//                CommonConfirmDialog(activity, "确定要将该成员移出家族吗？") {
//                    if (this) {
//                        val model = getModel<FamilyMemberRecord>()
//                        model.userId?.let { it1 -> mViewModel.moveOutFamily(it1) }
//                    }
//                }.show()
//            }
//            onFastClick(R.id.iv_family) {
//                if (!isVisitor) {
//                    val model = getModel<FamilyMemberRecord>()
//                    jump(PATH_USER_PROFILE, "userId" to model.userId.orEmpty())
//                }
//
//            }
//        }.models = familyMemberList
    }

    private fun initRefresh() {
        mBinding.pageRefreshLayout.apply {
            onRefresh {
                mViewModel.getFamilyUserListById(
                    familyId, pageSize, mBinding.pageRefreshLayout.index
                )
            }
            onLoadMore {
                mViewModel.getFamilyUserListById(
                    familyId, pageSize, mBinding.pageRefreshLayout.index
                )
            }
        }.autoRefresh()
    }

    private fun initRecyclerView() {
        mBinding.recyclerView.linear().setup {
            addType<FamilyMemberRecord> {
                R.layout.mine_list_item_family_member
            }
            onFastClick(R.id.tv_setting_manager) {
                showLoading()
                settingManager(getModel())
            }
            onFastClick(R.id.tv_pass) {
                CommonConfirmDialog(activity, "确定要将该成员移出家族吗？") {
                    if (this) {
                        val model = getModel<FamilyMemberRecord>()
                        model.userId?.let { it1 -> mViewModel.moveOutFamily(it1) }
                    }
                }.show()
            }
            onFastClick(R.id.iv_family) {
                if (!isVisitor) {
                    val model = getModel<FamilyMemberRecord>()
                    jump(PATH_USER_PROFILE, "userId" to model.userId.orEmpty())
                }

            }
        }.mutable = mutableListOf()
    }

    private fun initTitle() {
        if (headOfFamily) {
            mBinding.titleBar.setRightTitle("管理")
        }
        mBinding.titleBar.setOnTitleBarListener(object : OnTitleBarListener {
            override fun onLeftClick(titleBar: TitleBar?) {
                super.onLeftClick(titleBar)
                finish()
            }

            override fun onRightClick(titleBar: TitleBar?) {
                super.onRightClick(titleBar)
                if (mBinding.titleBar.rightTitle == "管理") {
                    mBinding.titleBar.setRightTitle("取消")
                    mBinding.recyclerView.getMutable<FamilyMemberRecord>().forEach {
                        if (it.isFamilyHeader == false) {
                            it.isShowSettingManager = true
                            it.isShowMoveOut = false
                            it.notifyChange()
                        }

                    }
                } else if (mBinding.titleBar.rightTitle == "取消") {
                    mBinding.titleBar.setRightTitle("管理")
                    mBinding.recyclerView.getMutable<FamilyMemberRecord>().forEach {
                        if (it.isFamilyHeader == false) {
                            it.isShowSettingManager = false
                            it.isShowMoveOut = headOfFamily && it.userRole != "004"
                            it.notifyChange()
                        }
                    }
                }
            }
        })

        mBinding.commonSearchView.onEditorActionBlock = {
            if (it.isNullOrEmpty()) {
                mBinding.recyclerView.bindingAdapter.mutable.clear()
                mBinding.recyclerView.bindingAdapter.notifyDataSetChanged()
                mViewModel.getFamilyUserListById(familyId, pageSize, 1)
            } else {
                mBinding.recyclerView.bindingAdapter.mutable.clear()
                mBinding.recyclerView.bindingAdapter.notifyDataSetChanged()
                mViewModel.getFamilyUserListById(familyId, keyword = it)
            }

        }
    }


    private fun settingManager(record: FamilyMemberRecord) {
        val param = mutableMapOf<String, Any?>()
        param["userId"] = record.userId
        request<Boolean>(MineApi.API_FAMILY_SETTING_MANAGER, Method.POST, param, onSuccess = {
            hideLoading()
            logE("原本的角色是---${record.userRole}")
            if (record.userRole == "001") {
                record.userRole = "003"
                customToast("设置成功")
            } else if (record.userRole == "003") {
                record.userRole = "001"
                customToast("取消成功")
            }
            record.notifyChange()
        }, onError = {
            hideLoading()
            toast(it.errorMsg)
        })
    }

    override fun createDataObserver() {
        super.createDataObserver()
        collectData(mViewModel.getFamilyMemberListModelEvent, onSuccess = {
            it.familyMemberRecords.forEach { that ->
                that.isShowMoveOut = headOfFamily && that.userRole != "004"
            }
            mBinding.pageRefreshLayout.addData(it.familyMemberRecords, hasMore = {
                it.current < it.pages
            })


        })
        //移出家族成员
        collectData(mViewModel.moveOutFamilyEvent, onSuccess = {
            toast("移出成功")
            //刷新列表
            //根据家族id查询所有家族成员信息
            mBinding.pageRefreshLayout.refresh()
            setResult(
                RESULT_OK, intent.putExtra(
                    "result", FamilyMemberListResult
                )
            )
        }, onError = {
            toast(it.message)
        })
    }


    companion object {
        const val FamilyMemberListResult = "FamilyMemberListResult"
    }
}