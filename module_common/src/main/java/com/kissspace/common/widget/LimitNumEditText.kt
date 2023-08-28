package com.kissspace.common.widget

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.text.*
import android.text.InputFilter.LengthFilter
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import com.blankj.utilcode.util.ConvertUtils.dp2px
import com.kissspace.module_common.R


class LimitNumEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(
    context, attrs, defStyleAttr
) {
    // 暴露EditText
    var editText: EditText? = null

    // 暴露TextView
    var textView: TextView? = null

    // 暴露最大字数
    var maxWordsNum = Int.MAX_VALUE // 默认为最大

    var onTextChangeListener: ((text: String) -> Unit)? = null

    var contentText: String? = null
        get() = editText?.text.toString()
        set(value) {
            if (value == field) {
                return
            }
            field = value
            onTextChangeListener?.invoke(value.orEmpty())
        }
    var hintText: String? = null
        get() = editText?.hint.toString()
        set(value) {
            if (value == field) {
                return
            }
            field = value
        }
    var limitBackground: Drawable? = null

    // 标记显示已输入字数颜色值
    private var signInputHaveNumColor: String? = null


    // 设置输入框文本大小
    fun setEditTextSize(size: Float): LimitNumEditText {
        if (editText != null) editText!!.textSize = size
        return this
    }

    fun setEnable(enable: Boolean): LimitNumEditText {
        if (editText != null) {
            editText?.isEnabled = enable
            editText?.isFocusable = false//设置输入框不能输入
            editText?.isFocusableInTouchMode = false//设置不可触摸
        }
        return this
    }

    // 设置输入框文本颜色
    fun setEditTextColor(color: Int): LimitNumEditText {
        try {
            if (editText != null) editText!!.setTextColor(color)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return this
    }

    // 设置输入框显示行数
    fun setEditTextLineNum(lineNum: Int): LimitNumEditText {
        if (editText != null) editText!!.setLines(lineNum)
        return this
    }

    // 设置输入框显示最大行数
    fun setEditTextMaxLines(maxLines: Int): LimitNumEditText {
        if (editText != null) editText!!.maxLines = maxLines
        return this
    }

    // 设置输入框默认文字
    fun setEditTextHint(hint: String?): LimitNumEditText {
        if (editText != null) editText?.hint = hint
        return this
    }

    // 设置输入框默认文字颜色
    private fun setEditTextHintColor(color: Int): LimitNumEditText {
        try {
            if (editText != null) editText!!.setHintTextColor(resources.getColor(color))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return this
    }

    // 设置输入框最大字数
    private fun setEditTextMaxWordsNum(maxWordsNum: Int): LimitNumEditText {
        this.maxWordsNum = maxWordsNum
        if (editText != null) {
            editText!!.filters = arrayOf<InputFilter>(LengthFilter(maxWordsNum + 1))
            updateSignTv()
        }
        return this
    }

    // 设置输入框Margin
    //    public LimitNumEditText setEditTextMargin(int left, int top, int right, int bottom) {
    //        if (editText != null && layoutParams != null) {
    //            layoutParams.setMargins(left, top, right, bottom);
    //            editText.setLayoutParams(layoutParams);
    //        }
    //        return this;
    //    }

    // 设置标记控件文字大小
    fun setTextViewSize(size: Float): LimitNumEditText {
        if (textView != null) textView?.setTextSize(TypedValue.COMPLEX_UNIT_SP, size)
        return this
    }

    // 设置标记控件文字颜色
//    fun setTextViewColor(color: String?): LimitNumEditText {
//        try {
//            if (textView != null) textView?.setTextColor(Color.parseColor(color))
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//        return this
//    }

    // 设置输入类型
    fun setInputType(type: Int): LimitNumEditText {
        if (editText != null) editText?.inputType = type
        return this
    }

    /**
     * 标记显示已输入字数颜色值
     *
     * @param color 待显示颜色值
     */
    fun setSignInputHaveNumColor(color: String?): LimitNumEditText {
        signInputHaveNumColor = color
        updateSignTv()
        return this
    }

    // 更新标记TextView
    private fun updateSignTv() {
        if (textView != null && editText != null) {
            val num = editText?.text?.length
            var text = "$num/$maxWordsNum"
            if (maxWordsNum == Int.MAX_VALUE) text = "$num/∞" else if (maxWordsNum <= 0) text =
                "$num/0"
            if (!TextUtils.isEmpty(signInputHaveNumColor)) {
                try {
                    val spannableString = SpannableString(text)
                    spannableString.setSpan(
                        ForegroundColorSpan(Color.parseColor(signInputHaveNumColor)),
                        0, (num.toString() + "").length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    textView?.text = spannableString
                } catch (e: Exception) {
                    textView?.text = text
                }
            } else textView?.text = text
        }
    }

    // 当输入字数多于最大字数事件接口
    interface OnMoreMaxWordsNumListener {
        fun onMoreMaxWordsNum(maxNum: Int)
    }

    private var onMoreMaxWordsNumListener: OnMoreMaxWordsNumListener? = null
    fun setOnMoreMaxWordsNumListener(onMoreMaxWordsNumListener: OnMoreMaxWordsNumListener?) {
        this.onMoreMaxWordsNumListener = onMoreMaxWordsNumListener
    }

    // 输入框内容变化事件
    interface OnTextChangedListener {
        fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int)
        fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int)
        fun afterTextChanged(s: Editable?)
    }

    private var onTextChangedListener: OnTextChangedListener? = null


    init {
        if (attrs != null) {
            val typedArray = context.obtainStyledAttributes(
                attrs,
                R.styleable.LimitNumEditText
            )
            maxWordsNum = typedArray.getInt(
                R.styleable.LimitNumEditText_maxWordsNum,
                Int.MAX_VALUE
            )
            limitBackground = typedArray.getDrawable(R.styleable.LimitNumEditText_limitBackground)

            val hintText = typedArray.getString(R.styleable.LimitNumEditText_hintText)
            val hintTextColor = typedArray.getColor(R.styleable.LimitNumEditText_hintTextColor,resources.getColor(R.color.color_949499))
            val tipTextColor = typedArray.getColor(R.styleable.LimitNumEditText_tipTextColor,resources.getColor(R.color.color_C4C1D7))
            val limitNumTextColor = typedArray.getColor(
                R.styleable.LimitNumEditText_limitTextColor,
                resources.getColor(R.color.color_A8A8B3)
            )

            //验证码字体大小
            val limitNumTextSize = typedArray.getDimensionPixelSize(
                R.styleable.LimitNumEditText_limitTextSize,
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 15f, resources.displayMetrics)
                    .toInt()
            ).toFloat()
            typedArray.recycle()

            if (limitBackground == null) {
                setBackgroundResource(R.drawable.common_edittextdrawable)
            } else {
                background = limitBackground
            }
            val editParams =
                LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            editText = EditText(context)
            editText?.background=null
            // layoutParams.setMargins(10, 10, 10, 10);
            editText?.layoutParams = editParams

            editText?.setPadding(dp2px(16f), dp2px(16f), dp2px(16f), dp2px(36f))
            editText?.setTextSize(TypedValue.COMPLEX_UNIT_PX, limitNumTextSize)
            editText?.setHintTextColor(hintTextColor)
            editText?.setTextColor(limitNumTextColor)
            editText?.setLineSpacing(6f, 1f)

            editText?.gravity = Gravity.TOP
            editText?.addTextChangedListener(object : TextWatcher {
                private var tempText: CharSequence? = null
                private var selectionStart = 0
                private var selectionEnd = 0
                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                    if (onTextChangedListener != null) onTextChangedListener!!.beforeTextChanged(
                        s,
                        start,
                        count,
                        after
                    )
                    tempText = s
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    if (onTextChangedListener != null) onTextChangedListener!!.onTextChanged(
                        s,
                        start,
                        before,
                        count
                    )
                }

                override fun afterTextChanged(s: Editable) {
                    updateSignTv()
                    contentText = s.toString()
                    selectionStart = editText!!.selectionStart
                    selectionEnd = editText!!.selectionEnd
                    if (tempText!!.length > maxWordsNum) {
                        s.delete(selectionStart - 1, selectionEnd)
                        val tempSelection = selectionStart
                        editText!!.text = s
                        // 设置光标在最前面
                        editText!!.setSelection(tempSelection)
                        if (onMoreMaxWordsNumListener != null) onMoreMaxWordsNumListener!!.onMoreMaxWordsNum(
                            maxWordsNum
                        )
                    }
                    if (onTextChangedListener != null) onTextChangedListener!!.afterTextChanged(s)
                }
            })
            this.addView(editText, editParams)
            setEditTextMaxWordsNum(maxWordsNum)
            setEditTextHint(hintText)
            val layoutParams1 =
                LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            layoutParams1.addRule(ALIGN_PARENT_RIGHT, TRUE)
            layoutParams1.addRule(ALIGN_PARENT_BOTTOM, TRUE)

            textView = TextView(context)
            textView?.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            textView?.setTextColor(tipTextColor)
            textView?.layoutParams = layoutParams1

            addView(textView, layoutParams1)
            textView?.setPadding(0, dp2px(12f), dp2px(12f), dp2px(12f));
            textView?.gravity = Gravity.BOTTOM or Gravity.END
            updateSignTv()
        }
    }
}