package com.kissspace.common.widget

import android.animation.ValueAnimator
import android.content.DialogInterface
import android.os.Bundle
import android.view.Gravity
import com.kissspace.module_common.R
import com.kissspace.module_common.databinding.CommonDialogRoomLoadingBinding
import com.kissspace.util.resToString

/**
 *
 * @Author: nicko
 * @CreateDate: 2023/3/8 19:46
 * @Description: 房间加载弹窗
 *
 */

class LoadingDialog(
    private var loadingText: String = R.string.common_loading.resToString()
) : BaseDialogFragment<CommonDialogRoomLoadingBinding>(Gravity.CENTER) {

    override fun getLayoutId() = R.layout.common_dialog_room_loading

    override fun initView() {
        mBinding.tvLoading.text = loadingText
    }

    fun setLoadingText(text: String) {
        loadingText = text
        mBinding.tvLoading.text = text
    }


}