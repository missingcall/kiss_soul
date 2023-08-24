package com.kissspace.room.widget

import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import com.kissspace.common.config.Constants
import com.kissspace.common.ext.safeClick
import com.kissspace.common.widget.BaseBottomSheetDialogFragment
import com.kissspace.module_room.databinding.RoomDialogShareBinding
import com.kissspace.room.viewmodel.PredictionViewModel
import com.kissspace.room.viewmodel.TaskViewModel

class RoomShareDialog(private var block: ((type:Int) -> Unit)? = null): BaseBottomSheetDialogFragment<RoomDialogShareBinding>(){

    private val mViewModel by viewModels<TaskViewModel>()
    override fun getViewBinding(): RoomDialogShareBinding = RoomDialogShareBinding.inflate(layoutInflater)

    override fun initView() {
        mBinding.tvShareFriend.safeClick {
            mViewModel.shareChatRoom()
            block?.invoke(Constants.SharePlatform.WECHAT.type)

        }

        mBinding.tvShareCircle.safeClick {
            mViewModel.shareChatRoom()
            block?.invoke(Constants.SharePlatform.WECHAT_FRIEND.type)
        }
    }


}