package com.kissspace.room.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import com.blankj.utilcode.util.LogUtils
import com.kissspace.util.withStyledAttributes
import com.kissspace.module_room.R

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/11/4
 * @Description: main activity
 *
 */
class PredictionProgressBar : View {
    private val mPaint = Paint()
    private val mPath = Path()
    private var mWidth = 0f
    private var mHeight = 0f
    private var mRadius = 0f
    private var mStartColor = Color.WHITE
    private var mEndColor = Color.WHITE
    private var mGradient: LinearGradient? = null
    private var mType = 0

    constructor(context: Context) : super(context)

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        withStyledAttributes(attributeSet, R.styleable.PredictionProgressView) {
            mStartColor =
                getColor(R.styleable.PredictionProgressView_gradient_startColor, Color.WHITE)
            mEndColor = getColor(R.styleable.PredictionProgressView_gradient_endColor, Color.WHITE)

            mType = getInt(R.styleable.PredictionProgressView_progress_type, 0)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mWidth = measuredWidth.toFloat()
        mHeight = measuredHeight.toFloat()
        mRadius = mHeight / 2
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (mGradient == null) {
            mGradient = LinearGradient(
                0f, 0f, mWidth, mHeight, mStartColor, mEndColor, Shader.TileMode.CLAMP
            )
        }
        mPaint.shader = mGradient
        if (mType == 0) {
            mPath.moveTo(0f, 0f)
            mPath.arcTo(0f, 0f, 2 * mRadius, 2 * mRadius, 180f, 90f, false)
            mPath.lineTo(mWidth, 0f)
            mPath.lineTo(mWidth - 50f, mHeight)
            mPath.arcTo(0f, height - 2 * mRadius, 2 * mRadius, mHeight, 90f, 90f, false)
            mPath.lineTo(0f, 0f)
        } else {
            mPath.moveTo(mWidth, 0f)
            mPath.arcTo(
                mWidth - 2 * mRadius,
                0f,
                mWidth,
                mRadius * 2,
                -90f,
                90f,
                false
            )
            mPath.lineTo(mWidth, mHeight)
            mPath.arcTo(
                mWidth - 2 * mRadius,
                mHeight - 2 * mRadius,
                mWidth,
                mHeight,
                0f,
                90f,
                false
            )
            mPath.lineTo(mWidth - mWidth, mHeight)
            mPath.lineTo((mWidth - mWidth) + 10, 0f)
        }
        mPath.close()
        canvas?.drawPath(mPath, mPaint)
    }


//    fun setProgress(progress: Float) {
//        val animator = ValueAnimator.ofFloat(mProgress, progress)
//        animator.duration = 1000
//        animator.addUpdateListener {
//            mProgress = it.animatedValue.toString().toFloat()
//            invalidate()
//        }
//        animator.start()
//    }



}