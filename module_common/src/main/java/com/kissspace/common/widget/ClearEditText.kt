package com.kissspace.common.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.res.ResourcesCompat
import com.blankj.utilcode.util.ConvertUtils
import com.kissspace.module_common.R

/**
 * Created by cretin on 2017/8/11.
 */
class ClearEditText : AppCompatEditText {
    private var mLeftDrawable: Drawable? = null
    private var mClearDrawable: Drawable? = null

    private var mWidth = 0
    private var mHeight = 0

    var boolShowCancelButton: Boolean = true

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    ) {
        init(context)
    }


    private fun init(context: Context) {
        //实例化右边的清除图片 如果要投入使用最好不要写死，需要后续封装，通过自定义属性设置
        mLeftDrawable = ResourcesCompat.getDrawable(resources, R.mipmap.common_icon_search, null)
        mClearDrawable =
            ResourcesCompat.getDrawable(resources, R.mipmap.common_icon_search_delete, null)
        addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable) {
                if (boolShowCancelButton) {
                    setDrawable()
                } else {
                    setLeftDrawable()
                }
            }
        })
    }

    //设置删除图片
    private fun setDrawable() {
        if (length() < 1) {
            setLeftDrawable()
        } else {
            mClearDrawable!!.setBounds( 0, 0, ConvertUtils.dp2px(16f), ConvertUtils.dp2px(16f))
            setCompoundDrawables(mLeftDrawable, null, mClearDrawable, null)
        }
    }

    private fun setLeftDrawable() {
        mLeftDrawable!!.setBounds(0, 0, ConvertUtils.dp2px(25f), ConvertUtils.dp2px(26f))
        setCompoundDrawables(
            mLeftDrawable, null, null, null
        )
    }

    // 处理删除事件
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            if (event.x > (width - paddingRight - mClearDrawable!!.intrinsicWidth)) {
                setText("")
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = 100
        val desiredHeight = 100
        val widthMode: Int = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize: Int = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode: Int = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize: Int = MeasureSpec.getSize(heightMeasureSpec)

        //Measure Width
        val width: Int = if (widthMode == MeasureSpec.EXACTLY) {
            //Must be this size
            widthSize
        } else if (widthMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            desiredWidth.coerceAtMost(widthSize)
        } else {
            //Be whatever you want
            desiredWidth
        }

        //Measure Height
        val height: Int = if (heightMode == MeasureSpec.EXACTLY) {
            //Must be this size
            heightSize
        } else if (heightMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            desiredHeight.coerceAtMost(heightSize)
        } else {
            //Be whatever you want
            desiredHeight
        }
        mWidth = width
        mHeight = height
        //MUST CALL THIS
        setPadding(
            ConvertUtils.dp2px(12f),
            ConvertUtils.dp2px(0f),
            ConvertUtils.dp2px(12f),
            ConvertUtils.dp2px(0f)
        )
//        如果要投入使用最好不要写死，需要后续封装，通过自定义属性设置
        if (boolShowCancelButton) {
            setDrawable()
        } else {
            setLeftDrawable()
        }
        setMeasuredDimension(width, height)
    }
}