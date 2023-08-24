package com.kissspace.room.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.kissspace.module_room.databinding.RoomProgressPkBinding
import com.kissspace.util.dp
import kotlin.math.min

class RoomPKProgressView : ConstraintLayout {
    private val mBinding = RoomProgressPkBinding.inflate(LayoutInflater.from(context), this, true)
    private val mindWidth = 30.dp.toInt()

    constructor(context: Context, attributeSet: AttributeSet?) : super(context, attributeSet)

    fun setProgress(left: Long, right: Long) {
        mBinding.tvPkBlueTotal.text = left.toString()
        mBinding.tvPkRedTotal.text = right.toString()
        val ratio = left.toFloat() / (left + right).toFloat()
        val leftWidth = (measuredWidth * ratio).toInt()
        val rightWidth = measuredWidth - leftWidth

        when {
            leftWidth >= mindWidth && rightWidth >= mindWidth -> {
                val leftParams =
                    mBinding.fltPkProgressLeft.layoutParams as LayoutParams
                leftParams.width = leftWidth
                mBinding.fltPkProgressLeft.layoutParams = leftParams

                val rightParams =
                    mBinding.fltPkProgressRight.layoutParams as LayoutParams
                rightParams.width = rightWidth
                mBinding.fltPkProgressRight.layoutParams = rightParams
            }

            left < mindWidth -> {
                val leftParams =
                    mBinding.fltPkProgressLeft.layoutParams as LayoutParams
                leftParams.width = mindWidth
                mBinding.fltPkProgressLeft.layoutParams = leftParams

                val rightParams =
                    mBinding.fltPkProgressRight.layoutParams as LayoutParams
                rightParams.width = measuredWidth - mindWidth
                mBinding.fltPkProgressRight.layoutParams = rightParams
            }

            right < mindWidth -> {
                val rightParams =
                    mBinding.fltPkProgressRight.layoutParams as LayoutParams
                rightParams.width = mindWidth
                mBinding.fltPkProgressRight.layoutParams = rightParams

                val leftParams =
                    mBinding.fltPkProgressLeft.layoutParams as LayoutParams
                leftParams.width = measuredWidth - mindWidth
                mBinding.fltPkProgressLeft.layoutParams = leftParams
            }
        }

    }

}