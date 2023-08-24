package com.kissspace.room.ui.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.os.bundleOf
import by.kirich1409.viewbindingdelegate.viewBinding
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.hjq.bar.OnTitleBarListener
import com.hjq.bar.TitleBar
import com.kissspace.common.base.BaseFragment
import com.kissspace.common.model.PredictionRankingBean
import com.kissspace.common.model.PredictionRankingListBean
import com.kissspace.common.util.formatNum
import com.kissspace.util.resToColor
import com.kissspace.module_room.R
import com.kissspace.module_room.databinding.RoomFragmentPredictionRankingBinding
import com.kissspace.network.net.Method
import com.kissspace.network.net.request
import com.kissspace.room.http.RoomApi
import com.kissspace.util.loadImageCircle

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/12/13 17:56
 * @Description: 竞猜广场fragment
 *
 */
class RoomPredictionRankingFragment : BaseFragment(R.layout.room_fragment_prediction_ranking) {
    private val mBinding by viewBinding<RoomFragmentPredictionRankingBinding>()
    private lateinit var crId: String

    companion object {
        fun newInstance(crId: String) = RoomPredictionRankingFragment().apply {
            arguments = bundleOf("crId" to crId)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        crId = arguments?.getString("crId")!!
    }

    override fun initView(savedInstanceState: Bundle?) {
        mBinding.titleBar.setOnTitleBarListener(object : OnTitleBarListener {
            override fun onLeftClick(titleBar: TitleBar?) {
                parentFragmentManager.popBackStack()
            }
        })

        mBinding.tvGoBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        request<PredictionRankingBean>(RoomApi.API_PREDICTION_RANKING,
            Method.GET,
            mutableMapOf("chatRoomId" to crId),
            onSuccess = {
                mBinding.layoutMineRank.visibility = if (it.nickname.isNullOrEmpty()) View.GONE else View.VISIBLE
                mBinding.tvNickname.text = it.nickname
                mBinding.ivAvatar.loadImageCircle(it.profilePath)
                mBinding.tvIncome.text = formatNum(it.profit)
                mBinding.tvRanking.text = it.ranking.toString()
                initRecyclerView(it.rankingList)
            })

    }

    @SuppressLint("SetTextI18n")
    private fun initRecyclerView(data: List<PredictionRankingListBean>) {
        mBinding.recyclerView.linear().setup {
            addType<PredictionRankingListBean> { R.layout.room_layout_prediction_ranking_list_item }
            onBind {
                val model = getModel<PredictionRankingListBean>()
                val index = findView<TextView>(R.id.tv_ranking)
                index.text = (modelPosition + 1).toString()
                val color = when (modelPosition) {
                    0 -> com.kissspace.module_common.R.color.color_EA4240.resToColor()
                    1 -> com.kissspace.module_common.R.color.color_ED6E38.resToColor()
                    2 -> com.kissspace.module_common.R.color.color_E9B23C.resToColor()
                    else -> com.kissspace.module_common.R.color.common_white.resToColor()
                }
                index.setTextColor(color)

                val income = findView<TextView>(R.id.tv_income)
                income.text = formatNum(model.profit)
            }
        }.models = data
    }
}