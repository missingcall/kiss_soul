package com.kissspace.login.widget

import android.R
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Parcelable
import android.text.*
import android.text.InputFilter.LengthFilter
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.View.OnFocusChangeListener
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.view.ViewCompat
import androidx.core.widget.TextViewCompat

class XEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.editTextStyle
) : AppCompatEditText(context, attrs, defStyleAttr) {
    // =================================== APIs begin ========================================
    var separator // the separator，default is "".
            : String? = null
        private set
    private var mInteractionPadding // padding of drawables' interactive rect area.
            = 0
    private var mXTextChangeListener: OnXTextChangeListener? = null
    private var mXFocusChangeListener: OnXFocusChangeListener? = null
    private val mTextWatcher: TextWatcher
    private var mOldLength = 0
    private var mNowLength = 0
    private var mSelectionPos = 0
    private var pattern // pattern to separate. e.g.: mSeparator = "-", pattern = [3,4,4] -> xxx-xxxx-xxxx
            : IntArray? = null
    private var intervals // indexes of separators.
            : IntArray? = null
    private var hasNoSeparator // if is true, the same as EditText.
            = false

    init {
        initAttrs(context, attrs, defStyleAttr)
        mTextWatcher = MyTextWatcher()
        addTextChangedListener(mTextWatcher)
        onFocusChangeListener = OnFocusChangeListener { v, hasFocus ->
            if (mXFocusChangeListener != null) {
                mXFocusChangeListener!!.onFocusChange(v, hasFocus)
            }
        }
    }

    private fun initAttrs(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        val a = context.obtainStyledAttributes(
            attrs,
            com.kissspace.module_login.R.styleable.XEditText,
            defStyleAttr,
            0
        )
        separator = a.getString(com.kissspace.module_login.R.styleable.XEditText_x_separator)
        val pattern = a.getString(com.kissspace.module_login.R.styleable.XEditText_x_pattern)
        mInteractionPadding = a.getDimensionPixelSize(
            com.kissspace.module_login.R.styleable.XEditText_x_interactionPadding,
            DEFAULT_PADDING
        )
        a.recycle()
        if (separator == null) {
            separator = ""
        }
        hasNoSeparator = TextUtils.isEmpty(separator)
        if (separator!!.length > 0) {
            val inputType = inputType
            if (inputType == 2 || inputType == 8194 || inputType == 4098) {
                // If the inputType is number, the separator can't be inserted, so change to phone type.
                setInputType(InputType.TYPE_CLASS_PHONE)
            }
        }
        if (mInteractionPadding < 0) mInteractionPadding = 0
        dealWithInputTypes(true)
        if (!separator!!.isEmpty() && pattern != null && !pattern.isEmpty()) {
            var ok = true
            if (pattern.contains(",")) {
                val split =
                    pattern.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val array = IntArray(split.size)
                for (i in array.indices) {
                    try {
                        array[i] = split[i].toInt()
                    } catch (e: Exception) {
                        ok = false
                        break
                    }
                }
                if (ok) {
                    setPattern(array, separator!!)
                }
            } else {
                try {
                    val i = pattern.toInt()
                    setPattern(intArrayOf(i), separator!!)
                } catch (e: Exception) {
                    ok = false
                }
            }
            if (!ok) {
                Log.e("XEditText", "the Pattern format is incorrect!")
            }
        }
    }

    private fun dealWithInputTypes(fromXml: Boolean) {
        var inputType = inputType
        if (!fromXml) {
            inputType++
            if (inputType == 17) inputType++
        }
        if (!fromXml) {
            textEx = textEx
        }
    }

    override fun setInputType(type: Int) {
        super.setInputType(type)
        dealWithInputTypes(false)
    }

    override fun onTextContextMenuItem(id: Int): Boolean {
        val clipboardManager = context
            .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        if (clipboardManager != null) {
            if (id == 16908320 || id == 16908321) { // catch CUT or COPY ops
                super.onTextContextMenuItem(id)
                val clip = clipboardManager.primaryClip
                if (clip != null) {
                    val item = clip.getItemAt(0)
                    if (item != null && item.text != null) {
                        val s = item.text.toString().replace(separator!!, "")
                        clipboardManager.setPrimaryClip(ClipData.newPlainText(null, s))
                        return true
                    }
                }
            } else if (id == 16908322) { // catch PASTE ops
                val clip = clipboardManager.primaryClip
                if (clip != null) {
                    val item = clip.getItemAt(0)
                    if (item != null && item.text != null) {
                        val content = item.text.toString().replace(separator!!, "")
                        val existedTxt = textNoneNull
                        var txt: String?
                        val start = selectionStart
                        val end = selectionEnd
                        if (start * end >= 0) {
                            val startHalfEx = existedTxt.substring(0, start).replace(
                                separator!!, ""
                            )
                            txt = startHalfEx + content
                            val endHalfEx = existedTxt.substring(end).replace(separator!!, "")
                            txt += endHalfEx
                        } else {
                            txt = existedTxt.replace(separator!!, "") + content
                        }
                        textEx = txt
                        return true
                    }
                }
            }
        }
        return super.onTextContextMenuItem(id)
    }

    private val isRtl: Boolean
        private get() = ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL

    // =========================== MyTextWatcher ================================
    private inner class MyTextWatcher : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            mOldLength = s.length
            if (mXTextChangeListener != null) {
                mXTextChangeListener!!.beforeTextChanged(s, start, count, after)
            }
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            mNowLength = s.length
            mSelectionPos = selectionStart
            if (mXTextChangeListener != null) {
                mXTextChangeListener!!.onTextChanged(s, start, before, count)
            }
        }

        override fun afterTextChanged(s: Editable) {
            if (separator!!.isEmpty()) {
                if (mXTextChangeListener != null) {
                    mXTextChangeListener!!.afterTextChanged(s)
                }
                return
            }
            removeTextChangedListener(mTextWatcher)
            val trimmedText: String
            trimmedText = if (hasNoSeparator) {
                s.toString().trim { it <= ' ' }
            } else {
                s.toString().replace(separator!!.toRegex(), "").trim { it <= ' ' }
            }
            setTextToSeparate(trimmedText, false)
            if (mXTextChangeListener != null) {
                s.clear()
                s.append(trimmedText)
                mXTextChangeListener!!.afterTextChanged(s)
            }
            addTextChangedListener(mTextWatcher)
        }
    }

    private fun setCompoundDrawablesCompat(drawableEnd: Drawable) {
        val drawables = TextViewCompat.getCompoundDrawablesRelative(this)
        TextViewCompat.setCompoundDrawablesRelative(
            this,
            drawables[0],
            drawables[1],
            drawableEnd,
            drawables[3]
        )
    }

    private val isTextEmpty: Boolean
        private get() = textNoneNull.trim { it <= ' ' }.length == 0

    /**
     * Set custom separator.
     */
    fun setSeparator(separator: String) {
        if (this.separator == separator) return
        this.separator = separator
        hasNoSeparator = TextUtils.isEmpty(this.separator)
        if (separator.length > 0) {
            val inputType = inputType
            if (inputType == 2 || inputType == 8194 || inputType == 4098) {
                // If the inputType is number, the separator can't be inserted, so change to phone type.
                setInputType(InputType.TYPE_CLASS_PHONE)
            }
        }
    }

    /**
     * Set custom pattern.
     *
     * @param pattern   e.g. pattern:{4,4,4,4}, separator:"-" to xxxx-xxxx-xxxx-xxxx
     * @param separator separator
     */
    fun setPattern(pattern: IntArray, separator: String) {
        setSeparator(separator)
        setPattern(pattern)
    }

    /**
     * Set custom pattern.
     *
     * @param pattern e.g. pattern:{4,4,4,4}, separator:"-" to xxxx-xxxx-xxxx-xxxx
     */
    fun setPattern(pattern: IntArray) {
        this.pattern = pattern
        intervals = IntArray(pattern.size)
        var sum = 0
        for (i in pattern.indices) {
            sum += pattern[i]
            intervals!![i] = sum
        }
        /* When you set pattern, it will automatically compute the max length of characters and separators,
           so you don't need to set 'maxLength' attr in your xml any more(it won't work).*/
        val maxLength = intervals!![intervals!!.size - 1] + pattern.size - 1
        val oldFilters = filters
        val list: MutableList<InputFilter> = ArrayList()
        for (filter in oldFilters) {
            if (filter !is LengthFilter) list.add(filter)
        }
        list.add(LengthFilter(maxLength))
        filters = list.toTypedArray()
    }

    /**
     * Set CharSequence to separate.
     *
     */
    @Deprecated("Call {@link #setTextEx(CharSequence)} instead.")
    fun setTextToSeparate(c: CharSequence) {
        setTextToSeparate(c, true)
    }

    private fun setTextToSeparate(c: CharSequence, fromUser: Boolean) {
        if (c.length == 0 || intervals == null) {
            return
        }
        val builder = StringBuilder()
        var i = 0
        val length1 = c.length
        while (i < length1) {
            builder.append(c.subSequence(i, i + 1))
            var j = 0
            val length2 = intervals!!.size
            while (j < length2) {
                if (i == intervals!![j] && j < length2 - 1) {
                    builder.insert(builder.length - 1, separator)
                    if (mSelectionPos == builder.length - 1 && mSelectionPos > intervals!![j]) {
                        if (mNowLength > mOldLength) { // inputted
                            mSelectionPos += separator!!.length
                        } else { // deleted
                            mSelectionPos -= separator!!.length
                        }
                    }
                }
                j++
            }
            i++
        }
        val text = builder.toString()
        setText(text)
        if (fromUser) {
            val maxLength = intervals!![intervals!!.size - 1] + pattern!!.size - 1
            val index = Math.min(maxLength, text.length)
            try {
                setSelection(index)
            } catch (e: IndexOutOfBoundsException) {
                // Last resort (￣▽￣)
                val message = e.message
                if (!TextUtils.isEmpty(message) && message!!.contains(" ")) {
                    val last = message.lastIndexOf(" ")
                    val lenStr = message.substring(last + 1)
                    if (TextUtils.isDigitsOnly(lenStr)) {
                        setSelection(lenStr.toInt())
                    }
                }
            }
        } else {
            if (mSelectionPos > text.length) {
                mSelectionPos = text.length
            }
            if (mSelectionPos < 0) {
                mSelectionPos = 0
            }
            setSelection(mSelectionPos)
        }
    }

    /**
     * Get text string had been trimmed.
     */
    val textTrimmed: String
        get() = textEx.trim { it <= ' ' }
    /**
     * Get text string without separator.
     */
    /**
     * Call [.setText] or set text to separate by the pattern had been set.
     * <br></br>
     * It's especially convenient to call [.setText] in Kotlin.
     */
    var textEx: String
        get() = if (hasNoSeparator) {
            textNoneNull
        } else {
            textNoneNull.replace(separator!!.toRegex(), "")
        }
        set(text) {
            if (TextUtils.isEmpty(text) || hasNoSeparator) {
                setText(text)
                setSelection(textNoneNull.length)
            } else {
                setTextToSeparate(text, true)
            }
        }

    /**
     * Get text String had been trimmed.
     *
     */
    @get:Deprecated("Call {@link #getTextTrimmed()} instead.") val trimmedString: String
        get() = if (hasNoSeparator) {
            textNoneNull.trim { it <= ' ' }
        } else {
            textNoneNull.replace(separator!!.toRegex(), "").trim { it <= ' ' }
        }
    private val textNoneNull: String
        private get() {
            val editable = text
            return editable?.toString() ?: ""
        }

    fun hasNoSeparator(): Boolean {
        return hasNoSeparator
    }

    /**
     * Set no separator, just like a @[android.widget.EditText].
     */
    fun setNoSeparator() {
        hasNoSeparator = true
        separator = ""
        intervals = null
    }

    fun setOnXTextChangeListener(listener: OnXTextChangeListener?) {
        mXTextChangeListener = listener
    }

    fun setOnXFocusChangeListener(listener: OnXFocusChangeListener?) {
        mXFocusChangeListener = listener
    }

    /**
     * OnXTextChangeListener is to XEditText what OnTextChangeListener is to EditText.
     */
    interface OnXTextChangeListener {
        fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int)
        fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int)
        fun afterTextChanged(s: Editable?)
    }

    /**
     * OnXFocusChangeListener is to XEditText what OnFocusChangeListener is to EditText.
     */
    interface OnXFocusChangeListener {
        fun onFocusChange(v: View?, hasFocus: Boolean)
    }

    /**
     * Interface definition for a callback to be invoked when the clear drawable is clicked.
     */
    interface OnClearListener {
        fun onClear()
    }

    // =================================== APIs end ========================================
    override fun onSaveInstanceState(): Parcelable? {
        val bundle = Bundle()
        bundle.putParcelable("save_instance", super.onSaveInstanceState())
        bundle.putString("separator", separator)
        bundle.putIntArray("pattern", pattern)
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        if (state is Bundle) {
            val bundle = state
            separator = bundle.getString("separator")
            pattern = bundle.getIntArray("pattern")
            hasNoSeparator = TextUtils.isEmpty(separator)
            if (pattern != null) {
                setPattern(pattern!!)
            }
            super.onRestoreInstanceState(bundle.getParcelable("save_instance"))
            return
        }
        super.onRestoreInstanceState(state)
    }


    companion object {
        private val DEFAULT_PADDING = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 4f, Resources.getSystem().displayMetrics
        ).toInt()
    }
}