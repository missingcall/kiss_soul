package com.kissspace.mine.ui.activity.family

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.viewModels
import com.blankj.utilcode.util.ToastUtils
import com.didi.drouter.annotation.Router
import com.drake.brv.annotaion.DividerOrientation
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.divider
import com.drake.brv.utils.grid
import com.drake.brv.utils.setup
import com.kissspace.common.base.BaseActivity
import com.kissspace.common.binding.dataBinding
import com.kissspace.common.config.Constants
import com.kissspace.common.ext.*
import com.kissspace.common.flowbus.Event
import com.kissspace.common.flowbus.FlowBus
import com.kissspace.common.model.family.FamilyDetailInfoModel
import com.kissspace.common.model.family.FamilyMemberRecord
import com.kissspace.common.router.RouterPath
import com.kissspace.common.router.jump
import com.kissspace.common.router.parseIntent
import com.kissspace.common.util.*
import com.kissspace.common.util.format.DateFormat
import com.kissspace.common.util.mmkv.MMKVProvider
import com.kissspace.common.widget.CommonConfirmDialog
import com.kissspace.common.widget.CommonHintDialog
import com.kissspace.mine.ui.activity.family.FamilyApplyInfoListActivity.Companion.FamilyApplyInfoListResult
import com.kissspace.mine.ui.activity.family.FamilyMemberListActivity.Companion.FamilyMemberListResult
import com.kissspace.mine.ui.activity.family.FamilyModifyActivity.Companion.FamilyModifyResult
import com.kissspace.mine.viewmodel.FamilyViewModel
import com.kissspace.mine.widget.FamilyNoticeDialog
import com.kissspace.module_mine.R
import com.kissspace.module_mine.databinding.MineActivityFamilyDetailBinding
import com.kissspace.network.result.collectData
import com.kissspace.util.activity
import com.kissspace.util.isNotEmptyBlank
import com.kissspace.util.loadImage
import com.kissspace.util.logE
import com.kissspace.util.orFalse
import com.kissspace.util.orZero


/**
 * @Author gaohangbo
 * @Date 2022/12/28 09:37.
 * @Describe
 */
@Router(path = RouterPath.PATH_FAMILY_DETAIL)
class FamilyDetailActivity : BaseActivity(R.layout.mine_activity_family_detail) {
    private val mBinding by dataBinding<MineActivityFamilyDetailBinding>()
    private val familyList = mutableListOf<FamilyMemberRecord>()
    private val mViewModel by viewModels<FamilyViewModel>()
    private var familyId by parseIntent<String>()
    private var chatRoomId: String? = null

    //是否是家族成员
    private var isFamilyNotMember = false

    private var activityResult: ActivityResultLauncher<Intent>? = null
    override fun initView(savedInstanceState: Bundle?) {
        mBinding.m = mViewModel
        setTitleBarListener(mBinding.titleBar)
        isFamilyNotMember = intent.getBooleanExtra("isFamilyNotMember", false)
        mBinding.etDesc.setEnable(false)

        activityResult =
            registerForActivityResult(object : ActivityResultContract<Intent, String?>() {
                override fun createIntent(context: Context, input: Intent): Intent {
                    return input
                }

                override fun parseResult(resultCode: Int, intent: Intent?): String? {
                    return intent?.getStringExtra("result")
                }
            }) {
                if (it.equals(FamilyApplyInfoListResult)) {
                    //根据家族id查询所有家族成员信息
                    familyId?.let { it1 -> mViewModel.getFamilyUserListById(it1, 10, 1) }
                    mViewModel.getSelectFamilyInfo { info ->
                        //查询家族信息 更新右上角的人物数目
                        updateFamily(info)
                    }
                } else if (it.equals(FamilyMemberListResult)) {
                    //查询家族信息 更新右上角的人物数目
                    familyId?.let { it1 -> mViewModel.getFamilyUserListById(it1, 10, 1) }
                    mViewModel.getSelectFamilyInfo { info ->
                        //查询家族信息
                        updateFamily(info)
                    }
                } else if (it.equals(FamilyModifyResult)) {
                    mViewModel.getSelectFamilyInfo { info ->
                        //查询家族信息
                        updateFamily(info)
                    }
                }
            }
        if (isFamilyNotMember) {
            mViewModel.getFamilyById(familyId) {
                updateFamily(it)
            }
        } else {
            //查询家族信息
            mViewModel.getSelectFamilyInfo {
                updateFamily(it)
            }
        }
        mViewModel.getFamilyUserListById(familyId, 5, 1)
        mBinding.ivRight.safeClick {
            jump(
                RouterPath.PATH_FAMILY_MEMBER_LIST,
                "headOfFamily" to mViewModel.isFamilyHeader.get().orFalse(),
                "familyId" to familyId.orEmpty(),
                "isVisitor" to mViewModel.isVisitor.get().orFalse(),
                activity = activity,
                resultLauncher = activityResult
            )
        }
        mBinding.ivApplyList.safeClick {
            //家族申请列表
            jump(
                RouterPath.PATH_FAMILY_APPLY_LIST,
                activity = activity,
                resultLauncher = activityResult
            )
        }
        mBinding.rvFamily.isNestedScrollingEnabled = false
        mBinding.rvFamily.grid(5)
            .divider(R.drawable.mine_family_member_divider_item, DividerOrientation.GRID).setup {
                addType<FamilyMemberRecord>(R.layout.mine_item_family_member)
                onFastClick(R.id.cl_root) {
                    val model = getModel<FamilyMemberRecord>(modelPosition)
                    if (mViewModel.isVisitor.get() != true) {
                        jump(RouterPath.PATH_USER_PROFILE, "userId" to model.userId.orEmpty())
                    }
                }
            }.models = mutableListOf()

        mBinding.ivModify.safeClick {
            jump(
                RouterPath.PATH_MODIFY_FAMILY,
                "familyId" to (mViewModel.familyDetailModel.get()?.familyId.orEmpty()),
                "familyIcon" to (mViewModel.familyDetailModel.get()?.familyIcon.orEmpty()),
                "familyDesc" to (mViewModel.familyDetailModel.get()?.familyDesc.orEmpty()),
                "familyNotice" to (mViewModel.familyDetailModel.get()?.familyNotice.orEmpty()),
                resultLauncher = activityResult
            )
        }
        mBinding.ivQuestion.safeClick {
            mViewModel.getQQNumber {
                CommonHintDialog.newInstance("如有问题请联系专属QQ", it.orEmpty())
                    .show(supportFragmentManager)
            }
        }

    }

    private fun updateFamily(it: FamilyDetailInfoModel?) {
        it?.let {
            mViewModel.isShowSetting.set(it.isFamilyHeader || it.isFamilyAdmin)
        }
        mViewModel.isFamilyHeader.set(it?.isFamilyHeader == true)
        mViewModel.isHaveUserApply.set(it?.isHaveUserApply.orFalse())
        chatRoomId = it?.chatRoomId
        mViewModel.familyDetailModel.set(it)
        if (it?.userFamilyStatus == Constants.FAMILY_NOT_APPLY) {
            mViewModel.familyNotice.set("家族还未发出公告~")
            mViewModel.familyNoticeColor.set(resources.getColor(com.kissspace.module_common.R.color.color_949499))
        } else {
            mViewModel.familyNotice.set(it?.familyNotice ?: "家族还未发出公告~")
            if (mViewModel.familyNotice.get().isNotEmptyBlank()) {
                mViewModel.familyNoticeColor.set(resources.getColor(com.kissspace.module_common.R.color.white))
            } else {
                mViewModel.familyNoticeColor.set(resources.getColor(com.kissspace.module_common.R.color.color_949499))
            }

        }
        if (it?.familyDesc.isNotEmptyBlank()) {
            mViewModel.familyDesc.set(it?.familyDesc)
        } else {
            if (mViewModel.isFamilyHeader.get() == true) {
                mViewModel.familyHint.set("家族长编辑的对本家族的介绍~")
            } else {
                mViewModel.familyHint.set("该家族没有任何介绍")
            }
        }

        mBinding.tvNotice.safeClick {
            FamilyNoticeDialog.newInstance(mViewModel.familyNotice.get().orEmpty())
                .show(supportFragmentManager)
        }

        mViewModel.familyCreateTime.set("创建时间:  ${it?.createTime.formatDate(DateFormat.YYYY_MM_DD)}")

        mViewModel.familyIcon.set(it?.familyIcon)

        mViewModel.thisWeekInLicenseRoomRevenue.set(
            formatNum(it?.thisWeekInLicenseRoomRevenue.orZero())
        )
        mViewModel.thisWeekOutLicenseRoomRevenue.set(
            formatNum(it?.thisWeekOutLicenseRoomRevenue.orZero())
        )
        mViewModel.todayInLicenseRoomRevenue.set(
            formatNum(it?.todayInLicenseRoomRevenue.orZero())
        )
        mViewModel.todayOutLicenseRoomRevenue.set(
            formatNum(it?.todayOutLicenseRoomRevenue.orZero())
        )
        mBinding.ivFamily.loadImage(it?.familyIcon, radius = 12f)
        if (it?.isFamilyHeader == true) {
            mViewModel.isFamilyHeader.set(true)
            mBinding.cl0.safeClick {
                jump(RouterPath.PATH_FAMILY_PROFIT, "familyId" to familyId.orEmpty())
            }
            mBinding.cl1.safeClick {
                jump(RouterPath.PATH_FAMILY_OUT_PROFIT, "familyId" to familyId.orEmpty())
            }
            when (it.familyStatus) {
                Constants.FAMILY_NORMAL -> {
                    mViewModel.familyAddText.set("申请解散")
                    mViewModel.isFamilyButtonEnable.set(true)
                    mViewModel.isHideFamilyButton.set(true)
                }

                Constants.APPLY_FOR_DISSOLUTION -> {
                    mViewModel.familyAddText.set("申请解散中")
                    mViewModel.isFamilyButtonEnable.set(false)
                }

                Constants.HAS_DISSOLUTION -> {
                    finish()
                }
            }
            mViewModel.isFamilyButtonEnable.set(true)
            mBinding.tvAddFamily.safeClick {
                CommonConfirmDialog(this, "确定要申请解散家族吗", negativeString = "取消") {
                    if (this) {
                        mViewModel.dissolveFamily(MMKVProvider.userId)
                    }
                }.show()
            }
        } else {
            mViewModel.isFamilyHeader.set(false)
            //001 家族成员；002 已申请加入；003 未申请
            when (it?.userFamilyStatus) {
                Constants.FAMILY_MEMBER -> {
                    mViewModel.isHideFamilyButton.set(false)
                    mViewModel.familyAddText.set("退出家族")
                    mViewModel.isVisitor.set(false)
                    mViewModel.isFamilyButtonEnable.set(true)
                    mBinding.tvAddFamily.safeClick {
                        CommonConfirmDialog(this, "确定要退出家族吗", negativeString = "取消") {
                            if (this) {
                                //退出当前家族
                                mViewModel.moveOutFamily(MMKVProvider.userId)
                            }
                        }.show()
                    }
                }

                Constants.FAMILY_NOT_APPLY -> {
                    mViewModel.isVisitor.set(true)
                    mViewModel.familyAddText.set("申请加入")
                    mViewModel.isFamilyButtonEnable.set(true)
                    mBinding.tvAddFamily.safeClick {
                        CommonConfirmDialog(
                            this@FamilyDetailActivity,
                            "是否确认加入家族${mViewModel.familyDetailModel.get()?.familyName}",
                            ""
                        ) {
                            if (this) {
                                mViewModel.applyFamilyUser(it.familyId.orEmpty())
                            }
                        }.show()
                    }
                }

                Constants.FAMILY_APPLY -> {
                    mViewModel.familyAddText.set("已申请")
                    mViewModel.isFamilyButtonEnable.set(false)
                }

            }
        }
    }

    override fun createDataObserver() {
        super.createDataObserver()
        collectData(mViewModel.getFamilyMemberListModelEvent, onSuccess = {
            mBinding.rvFamily.bindingAdapter.addModels(it.familyMemberRecords)
        })


        //申请家族
        collectData(mViewModel.applyUserEvent, onSuccess = {
            customToast("申请成功")
            //设置回调
            setResult(RESULT_OK)
            //查询家族信息
            if (isFamilyNotMember) {
                mViewModel.getFamilyById(familyId.orEmpty()) {
                    updateFamily(it)
                }
            } else {
                //查询家族信息
                mViewModel.getSelectFamilyInfo {
                    updateFamily(it)
                }
            }
        }, onError = {
            ToastUtils.showShort(it.message)
        })
        collectData(mViewModel.dissolveFamilyEvent, onSuccess = {
            finish()
        }, onError = {
            ToastUtils.showShort(it.message)
        })
        //退出家族
        collectData(mViewModel.moveOutFamilyEvent, onSuccess = {
            finish()
        }, onError = {
            ToastUtils.showShort(it.message)
        })

        collectData(mViewModel.updateFamilyEvent, onSuccess = {
            updateFamily()
        }, onError = {
            ToastUtils.showShort(it.message)
        })

        FlowBus.observerEvent<Event.MsgFamilyEvent>(this) {
            updateFamily()
        }
        FlowBus.observerEvent<Event.MsgFamilyPassEvent>(this) {
            updateFamily()
        }
    }

    private fun updateFamily() {
        if (isFamilyNotMember) {
            mViewModel.getFamilyById(familyId.orEmpty()) {
                updateFamily(it)
            }
        } else {
            //查询家族信息
            mViewModel.getSelectFamilyInfo {
                updateFamily(it)
            }
        }
    }


}