package com.kissspace.common.widget

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.blankj.utilcode.util.SizeUtils
import com.kissspace.module_common.R

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/12/7
 * @Description: 通用确认对话框
 *
 */
class CommonSingleConfirmDialog(
    context: Context,
    private val title: String?,
    private val subTitle: String = "",
    private val positiveText: String? = null,
    private val block: Boolean.() -> Unit
) : Dialog(context, R.style.Theme_CustomDialog) {
    private var mTitle: TextView
    private var mSubTitle: TextView
    private var mPositive: TextView


    init {
        setContentView(R.layout.common_dialog_single_confirm)
        mTitle = findViewById(R.id.title)
        mSubTitle = findViewById(R.id.tv_sub_title)
        mPositive = findViewById(R.id.tv_positive)
        init()
    }

    private fun init() {
         mTitle.text = title
        mSubTitle.text = subTitle
        mSubTitle.visibility = if (subTitle.isNotEmpty()) View.VISIBLE else View.GONE
        positiveText?.let {
            mPositive.text = it
        }
        mPositive.setOnClickListener {
            dismiss()
            block(true)
        }
    }

    override fun onStart() {
        super.onStart()
        val attr = window?.attributes;
        if (attr != null) {
            attr.height = ViewGroup.LayoutParams.WRAP_CONTENT
            attr.width = context.resources.displayMetrics.widthPixels-SizeUtils.dp2px(64f)
            attr.gravity = Gravity.CENTER;//设置dialog 在布局中的位置
        }
    }
}