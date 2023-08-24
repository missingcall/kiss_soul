package com.kissspace.common.widget

import android.app.Dialog
import android.content.Context
import android.text.SpannableStringBuilder
import android.view.View
import android.widget.TextView
import com.kissspace.module_common.R

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/12/7
 * @Description: 通用确认对话框
 *
 */
class CommonConfirmDialog(
    context: Context,
    private val title: String,
    private val subTitle: String = "",
    private val positiveString: String? = null,
    private val negativeString: String? = null,
    private val isShowNegative: Boolean = true,
    private val cancelable: Boolean = true,
    private val titleSpannableString: SpannableStringBuilder? = null,
    private val block: Boolean.() -> Unit
) : Dialog(context, R.style.Theme_CustomDialog) {
    private var mTitle: TextView
    private var mSubTitle: TextView
    private var mPositive: TextView
    private var mNegative: TextView


    init {
        setContentView(R.layout.common_dialog_confirm)
        mTitle = findViewById(R.id.title)
        mSubTitle = findViewById(R.id.tv_sub_title)
        mPositive = findViewById(R.id.tv_positive)
        mNegative = findViewById(R.id.tv_negative)
        init()
    }

    private fun init() {
        setCancelable(cancelable)
        mTitle.text = title
        titleSpannableString?.let {
            mTitle.text = titleSpannableString
        }
        mSubTitle.text = subTitle
        mSubTitle.visibility = if (subTitle.isNotEmpty()) View.VISIBLE else View.GONE

        negativeString?.let {
            mNegative.text = it
        }
        positiveString?.let {
            mPositive.text = it
        }
        mNegative.setOnClickListener {
            dismiss()
            block(false)
        }
        mPositive.setOnClickListener {
            dismiss()
            block(true)
        }
        if (!isShowNegative) {
            mNegative.visibility = View.GONE
        } else {
            mNegative.visibility = View.VISIBLE
        }
    }

}