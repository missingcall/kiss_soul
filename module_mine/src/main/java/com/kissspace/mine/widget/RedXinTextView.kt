package com.kissspace.mine.widget

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.kissspace.module_mine.R
import com.kissspace.util.logE

class RedXinTextView constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {
    constructor(context: Context) : this(context, null, 0)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs,0)
    var orientation=0
    var spannableStringBuilder:SpannableStringBuilder?=null
    override fun setText(text: CharSequence, type: BufferType) {
        if(text.isNotEmpty()){
            if(orientation==1){
                if (text.subSequence(0, 1).toString() != "*"){
                    spannableStringBuilder = SpannableStringBuilder("*")
                    spannableStringBuilder?.append(text)
                    spannableStringBuilder?.setSpan(
                        ForegroundColorSpan(Color.RED),
                        0,
                        1,
                        Spannable.SPAN_EXCLUSIVE_INCLUSIVE
                    ) //设置指定位置文字的颜色
                }
            }else{
                if (text.subSequence(text.length-1, text.length).toString() != "*"){
                    spannableStringBuilder = SpannableStringBuilder(text)
                    spannableStringBuilder?.append("*")
                    spannableStringBuilder?.setSpan(
                        ForegroundColorSpan(Color.RED),
                        text.length,
                        text.length+1,
                        Spannable.SPAN_EXCLUSIVE_INCLUSIVE
                    ) //设置指定位置文字的颜色
                }
            }
            super.setText(spannableStringBuilder, type)
        }
    }

    init {
        val a: TypedArray =
            context.obtainStyledAttributes(attrs, R.styleable.RedXinTextView, defStyleAttr, 0)
        orientation = a.getInt(
            R.styleable.RedXinTextView_redxin_orientation,
            0
        )
        a.recycle()
    }
}