package com.kissspace.common.widget

import android.content.Context
import android.util.AttributeSet
import com.kissspace.util.withStyledAttributes
import com.kissspace.module_common.R
import org.libpag.PAGImageView
import org.libpag.PAGView

/**
 *
 * @Author: nicko
 * @CreateDate: 2023/3/17 14:25
 * @Description: 封装PAGView，提供自动销毁方法
 *
 */

class EasyPagView : PAGView {
    private var clearAfterStop = true
    private var loopCount = 1

    constructor(context: Context, attributeSet: AttributeSet?) : super(context, attributeSet) {
        withStyledAttributes(attributeSet, R.styleable.EasyPagView) {
            clearAfterStop = getBoolean(R.styleable.EasyPagView_clearAfterStop, true)
            loopCount = getInteger(R.styleable.EasyPagView_loop_count, 1)
        }
        initView()
    }

    fun setLoopCount(count: Int) {
        setRepeatCount(count)
    }

    private fun initView() {
        setRepeatCount(loopCount)
        addListener(object : PAGViewListener {
            override fun onAnimationStart(p0: PAGView?) {

            }

            override fun onAnimationEnd(p0: PAGView?) {
                if (clearAfterStop) {
                    composition = null
                    flush()
                }
            }

            override fun onAnimationCancel(p0: PAGView?) {
            }

            override fun onAnimationRepeat(p0: PAGView?) {
            }

            override fun onAnimationUpdate(p0: PAGView?) {
            }

        })
    }

    fun play(url: String) {
        path = url
        play()
    }

    fun clear() {
        composition = null
        flush()
    }
}