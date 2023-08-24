package com.kissspace.room.ui.fragment

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.blankj.utilcode.util.StringUtils
import com.drake.brv.utils.*
import com.kissspace.common.base.BaseFragment
import com.kissspace.common.config.Constants
import com.kissspace.common.model.RoomIncomeBean
import com.kissspace.common.model.RoomRankBean
import com.kissspace.common.model.RoomRankUser
import com.kissspace.module_room.R
import com.kissspace.module_room.databinding.RoomFragmentRankListBinding
import com.kissspace.network.net.Method
import com.kissspace.network.net.request
import com.kissspace.network.result.collectData
import com.kissspace.room.http.RoomApi
import com.kissspace.room.viewmodel.LiveViewModel
import com.kissspace.room.widget.RoomUserProfileDialog
import com.kissspace.util.loadImageCircle
import com.kissspace.util.logE
import com.kissspace.util.toast
import com.zego.ve.VCam

/**
 *
 * @Author: nicko
 * @CreateDate: 2023/1/31 15:59
 * @Description: 房间排行榜列表
 *
 */
class RoomRankListFragment : BaseFragment(R.layout.room_fragment_rank_list) {
    private val mBinding by viewBinding<RoomFragmentRankListBinding>()
    private val mViewModel by viewModels<LiveViewModel>()
    private lateinit var crId: String
    private lateinit var userRole: String
    private lateinit var rankCycle: String
    private lateinit var rankType: String

    companion object {
        fun newInstance(crId: String, rankCycle: String, rankType: String, userRole: String) =
            RoomRankListFragment().apply {
                arguments = bundleOf(
                    "crId" to crId,
                    "userRole" to userRole,
                    "rankType" to rankType,
                    "rankCycle" to rankCycle
                )
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rankType = arguments?.getString("rankType")!!
        rankCycle = arguments?.getString("rankCycle")!!
        crId = arguments?.getString("crId")!!
        userRole = arguments?.getString("userRole")!!
    }


    override fun initView(savedInstanceState: Bundle?) {
        if (rankCycle == "002") {
            mBinding.layoutIncome.visibility = View.VISIBLE
            mBinding.tvIncomeWeek.visibility = View.VISIBLE
            mBinding.tvIncomeToday.visibility = View.INVISIBLE
            mBinding.tvIncomeYesterday.visibility = View.INVISIBLE
            mBinding.viewLineMiddle.visibility = View.INVISIBLE
        } else if (userRole != Constants.ROOM_USER_TYPE_NORMAL && rankType == "001" && rankCycle == "001") {
            mBinding.layoutIncome.visibility = View.VISIBLE
            mBinding.tvIncomeWeek.visibility = View.GONE
        } else {
            mBinding.layoutIncome.visibility = View.GONE
        }

        mBinding.recyclerView.linear().setup {
            addType<RoomRankUser> {
                if (rankType == "001") R.layout.room_rank_list_income_item else R.layout.room_rank_list_expend_item
            }
            onBind {
                val index = findView<TextView>(R.id.tv_index)
                when (modelPosition) {
                    0 -> {
                        index.text = ""
                        index.setBackgroundResource(R.mipmap.room_icon_ranking_first)
                    }

                    1 -> {
                        index.text = ""
                        index.setBackgroundResource(R.mipmap.room_icon_ranking_second)
                    }

                    2 -> {
                        index.text = ""
                        index.setBackgroundResource(R.mipmap.room_icon_ranking_third)
                    }

                    else -> {
                        index.text = (modelPosition + 1).toString()
                        index.background = null
                    }
                }
            }
            onClick(R.id.iv_avatar) {
                if (userRole != Constants.ROOM_USER_TYPE_NORMAL) {
                    val model = getModel<RoomRankUser>()
                    RoomUserProfileDialog.newInstance(model.userId, userRole, crId)
                        .show(childFragmentManager)
                }
            }
        }.models = mutableListOf()


    }

    override fun onResume() {
        super.onResume()
        mViewModel.getRoomRankList(crId, rankCycle, rankType)
        logE("rankType=====${rankType}")
        if (userRole != Constants.ROOM_USER_TYPE_NORMAL && rankType == "001" && rankCycle == "001") {
            requestRankIncome()
        }

    }


    override fun createDataObserver() {
        super.createDataObserver()
        collectData(mViewModel.roomRankUserEvent, onSuccess = {
            mBinding.recyclerView.mutable.clear()
            if (it.rankUserList.isEmpty()) {
                mBinding.stateLayout.showEmpty()
                mBinding.layoutMine.visibility = View.GONE
            } else {
                mBinding.stateLayout.showContent()
                mBinding.layoutMine.visibility = View.VISIBLE
                mBinding.recyclerView.bindingAdapter.addModels(it.rankUserList)
                initMineInfo(it)
                mBinding.ivAvatar.setOnClickListener { _ ->
                    RoomUserProfileDialog.newInstance(it.currentUser.userId, userRole, crId)
                        .show(childFragmentManager)
                }
            }
        })
    }

    private fun initMineInfo(model: RoomRankBean) {
        if (model.isOnRank) {
            mBinding.tvIndex.visibility = View.VISIBLE
            mBinding.tvNoRank.visibility = View.GONE
            mBinding.tvNeedValue.visibility = View.GONE
            mBinding.tvIndex.text = model.index.toString()
        } else {
            mBinding.tvIndex.visibility = View.GONE
            mBinding.tvNoRank.visibility = View.VISIBLE
            mBinding.tvNeedValue.visibility = View.VISIBLE
            mBinding.tvNeedValue.text =
                StringUtils.getString(R.string.room_rank_need_value, model.currentUser.difference)
        }
        mBinding.ivAvatar.loadImageCircle(model.currentUser.profilePath)
        mBinding.tvNickname.text = model.currentUser.nickname
        mBinding.tvValue.text = model.currentUser.value.toString()
        mBinding.levelExpend.setLeveCount(model.currentUser.consumeLevel)
        mBinding.levelIncome.setLeveCount(model.currentUser.charmLevel)
    }

    private fun requestRankIncome() {
        val param = mutableMapOf<String, Any?>("chatRoomId" to crId)
        request<RoomIncomeBean>(RoomApi.API_ROOM_INCOME, Method.GET, param, onSuccess = {
            mBinding.tvIncomeToday.text = it.today + "：" + it.todayFlowTotal
            mBinding.tvIncomeYesterday.text = it.yesterday + "：" + it.yesterdayFlowTotal
        }, onError = {
            toast(it.errorMsg)
        })
    }

    private fun requestRankWeekIncome() {
        val param = mutableMapOf<String, Any?>("chatRoomId" to crId)
        request<Long>(RoomApi.API_ROOM_INCOME_WEEK, Method.GET, param, onSuccess = {
            mBinding.tvIncomeWeek.text =
                if (rankType == "001") "本周魅力值：${it}" else "本周贡献值：${it}"
        })
    }
}