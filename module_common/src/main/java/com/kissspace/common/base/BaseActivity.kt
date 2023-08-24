package com.kissspace.common.base

import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.ViewGroup.MarginLayoutParams
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.hjq.bar.TitleBar
import com.netease.nimlib.sdk.NIMClient
import com.noober.background.BackgroundLibrary
import com.kissspace.common.callback.ActivityTouchEvent
import com.kissspace.common.model.custommessage.GlobalNotificationMessage
import com.kissspace.util.immersiveStatusBar
import com.kissspace.util.setStatusBarColor
import com.kissspace.util.statusBarHeight

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/11/2 15:09
 * @Description: activity 基类
 *
 */
abstract class BaseActivity(layoutId: Int) : AppCompatActivity(layoutId) {
    private val mActTouchEvents = mutableListOf<ActivityTouchEvent>()
    private val onBackCallBack = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            handleBackPressed()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        BackgroundLibrary.inject(this)
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        onBackPressedDispatcher.addCallback(onBackCallBack)
        setStatusBarColor(Color.TRANSPARENT, false)
        initView(savedInstanceState)
        createDataObserver()
    }


    /**
     * 初始化UI
     */
    abstract fun initView(savedInstanceState: Bundle?)

    /**
     *  监听数据变化
     */
    open fun createDataObserver() {

    }


    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        mActTouchEvents.forEach {
            it.onDispatchTouchEvent(ev!!)
        }
        return super.dispatchTouchEvent(ev)
    }

    open fun registerActivityTouchEvent(activityTouchEvent: ActivityTouchEvent) {
        mActTouchEvents.add(activityTouchEvent)
    }

    open fun unRegisterActivityTouchEvent(activityTouchEvent: ActivityTouchEvent) {
        mActTouchEvents.remove(activityTouchEvent)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        mActTouchEvents.forEach {
            it.onTouchEvent(event!!)
        }
        return super.onTouchEvent(event)
    }

    open fun handleBackPressed() {
        finish()
    }

}