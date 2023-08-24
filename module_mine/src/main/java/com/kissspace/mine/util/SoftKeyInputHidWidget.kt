package com.kissspace.mine.util

import android.R
import android.app.Activity
import android.graphics.Rect
import android.os.Build
import android.view.View
import android.widget.FrameLayout
import com.kissspace.mine.util.SoftKeyInputHidWidget
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import com.blankj.utilcode.util.BarUtils

/**
 * 用于解决因为沉浸式状态栏，但不想采用fitSystemWidnow属性
 * 或不想设置键盘属adjustResize/adjustPan属性，想实现键盘不遮挡输入框
 * Created by SmileXie on 2017/3/27.
 */
class SoftKeyInputHidWidget private constructor(activity: Activity) {
    private val mChildOfContent: View
    private var usableHeightPrevious = 0
    private val frameLayoutParams: FrameLayout.LayoutParams
    private var contentHeight = 0
    private var isfirst = true
    private val statusBarHeight: Int = getStatusBarHeight()

    init {
        val content = activity.findViewById<View>(R.id.content) as FrameLayout
        mChildOfContent = content.getChildAt(0)

        //界面出现变动都会调用这个监听事件
        mChildOfContent.viewTreeObserver.addOnGlobalLayoutListener {
            if (isfirst) {
                contentHeight = mChildOfContent.height //兼容华为等机型
                isfirst = false
            }
            possiblyResizeChildOfContent()
        }
        frameLayoutParams = mChildOfContent.layoutParams as FrameLayout.LayoutParams
    }

    //重新调整跟布局的高度
    private fun possiblyResizeChildOfContent() {
        val usableHeightNow = computeUsableHeight()
        //当前可见高度和上一次可见高度不一致 布局变动
        if (usableHeightNow != usableHeightPrevious) {
            val usableHeightSansKeyboard = mChildOfContent.rootView.height
            val heightDifference = usableHeightSansKeyboard - usableHeightNow
            if (heightDifference > usableHeightSansKeyboard / 4) {
                // keyboard probably just became visible
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    frameLayoutParams.height =
                        usableHeightSansKeyboard - heightDifference + statusBarHeight
                } else {
                    frameLayoutParams.height = usableHeightSansKeyboard - heightDifference
                }
            } else {
                frameLayoutParams.height = contentHeight
            }
            mChildOfContent.requestLayout()
            usableHeightPrevious = usableHeightNow
        }
    }

    /**
     * 获取改变之后界面的可用高度（可以为开发者显示内容的高度）
     * @return
     */
    private fun computeUsableHeight(): Int {
        val r = Rect()
        mChildOfContent.getWindowVisibleDisplayFrame(r) //获取到的rect就是界面除去标题栏、除去软键盘挡住部分，所剩下的域
        return r.bottom - r.top
    }

    companion object {
        fun assistActivity(activity: Activity) {
            SoftKeyInputHidWidget(activity)
        }

        fun getStatusBarHeight(): Int {
            return  BarUtils.getStatusBarHeight()
        }
    }
}