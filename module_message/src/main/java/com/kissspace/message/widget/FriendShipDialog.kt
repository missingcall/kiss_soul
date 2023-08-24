package com.kissspace.message.widget

import androidx.fragment.app.FragmentManager
import com.kissspace.common.ext.safeClick
import com.kissspace.common.widget.BaseBottomSheetDialogFragment
import com.kissspace.module_message.databinding.MessageDialogFrendShipBinding

/**
 *
 * @Author: nicko
 * @CreateDate: 2023/2/28 10:23
 * @Description: 好友关系处理弹窗
 *
 */

class FriendShipDialog(private val text: String, private val block: (Int) -> Unit) :
    BaseBottomSheetDialogFragment<MessageDialogFrendShipBinding>() {
    override fun getViewBinding() = MessageDialogFrendShipBinding.inflate(layoutInflater)

    override fun initView() {
        mBinding.tvBlack.text = text
        mBinding.tvBlack.safeClick {
            dismiss()
            block(0)
        }

        mBinding.tvReport.safeClick {
            dismiss()
            block(1)
        }
    }

}