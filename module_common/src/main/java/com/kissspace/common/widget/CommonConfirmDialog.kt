package com.kissspace.common.widget

import android.app.Dialog
import android.content.Context
import android.text.SpannableStringBuilder
import android.view.View
import android.view.WindowManager
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
    private var mContent: TextView


    init {
        setContentView(R.layout.common_dialog_confirm)
        mTitle = findViewById(R.id.title)
        mSubTitle = findViewById(R.id.tv_sub_title)
        mPositive = findViewById(R.id.tv_positive)
        mNegative = findViewById(R.id.tv_negative)
        mContent = findViewById(R.id.tv_content)
        init()
    }

    override fun onStart() {
        super.onStart()
        if (window != null){
            val layoutParams = WindowManager.LayoutParams()
            layoutParams.copyFrom(window!!.attributes)
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
            window!!.attributes = layoutParams
        }
    }

    private fun init() {
        setCancelable(cancelable)
        mSubTitle.text = subTitle
        if (subTitle.isNotEmpty()){
            mSubTitle.visibility = View.VISIBLE
            mTitle.text = title
            titleSpannableString?.let {
                mTitle.text = titleSpannableString
            }
        }else{
            mSubTitle.visibility = View.INVISIBLE
            mTitle.visibility = View.INVISIBLE
            mContent.visibility = View.VISIBLE
            mContent.text = title
        }

//        negativeString?.let {
//            mNegative.text = it
//        }
//        positiveString?.let {
//            mPositive.text = it
//        }
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