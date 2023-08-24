package com.kissspace.common.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import androidx.core.content.withStyledAttributes
import androidx.databinding.DataBindingUtil
import com.kissspace.module_common.R
import com.kissspace.module_common.databinding.CommonLayoutSearchviewBinding
import com.kissspace.util.trimString


/**
 * @Author gaohangbo
 * @Date 2023/4/7 18:00.
 * @Describe 通用搜索View
 */
class CommonSearchView(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    var onEditorActionBlock: ((String) -> Unit)? = null

    var mBinding: CommonLayoutSearchviewBinding

    init {
        mBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.common_layout_searchview,
            this,
            true
        )
        orientation = HORIZONTAL
        context.withStyledAttributes(attrs, R.styleable.CommonSearchView) {
            val hintText = getString(R.styleable.CommonSearchView_search_hintText)
            val hintTextColor = getColor(
                R.styleable.CommonSearchView_search_hintTextColor,
                resources.getColor(R.color.color_CCC7CC)
            )
            //设置背景
//            val background = getResourceId(
//                R.styleable.CommonSearchView_search_background,
//                R.drawable.common_search_drawable
//            )
//            mBinding.etSearch.setBackgroundResource(background)
            val isShowCancelText = getBoolean(R.styleable.CommonSearchView_isShowCancelText, false)
            val isShowCancelIcon = getBoolean(R.styleable.CommonSearchView_isShowClearIcon, false)
            mBinding.cancel.visibility = if (isShowCancelText) View.VISIBLE else View.GONE
            mBinding.etSearch.hint = hintText
            mBinding.etSearch.setHintTextColor(hintTextColor)
            mBinding.etSearch.boolShowCancelButton = isShowCancelIcon
            mBinding.etSearch.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    onEditorActionBlock?.invoke(mBinding.etSearch.text.trimString())
                }
                true
            }
        }

    }
}