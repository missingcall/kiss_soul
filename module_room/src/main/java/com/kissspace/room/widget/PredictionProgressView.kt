package com.kissspace.room.widget

import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.kissspace.module_room.R

class PredictionProgressView : ConstraintLayout {
    private var mWidth = 0
    private var mLeftProgress: View
    private var mRightProgress: View
    private var mLeftWidth = 0
    private var mRightWith = 0

    constructor(context: Context) : super(context, null) {
    }

    constructor(context: Context, attributeSet: AttributeSet?) : super(context, attributeSet) {

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mWidth = measuredWidth
    }

    init {
        View.inflate(context, R.layout.room_layout_prediction_progress, this)
        mLeftProgress = findViewById(R.id.progress_left)
        mRightProgress = findViewById(R.id.progress_right)
    }

    fun setProgress(leftRatio: Float, rightRatio: Float, isSmall: Boolean = false) {
        mLeftProgress.visibility = View.VISIBLE
        mRightProgress.visibility = View.VISIBLE
        if (isSmall){
            mLeftProgress.setBackgroundResource(R.mipmap.room_icon_pk_progress_left)
            mRightProgress.setBackgroundResource(R.mipmap.room_icon_pk_progress_right)
        }else{
            mLeftProgress.setBackgroundResource(R.mipmap.room_bg_prediction_progress_left)
            mRightProgress.setBackgroundResource(R.mipmap.room_bg_prediction_progress_right)
        }
        val lpLeft = mLeftProgress.layoutParams
        val lpRight = mRightProgress.layoutParams
        val animatorLeft = ObjectAnimator.ofInt(mLeftWidth, (mWidth * leftRatio).toInt())
        animatorLeft.duration = 500
        animatorLeft.addUpdateListener {
            lpLeft.width = it.animatedValue.toString().toInt()
            mLeftWidth = it.animatedValue.toString().toInt()
            mLeftProgress.layoutParams = lpLeft
        }
        animatorLeft.start()

        val animatorRight = ObjectAnimator.ofInt(mRightWith, (mWidth * rightRatio).toInt())
        animatorRight.duration = 500
        animatorRight.addUpdateListener {
            lpRight.width = it.animatedValue.toString().toInt()
            mRightWith = it.animatedValue.toString().toInt()
            mRightProgress.layoutParams = lpRight
        }
        animatorRight.start()
    }

    fun showOnlyLeftProgress() {
        mLeftProgress.visibility = View.VISIBLE
        mRightProgress.visibility = View.GONE
        mLeftProgress.setBackgroundResource(R.drawable.room_bg_progress_left)
        val lpLeft = mLeftProgress.layoutParams
        val animatorLeft = ObjectAnimator.ofInt(mLeftWidth, mWidth)
        animatorLeft.duration = 500
        animatorLeft.addUpdateListener {
            lpLeft.width = it.animatedValue.toString().toInt()
            mLeftWidth = it.animatedValue.toString().toInt()
            mLeftProgress.layoutParams = lpLeft
        }
        animatorLeft.start()
    }

    fun showOnlyRightProgress() {
        mLeftProgress.visibility = View.GONE
        mRightProgress.visibility = View.VISIBLE
        mRightProgress.setBackgroundResource(R.drawable.room_bg_progress_right)
        val lpRight = mRightProgress.layoutParams
        val animatorLeft = ObjectAnimator.ofInt(mRightWith, mWidth)
        animatorLeft.duration = 500
        animatorLeft.addUpdateListener {
            lpRight.width = it.animatedValue.toString().toInt()
            mRightWith = it.animatedValue.toString().toInt()
            mRightProgress.layoutParams = lpRight
        }
        animatorLeft.start()
    }

}