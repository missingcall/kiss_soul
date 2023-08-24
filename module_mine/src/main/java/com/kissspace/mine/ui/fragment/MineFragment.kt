package com.kissspace.mine.ui.fragment

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.didi.drouter.api.DRouter
import com.kissspace.common.base.BaseFragment
import com.kissspace.common.config.Constants
import com.kissspace.common.router.jump
import com.kissspace.common.ext.safeClick
import com.kissspace.common.http.getSelectPayChannelList
import com.kissspace.common.provider.IPayProvider
import com.kissspace.common.util.*
import com.kissspace.common.router.RouterPath
import com.kissspace.common.util.mmkv.MMKVProvider
import com.kissspace.common.widget.CommonHintDialog
import com.kissspace.mine.viewmodel.FamilyViewModel
import com.kissspace.mine.viewmodel.MineViewModel
import com.kissspace.mine.widget.FirstChargeDialog
import com.kissspace.module_mine.R
import com.kissspace.module_mine.databinding.FragmentMineBinding
import com.kissspace.common.http.getUserInfo
import com.kissspace.util.logE
import com.kissspace.util.orFalse
import kotlin.collections.isNotEmpty

class MineFragment : BaseFragment(R.layout.fragment_mine) {

    private val mBinding by viewBinding<FragmentMineBinding>()

    private val mViewModel by viewModels<MineViewModel>()

    private val familyModel by viewModels<FamilyViewModel>()


    override fun initView(savedInstanceState: Bundle?) {
        mBinding.m = mViewModel
        mBinding.lifecycleOwner = this
        mViewModel.isShowFirstRecharge.value = MMKVProvider.firstRecharge
        mBinding.layoutSettings.safeClick {
            jump(RouterPath.PATH_SETTING)
        }
        mBinding.layoutFamily.safeClick {
            //判断当前是否已经加入到家族
            familyModel.getSelectFamilyInfo {
                if (it != null) {
                    if (it.userFamilyStatus == Constants.FAMILY_MEMBER || it.userFamilyStatus == Constants.FAMILY_LICENSED_HOMEOWNER || it.userFamilyStatus == Constants.FAMILY_HEADER) {
                        //跳转到家族详情页面
                        jump(RouterPath.PATH_FAMILY_DETAIL, "familyId" to it.familyId.orEmpty())
                    } else {
                        //跳转到申请家族列表
                        jump(RouterPath.PATH_FAMILY_LIST)
                    }
                } else {
                    logE("家族信息查询失败")
                    jump(RouterPath.PATH_FAMILY_LIST)
                }
            }
        }

        mBinding.clCharge.safeClick {
            getSelectPayChannelList {
                val firstChargeDialog = FirstChargeDialog(requireContext())
                firstChargeDialog.callBack = { payChannelType: String, payProductId: String ->
                    DRouter.build(IPayProvider::class.java).getService().pay(
                        payChannelType,
                        payProductId,
                        activity as AppCompatActivity
                    ) { result ->
                        if (result) {
                            MMKVProvider.firstRecharge = false
                            mViewModel.isShowFirstRecharge.value = false
                        }
                        firstChargeDialog.dismiss()
                    }
                }
                firstChargeDialog.show()
                if (it.isNotEmpty()) {
                    if (it.size > 1) {
                        firstChargeDialog.setData(
                            it[0].firstRechargePayProductListResponses,
                            it[1].firstRechargePayProductListResponses
                        )
                    }
                }
            }
        }

        mBinding.layoutWallet.setOnClickListener {
            jump(RouterPath.PATH_USER_WALLET)
        }
        mBinding.layoutStore.safeClick {
            jump(RouterPath.PATH_STORE)
        }

        mBinding.layoutFeedback.safeClick {
            jump(
                RouterPath.PATH_FEEDBACK_TYPE_LIST,
                "showFeedBack" to mViewModel.feedBackNewMessage.value.orFalse()
            )
        }

        mBinding.layoutTaskCenter.safeClick {
            jump(RouterPath.PATH_TASK_CENTER_LIST)
        }

        mBinding.ivAvatar.safeClick {
            jump(RouterPath.PATH_USER_PROFILE, "userId" to MMKVProvider.userId)
        }

        mBinding.ivEdit.safeClick {
            jump(RouterPath.PATH_EDIT_PROFILE)
        }

        mBinding.lltCollect.safeClick {
            jump(RouterPath.PATH_MY_COLLECT)
        }

        mBinding.lltFollow.safeClick {
            jump(RouterPath.PATH_MY_FOLLOW)
        }

        mBinding.tvUserId.setOnClickListener {
            mViewModel.userInfo.value?.let {
                copyClip(it.beautifulId.ifEmpty { it.displayId })
            }

        }

        mBinding.lltFans.safeClick {
            jump(RouterPath.PATH_MY_FANS)
        }


        mBinding.lltVisitor.safeClick {
            jump(RouterPath.PATH_MY_VISITOR)
        }

        mBinding.layoutGrade.safeClick {
            jump(RouterPath.PATH_MY_LEVEL)
        }
        mBinding.layoutActionCenter.safeClick {
            jump(
                RouterPath.PATH_WEBVIEW,
                "url" to getH5Url(Constants.H5.centerActionUrl, needToken = true),
                "showTitle" to true
            )
        }

        mBinding.layoutRoom.safeClick {
            jumpRoom(roomType = Constants.ROOM_TYPE_PARTY)
        }

        mBinding.layoutCustomerService.safeClick {
            CommonHintDialog.newInstance(
                "若有问题请关注微信公众号",
                MMKVProvider.wechatPublicAccount
            ).show(childFragmentManager)
        }

    }


    override fun onResume() {
        super.onResume()
        refreshUserinfo()
        mViewModel.isShowFirstRecharge.value = MMKVProvider.firstRecharge
        //获取我的页面新消息状态
        mViewModel.queryNewMessageStatus {
            mViewModel.familyNewMessage.value = it.familyMessage != null && it.familyMessage != 0
            mViewModel.walletNewMessage.value = it.walletMessage != null && it.walletMessage != 0
            mViewModel.taskNewMessage.value =
                it.taskCenterMessage != null && it.taskCenterMessage != 0
            mViewModel.feedBackNewMessage.value =
                it.feedbackMessage != null && it.feedbackMessage != 0
            mViewModel.taskCenterMessage.value =
                it.taskCenterMessage != null && it.taskCenterMessage != 0
        }

    }

    private fun refreshUserinfo() {
        getUserInfo(onSuccess = {
            mViewModel.userInfo.value = it
        })
    }

}