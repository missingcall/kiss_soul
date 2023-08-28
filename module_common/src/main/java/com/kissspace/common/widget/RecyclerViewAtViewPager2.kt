package com.kissspace.common.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

class RecyclerViewAtViewPager2@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : RecyclerView(context, attrs, defStyle) {

    private var startX = 0f
    private var startY = 0f
    private var isHorizontalScrolling = false

    override fun onInterceptTouchEvent(e: MotionEvent): Boolean {
        when (e.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = e.x
                startY = e.y
                isHorizontalScrolling = false
            }
            MotionEvent.ACTION_MOVE -> {
                val deltaX = abs(e.x - startX)
                val deltaY = abs(e.y - startY)
                if (!isHorizontalScrolling && deltaX > deltaY) {
                    isHorizontalScrolling = true
                    parent?.requestDisallowInterceptTouchEvent(true)
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                parent?.requestDisallowInterceptTouchEvent(false)
            }
        }
        return super.onInterceptTouchEvent(e)
    }
}