package com.kissspace.room.ui.fragment

import android.os.Bundle
import by.kirich1409.viewbindingdelegate.viewBinding
import com.hjq.bar.OnTitleBarListener
import com.hjq.bar.TitleBar
import com.kissspace.common.base.BaseFragment
import com.kissspace.common.config.Constants
import com.kissspace.common.util.getH5Url
import com.kissspace.module_room.R
import com.kissspace.module_room.databinding.RoomFragmentPredicitonRuleBinding

class PredictionRuleFragment : BaseFragment(R.layout.room_fragment_prediciton_rule) {
    private val mBinding by viewBinding<RoomFragmentPredicitonRuleBinding>()
    override fun initView(savedInstanceState: Bundle?) {
        val url = getH5Url(Constants.H5.PREDICTION_INFO_URL)
        mBinding.webView.setBackgroundColor(resources.getColor(com.kissspace.module_common.R.color.common_black))
        mBinding.webView.loadUrl(url)
        mBinding.titleBar.setOnTitleBarListener(object : OnTitleBarListener {
            override fun onLeftClick(titleBar: TitleBar?) {
                super.onLeftClick(titleBar)
                parentFragmentManager.popBackStack()
            }
        })
    }
}