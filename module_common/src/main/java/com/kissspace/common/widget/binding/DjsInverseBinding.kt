package com.kissspace.common.widget.binding

import androidx.databinding.*
import com.kissspace.common.util.format.Format
import com.kissspace.common.widget.LimitNumEditText
import com.kissspace.util.logE

/**
 * @author Loja
 * @created on 3/28/19.
 */
@InverseMethod("doubleToString")
fun doubleToString(value:Double):String {
    return Format.O_OO.format(value)
}


@InverseMethod("doubleToIntToString")
fun doubleToIntToString(value:Double):String {
    logE("doubleToIntToString11111"+Format.E.format(value))
    return Format.E.format(value)
}



@InverseMethod("doubleFormatString")
fun doubleFormatString(value:Double):String {
    logE("doubleFormatString"+Format.E.format(value))
    return Format.E_EE.format(value)
}


@InverseMethod("nullToAny")
fun nullToAny(value:Any?):Any {
    return value ?: ""
}
@InverseBindingMethods(
    InverseBindingMethod(
        type = LimitNumEditText::class,
        attribute = DjsInverseBinding.ATTR_LMT_TEXT
    )
)

object DjsInverseBinding {
    const val ATTR_LMT_TEXT = "contentText"
    const val ATTR_LMT_HINT_TEXT = "hintText"

    @JvmStatic
    @InverseBindingAdapter(attribute = ATTR_LMT_TEXT)
    fun getContentText(view: LimitNumEditText) = view.contentText

    @JvmStatic
    @BindingAdapter(ATTR_LMT_TEXT)
    fun setContentText(view: LimitNumEditText, text: String?) {
        if (text != view.editText?.text.toString()) {
            view.editText?.setText(text)
        }

    }

    @JvmStatic
    @InverseBindingAdapter(attribute = ATTR_LMT_HINT_TEXT)
    fun getHintText(view: LimitNumEditText) = view.hintText

    @JvmStatic
    @BindingAdapter(ATTR_LMT_HINT_TEXT)
    fun setHintText(view: LimitNumEditText, text: String?) {
        view.editText?.hint=text

    }

    @JvmStatic
    @BindingAdapter("${ATTR_LMT_TEXT}AttrChanged")
    fun setContentTextChangeListener(
        view: LimitNumEditText,
        bindListener: InverseBindingListener?
    ) {
        view.onTextChangeListener = bindListener?.let { listener ->
            {
                listener.onChange()
            }
        }
    }


}