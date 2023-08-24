package com.kissspace.room.widget

import android.view.Gravity
import com.kissspace.common.ext.safeClick
import com.kissspace.common.widget.BaseDialogFragment
import com.kissspace.module_room.R
import com.kissspace.module_room.databinding.RoomDialogPkRuleBinding

class RoomPKRuleDialog : BaseDialogFragment<RoomDialogPkRuleBinding>(Gravity.CENTER) {
    override fun getLayoutId(): Int = R.layout.room_dialog_pk_rule

    override fun initView() {
        mBinding.ivClose.setOnClickListener { dismiss() }
    }

}