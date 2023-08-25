package com.kissspace.setting.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.kissspace.common.base.BaseFragment
import com.kissspace.common.ext.safeClick
import com.kissspace.common.http.uploadPicture
import com.kissspace.common.util.openPictureSelector
import com.kissspace.common.util.customToast
import com.kissspace.module_setting.R
import com.kissspace.module_setting.databinding.SettingFragmentSayHiPictureBinding
import com.kissspace.network.result.collectData
import com.kissspace.setting.viewmodel.SayHiViewModel
import com.kissspace.util.loadImage
import java.io.File

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/12/28 15:45
 * @Description: 打招呼图片设置
 *
 */
class SayHiPictureFragment : BaseFragment(R.layout.setting_fragment_say_hi_picture) {
    private val mBinding by viewBinding<SettingFragmentSayHiPictureBinding>()
    private val mViewModel by activityViewModels<SayHiViewModel>()
    private var currentPicture: String? = null
    override fun initView(savedInstanceState: Bundle?) {
        mBinding.vm = mViewModel
        mViewModel.getSayPictureInfo()

        mBinding.ivDeletePicture.setOnClickListener {
            currentPicture = null
            mBinding.ivPicture.setImageResource(R.mipmap.setting_icon_add_picture)
            mBinding.ivDeletePicture.visibility = View.GONE
            mViewModel.submitEnable.set(false)
        }
        mBinding.ivPicture.setOnClickListener {
            if (currentPicture == null) {
                activity?.let { it1 ->
                    openPictureSelector(it1,1) {
                        currentPicture = it?.get(0)
                        mBinding.ivPicture.loadImage(File(currentPicture!!), radius = 8f)
                        mBinding.ivDeletePicture.visibility = View.VISIBLE
                        mViewModel.submitEnable.set(true)
                    }
                }
            }
        }

        mBinding.tvSubmit.safeClick {
            if (currentPicture != null) {
                uploadPicture(mutableListOf(File(currentPicture!!))) {
                    mViewModel.submitPictureInfo(it[0])
                }
            }
        }
    }

    override fun createDataObserver() {
        super.createDataObserver()
        collectData(mViewModel.submitEvent, onSuccess = {
            customToast("保存成功")
            activity?.finish()
        })

        collectData(mViewModel.sayHiPictureInfo, onSuccess = {
            mBinding.ivDeletePicture.visibility = View.GONE
            mBinding.ivPicture.loadImage(it, radius = 8f)
            mBinding.ivDeletePicture.visibility = View.VISIBLE
            currentPicture = it
        })
    }
}