package com.kissspace.common.callback

import android.view.MotionEvent

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/11/2 15:13
 * @Description:
 *
 */
interface ActivityTouchEvent {

    fun onTouchEvent(event: MotionEvent)

    fun onDispatchTouchEvent(ev: MotionEvent)
}
