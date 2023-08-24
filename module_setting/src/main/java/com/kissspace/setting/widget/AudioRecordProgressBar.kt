package com.kissspace.setting.widget

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.kissspace.util.resToColor
import com.kissspace.module_common.R

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/12/28 16:24
 * @Description: 声音录制view进度条
 *
 */
class AudioRecordProgressBar : View {
    private val paint = Paint()
    private val rect = Rect()
    private val rectF = RectF()
    private var size: Int = 0
    private var current: Int = 0
    private var total: Int = 100
    private var bigCircleRadius = 0f
    private var smallCircleRadius = 0f
    private val arcWidth = 8f

    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    init {
        paint.isAntiAlias = true
    }

    fun setProgress(progress: Int) {
        this.current = progress
        invalidate()
    }

    fun setTotal(total: Int) {
        this.total = total
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        size = measuredWidth
        bigCircleRadius = size.toFloat() / 2
        smallCircleRadius = bigCircleRadius - arcWidth
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = arcWidth
        paint.color = R.color.color_1AFFFFFF.resToColor()
        canvas?.drawCircle(bigCircleRadius, bigCircleRadius, smallCircleRadius, paint)
        paint.color = R.color.color_FFFD62.resToColor()
        rectF.set(arcWidth, arcWidth, size - arcWidth, size - arcWidth)
        canvas?.drawArc(rectF, 270f, current * 360 / total.toFloat(), false, paint);
    }
}