package com.kissspace.common.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.PopupWindow
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding

/**
 * @Author gaohangbo
 * @Date 2023/8/2 11:20.
 * @Describe
 */
open class BasePopWindow<B : ViewDataBinding>(val context: Context,
                                               layoutId: Int,
                                               attrs: AttributeSet?=null):PopupWindow(context, attrs, 0) {
    var  mBinding:B
    init {
        mBinding  = DataBindingUtil.inflate(LayoutInflater.from(context),layoutId, null, false)
        if (context is AppCompatActivity) {
            mBinding.lifecycleOwner = context
        }
        contentView = mBinding.root
        width = WindowManager.LayoutParams.MATCH_PARENT
        height = WindowManager.LayoutParams.MATCH_PARENT
        isFocusable = true
        isOutsideTouchable = true
    }


//    private open fun setAlpha(f: Float) {
//        val lp: WindowManager.LayoutParams = getWindow().getAttributes()
//        lp.alpha = f
//        getWindow().setAttributes(lp)
//    }


}

