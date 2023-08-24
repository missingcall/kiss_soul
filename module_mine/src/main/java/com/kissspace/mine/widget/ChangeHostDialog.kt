package com.kissspace.mine.widget

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.kissspace.module_mine.R

class ChangeHostDialog(context: Context) :
    Dialog(context, com.kissspace.module_common.R.style.Theme_CustomDialog) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mine_dialog_change_host)
    }
}