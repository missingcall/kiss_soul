package com.kissspace.mine.widget

import android.view.Gravity
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.SizeUtils
import com.kissspace.common.ext.safeClick
import com.kissspace.common.widget.BaseDialogFragment
import com.kissspace.mine.viewmodel.FamilyViewModel
import com.kissspace.module_mine.R
import com.kissspace.module_mine.databinding.MineDialogFamilyNoticeHintBinding

/**
 * @Author gaohangbo
 * @Date 2022/12/28 07:55.
 * @Describe 家族提示广告
 */
class FamilyNoticeDialog : BaseDialogFragment<MineDialogFamilyNoticeHintBinding>(Gravity.CENTER) {
    private val mViewModel by viewModels<FamilyViewModel>()
    private var noticeContent:String?= null
    override fun getLayoutId(): Int {
        return R.layout.mine_dialog_family_notice_hint
    }
    companion object{
        fun newInstance(noticeContent:String) = FamilyNoticeDialog().apply {
            arguments = bundleOf("noticeContent" to noticeContent)
        }
    }
    override fun initView() {
        arguments?.let {
            noticeContent = it.getString("noticeContent")
        }
        mBinding.m = mViewModel
        mViewModel.familyNotice.set(noticeContent)
        mViewModel.isNoticeEmpty.set(noticeContent?.isEmpty() == true)
        mBinding.ivClose.safeClick {
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            val attr = attributes
            attr.width = context.resources.displayMetrics.widthPixels-SizeUtils.dp2px(64f)
            attr.height = ConvertUtils.dp2px(325f)
            attributes = attr
        }
    }
}