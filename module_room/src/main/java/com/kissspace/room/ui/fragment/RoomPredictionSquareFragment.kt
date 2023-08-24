package com.kissspace.room.ui.fragment

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
import com.kissspace.common.model.PredictionSquareListBean
import com.kissspace.common.util.jumpRoom
import com.kissspace.module_room.R
import com.kissspace.module_room.databinding.RoomFragmentPredictionSquareBinding
import com.kissspace.network.net.Method
import com.kissspace.network.net.request
import com.kissspace.room.http.RoomApi
import com.kissspace.room.widget.RoomPredictionDialog

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/12/13 17:56
 * @Description: 竞猜广场fragment
 *
 */
class RoomPredictionSquareFragment : BaseFragment(R.layout.room_fragment_prediction_square) {
    private val mBinding by viewBinding<RoomFragmentPredictionSquareBinding>()
    private lateinit var crId: String

    companion object {
        fun newInstance(crId: String) = RoomPredictionSquareFragment().apply {
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

        request<List<PredictionSquareListBean>>(
            RoomApi.API_PREDICTION_SQUARE_LIST,
            Method.GET,
            onSuccess = {
                if (it.isEmpty()) {
                    mBinding.tvEmpty.visibility = View.VISIBLE
                } else {
                    mBinding.recyclerView.linear().setup {
                        addType<PredictionSquareListBean> { R.layout.room_prediction_square_item }
                        onBind {
                            val model = getModel<PredictionSquareListBean>()
                            val current = findView<TextView>(R.id.tv_current)
                            current.visibility =
                                if (model.chatRoomId == crId) View.VISIBLE else View.GONE
                        }
                        onClick(R.id.root, R.id.iv_poster) {
                            val model = getModel<PredictionSquareListBean>()
                            if (model.chatRoomId != crId) {
                                jumpRoom(model.chatRoomId)
                            } else {
                                (parentFragment as RoomPredictionDialog).dismiss()
                            }
                        }
                    }.models = it
                }
            })

    }


}