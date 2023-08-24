package com.kissspace.util.activityresult

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.style.BackgroundColorSpan
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.view.View
import androidx.annotation.NonNull


/**
 * @Author gaohangbo
 * @Date 2023/4/14 14:00.
 * @Describe
 */
object SpanUtils {


    /**
     * 变大变小
     */
    fun toSizeSpan(charSequence: CharSequence, start: Int, end: Int, scale: Float): CharSequence {
        val spannableString = SpannableString(charSequence)
        spannableString.setSpan(
            RelativeSizeSpan(scale),
            start,
            end,
            Spannable.SPAN_INCLUSIVE_EXCLUSIVE
        )
        return spannableString
    }

    /**
     * 变色
     */
    fun toColorSpan(charSequence: CharSequence, start: Int, end: Int, color: Int): CharSequence {
        val spannableString = SpannableString(charSequence)
        spannableString.setSpan(
            ForegroundColorSpan(color),
            start,
            end,
            Spannable.SPAN_INCLUSIVE_EXCLUSIVE
        )
        return spannableString
    }

    /**
     * 变背景色
     */
    fun toBackgroundColorSpan(
        charSequence: CharSequence,
        start: Int,
        end: Int,
        color: Int
    ): CharSequence {
        val spannableString = SpannableString(charSequence)
        spannableString.setSpan(
            BackgroundColorSpan(color),
            start,
            end,
            Spannable.SPAN_INCLUSIVE_EXCLUSIVE
        )
        return spannableString
    }

    private var mLastClickTime: Long = 0
    val TIME_INTERVAL = 1000

    /**
     * 可点击-带下划线
     */
    fun toClickSpan(
        charSequence: CharSequence,
        start: Int,
        end: Int,
        color: Int,
        needUnderLine: Boolean,
        listener: OnSpanClickListener?
    ): CharSequence {
        val spannableString = SpannableString(charSequence)
        val clickableSpan: ClickableSpan = object : ClickableSpan() {

            override fun updateDrawState(@NonNull ds: TextPaint) {
                ds.color = color
                ds.isUnderlineText = needUnderLine
            }

            override fun onClick(widget: View) {
                if (listener != null) {
                    //防止重复点击
                    if (System.currentTimeMillis() - mLastClickTime >= TIME_INTERVAL) {
                        //to do
                        listener.onClick(charSequence.subSequence(start, end))
                        mLastClickTime = System.currentTimeMillis()
                    }
                }
            }
        }
        spannableString.setSpan(
            clickableSpan,
            start,
            end,
            Spannable.SPAN_INCLUSIVE_EXCLUSIVE
        )
        return spannableString
    }

    interface OnSpanClickListener {
        fun onClick(charSequence: CharSequence)
    }


    /**
     * 变成自定义的字体
     */
//    fun toCustomTypeFaceSpan(
//        charSequence: CharSequence?,
//        start: Int,
//        end: Int,
//        typeface: Typeface?
//    ): CharSequence? {
//        val spannableString = SpannableString(charSequence)
//        spannableString.setSpan(
//            MyTypefaceSpan(typeface),
//            start,
//            end,
//            Spannable.SPAN_INCLUSIVE_EXCLUSIVE
//        )
//        return spannableString
//    }
}