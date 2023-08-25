package com.kissspace.dynamic.ui.fragment

import android.os.Bundle
import by.kirich1409.viewbindingdelegate.viewBinding
import com.blankj.utilcode.util.LogUtils
import com.kissspace.common.base.BaseFragment
import com.kissspace.module_dynamic.R
import com.kissspace.module_dynamic.databinding.DynamicFragmentRecommendBinding

/**
 * @Author gaohangbo
 * @Date 2023/7/20 11:46.
 * @Describe 关注fragment
 */
class FocusFragment(private val data: String?) : BaseFragment(R.layout.dynamic_fragment_recommend) {

    private val mBinding by viewBinding<DynamicFragmentRecommendBinding>()

    override fun initView(savedInstanceState: Bundle?) {
        LogUtils.e("data$data")
    }
}