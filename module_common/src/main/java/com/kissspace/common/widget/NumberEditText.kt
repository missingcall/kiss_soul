package com.kissspace.common.widget

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText


/**
 * @Author gaohangbo
 * @Date 2023/3/15 10:41.
 * @Describe 0后面不能连续输入数字的edittext
 */
class NumberEditText : AppCompatEditText {

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context)
    }

    var onAfterTextChanged: ((text: String) -> Unit)? = null

    private fun init(context: Context) {
        this.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable) {
                var text = s.toString().trim()
                if (text.startsWith("0")) {
                    if (text.length >= 2) {
                        if ("." != text[0].toString()) {
                            text = text.substring(1, text.length)
                            setText(text)
                            try {
                                setSelection(text.length)
                            }catch (e:java.lang.Exception){
                                e.printStackTrace()
                            }
                        }
                    }
                }
                onAfterTextChanged?.invoke(text)
            }

        })
    }
}


