package com.kissspace.setting.widget

import android.app.Dialog
import android.content.Context
import android.widget.EditText
import android.widget.TextView
import com.drake.net.NetConfig
import com.kissspace.common.util.loginOut
import com.kissspace.module_setting.R
import com.kissspace.util.toast

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/12/7
 * @Description: 通用确认对话框
 *
 */
class ChangeHostDialog(context: Context) :
    Dialog(context, com.kissspace.module_common.R.style.Theme_CustomDialog) {
    private var mPositive: TextView
    private var mNegative: TextView
    private var mEditText: EditText

    init {
        setContentView(R.layout.setting_dialog_change_host)
        mPositive = findViewById(R.id.tv_positive)
        mNegative = findViewById(R.id.tv_negative)
        mEditText = findViewById(R.id.edit_host)
        init()
    }

    private fun init() {
        mEditText.setText(NetConfig.host)
        mNegative.setOnClickListener { dismiss() }
        mPositive.setOnClickListener {
            val text = mEditText.text.toString().trim()
            if (text.isNotEmpty()) {
                loginOut()
            } else {
                toast("请输入服务器地址")
            }

        }
    }

}