package com.kissspace.setting.ui.fragment

import android.os.Bundle
import androidx.fragment.app.activityViewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.kissspace.common.base.BaseFragment
import com.kissspace.common.util.customToast
import com.kissspace.module_setting.R
import com.kissspace.module_setting.databinding.SettingFragmentSayHiTextBinding
import com.kissspace.network.result.collectData
import com.kissspace.setting.viewmodel.SayHiViewModel

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/12/28 15:45
 * @Description: 打招呼文本设置
 *
 */
class SayHiTextFragment : BaseFragment(R.layout.setting_fragment_say_hi_text) {
    private val mBinding by viewBinding<SettingFragmentSayHiTextBinding>()
    private val mViewModel by activityViewModels<SayHiViewModel>()
    override fun initView(savedInstanceState: Bundle?) {
        mBinding.vm = mViewModel
        mViewModel.getSayHiTextInfo()
        mBinding.tvSubmit.setOnClickListener {
            mViewModel.submitTextInfo()
        }
    }

    override fun createDataObserver() {
        super.createDataObserver()
        collectData(mViewModel.submitEvent, onSuccess = {
            customToast("保存成功")
            activity?.finish()
        })

        collectData(mViewModel.sayHiTextContent, onSuccess = {
            mBinding.edit.setText(it)
        })
    }
}