package com.kissspace.room.widget

import android.view.Gravity
import com.kissspace.common.widget.BaseDialogFragment
import com.kissspace.module_room.R
import com.kissspace.module_room.databinding.RoomDialogGiftBinding

class GiftDialogV2 : BaseDialogFragment<RoomDialogGiftBinding>(Gravity.BOTTOM) {
    override fun getLayoutId(): Int = R.layout.room_dialog_gift

    override fun initView() {

    }
}