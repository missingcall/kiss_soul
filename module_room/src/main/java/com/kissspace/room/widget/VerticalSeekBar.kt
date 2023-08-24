package com.kissspace.room.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent

/**
 *@author: adan
 *@date: 2023/3/16
 *@Description: 垂直的seekbar
 */
class VerticalSeekBar : androidx.appcompat.widget.AppCompatSeekBar{
    private var mThumb: Drawable? = null
    private var mOnSeekBarChangeListener: OnSeekBarChangeListener? = null

    constructor(context: Context):super(context)
    constructor(context: Context, attrs: AttributeSet):super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, style: Int):super(context, attrs, style)

    override fun setOnSeekBarChangeListener(l: OnSeekBarChangeListener?) {
        mOnSeekBarChangeListener = l
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(h, w, oldh, oldw)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(heightMeasureSpec, widthMeasureSpec)
        setMeasuredDimension(measuredHeight, measuredWidth)
    }

    override fun onDraw(c: Canvas) {
        c.rotate(-90f)
        c.translate(-height.toFloat(), 0f)
        super.onDraw(c)
    }

    private fun onStopTrackingTouch(){
        if (mOnSeekBarChangeListener != null) {
            mOnSeekBarChangeListener?.onStopTrackingTouch(this);
        }
    }

    private fun setThumbPos(w: Int, thumb: Drawable, scale: Float, gap: Int) {
        val available = w - paddingLeft - paddingRight
        val thumbWidth = thumb.intrinsicWidth
        val thumbHeight = thumb.intrinsicHeight
        val thumbPos = (scale * available + 0.5f).toInt()
        val topBound: Int
        val bottomBound: Int
        if (gap == Int.MIN_VALUE) {
            val oldBounds: Rect = thumb.bounds
            topBound = oldBounds.top
            bottomBound = oldBounds.bottom
        } else {
            topBound = gap
            bottomBound = gap + thumbHeight
        }
        thumb.setBounds(thumbPos, topBound, thumbPos + thumbWidth, bottomBound)
    }

    override fun setThumb(thumb: Drawable?) {
        mThumb = thumb
        super.setThumb(thumb)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) {
            return false
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE-> {
                //progress =  max - (max * event.y / height).toInt()
                //将浮点数 强制类型转换为整数  是只取整数部分
                //正数+0.5，负数-0.5，然后取整即可得到四舍五入
                //实现滑条拖动半拉可以选择
                progress =  max - ((max * event.y / height)+0.5).toInt()

                onSizeChanged(width, height, 0, 0)
            }
            MotionEvent.ACTION_CANCEL ,MotionEvent.ACTION_UP-> {
                onStopTrackingTouch()
            }
        }
        return true
    }
}