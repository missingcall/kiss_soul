package com.kissspace.dynamic.ui.fragment

import android.os.Bundle
import by.kirich1409.viewbindingdelegate.viewBinding
import com.drake.brv.utils.linear
import com.drake.brv.utils.mutable
import com.drake.brv.utils.setup
import com.kissspace.common.base.BaseFragment
import com.kissspace.common.model.dynamic.DynamicDetailCommentInfo
import com.kissspace.dynamic.Dynamic
import com.kissspace.dynamic.http.DynamicApi
import com.kissspace.module_dynamic.R
import com.kissspace.module_dynamic.databinding.DynamicFragmentDetailCommentBinding
import com.kissspace.network.net.Method
import com.kissspace.network.net.request
import com.kissspace.util.logE

class DynamicCommentFragment(private val dynamicId:String):BaseFragment(R.layout.dynamic_fragment_detail_comment) {
    private val mBinding by viewBinding<DynamicFragmentDetailCommentBinding>()
    override fun initView(savedInstanceState: Bundle?) {
        mBinding.recyclerView.linear().setup {
            addType<DynamicDetailCommentInfo>(R.layout.dynamic_detail_comment_item)
        }.mutable = mutableListOf()
        initData()

    }

    private fun initData(){
        val params = mutableMapOf<String,Any?>("dynamicId" to dynamicId)
        request<List<DynamicDetailCommentInfo>>(DynamicApi.API_GET_COMMENT_DETAIL, method = Method.GET, param = params, onSuccess = {
            mBinding.recyclerView.mutable.addAll(it)
        })
    }
}