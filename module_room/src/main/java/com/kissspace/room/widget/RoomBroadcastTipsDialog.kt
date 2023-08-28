package com.kissspace.room.widget
import android.content.Context
import com.kissspace.common.ext.safeClick
import com.kissspace.common.widget.BaseDialog
import com.kissspace.module_room.R
import com.kissspace.module_room.databinding.RoomDialogBroadcastTipsBinding

class RoomBroadcastTipsDialog (context: Context) :
    BaseDialog<RoomDialogBroadcastTipsBinding>(context,true)  {

    override fun getLayoutId() = R.layout.room_dialog_broadcast_tips

    override fun initView() {
        mBinding.ivClose.safeClick {
            dismiss()
        }
    }

}