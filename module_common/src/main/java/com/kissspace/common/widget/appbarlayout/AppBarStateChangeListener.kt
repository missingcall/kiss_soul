package com.kissspace.common.widget.appbarlayout

import android.graphics.Color
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener
import com.netease.cloud.nos.yidun.utils.LogUtil
import com.youth.banner.util.LogUtils

/**
 * @author: adan
 * @date: 2023/3/4
 * @Description:
 */
abstract class AppBarStateChangeListener : OnOffsetChangedListener {

    private var mCurrentState = State.IDLE

    override fun onOffsetChanged(appBarLayout: AppBarLayout, i: Int) {
        mCurrentState = if (i == 0) {
            if (mCurrentState != State.EXPANDED) {
                onStateChanged(
                    appBarLayout,
                    State.EXPANDED,i
                )
            }
            State.EXPANDED
        } else if (Math.abs(i) >= appBarLayout.totalScrollRange) {
            if (mCurrentState != State.COLLAPSED) {
                onStateChanged(
                    appBarLayout,
                    State.COLLAPSED,i
                )
            }
            State.COLLAPSED
        } else {
//            if (mCurrentState != State.IDLE) {
            val offset = Math.abs(i * 1.0f)
            val scale: Float = offset / Math.abs(appBarLayout.totalScrollRange)
            val alpha = (255 * scale).toInt()
            onStateChanged(
                appBarLayout,
                State.IDLE,alpha
            )
            State.IDLE
//            if (offset % 5 == 0f){
//
//            }else{
//                mCurrentState
//            }
        }
    }

    abstract fun onStateChanged(appBarLayout: AppBarLayout, state: State,alpha:Int)

    enum class State {
        EXPANDED, COLLAPSED, IDLE
    }
}