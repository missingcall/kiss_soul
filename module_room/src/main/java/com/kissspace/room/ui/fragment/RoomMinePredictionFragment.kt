package com.kissspace.room.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import by.kirich1409.viewbindingdelegate.viewBinding
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.hjq.bar.OnTitleBarListener
import com.hjq.bar.TitleBar
import com.kissspace.common.base.BaseFragment
import com.kissspace.common.config.Constants
import com.kissspace.common.router.jump
import com.kissspace.common.model.PredictionHistoryBean
import com.kissspace.common.router.RouterPath
import com.kissspace.common.widget.CommonConfirmDialog
import com.kissspace.module_room.R
import com.kissspace.module_room.databinding.RoomFragmentMinePredictionBinding
import com.kissspace.network.net.Method
import com.kissspace.network.net.request
import com.kissspace.room.http.RoomApi
import com.kissspace.room.widget.RoomUserProfileDialog

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/12/13 17:55
 * @Description: 我的竞猜列表fragment
 *
 */
class RoomMinePredictionFragment : BaseFragment(R.layout.room_fragment_mine_prediction) {
    private lateinit var crId: String
    private val mBinding by viewBinding<RoomFragmentMinePredictionBinding>()

    companion object {
        fun newInstance(crId: String): RoomMinePredictionFragment =
            RoomMinePredictionFragment().apply {
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
        request<List<PredictionHistoryBean>>(
            RoomApi.API_GET_PREDICTION_HISTORY,
            Method.GET,
            onSuccess = {
                if (it.isNotEmpty()) {
                    mBinding.recyclerView.linear().setup {
                        addType<PredictionHistoryBean> { R.layout.room_mine_prediction_item }
                        onClick(R.id.tv_report) {
                            val model = getModel<PredictionHistoryBean>()
                            CommonConfirmDialog(
                                requireContext(),
                                "提交后平台会根据回放确认情况，如果做实有违规情况，本场积分将全部退回。",
                                negativeString = "取消"
                            ) {
                                if (this) {
                                    jump(
                                        RouterPath.PATH_REPORT,
                                        "reportType" to Constants.ReportType.USER.type,
                                        "userId" to model.userId
                                    )
                                }
                            }
                        }
                        onClick(R.id.iv_avatar) {
                            val model = getModel<PredictionHistoryBean>()
                            RoomUserProfileDialog.newInstance(
                                model.creatorId,
                                Constants.ROOM_USER_TYPE_NORMAL,
                                crId
                            ).show(childFragmentManager)
                        }
                    }.models = it
                } else {
                    mBinding.tvEmpty.visibility = View.VISIBLE
                }
            })
    }
}