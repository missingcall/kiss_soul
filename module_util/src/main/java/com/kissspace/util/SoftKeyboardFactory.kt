package com.kissspace.util

import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.widget.PopupWindow
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.blankj.utilcode.util.ConvertUtils.dp2px

class SoftKeyboardFactory constructor(private val activity: AppCompatActivity) :
    ViewTreeObserver.OnGlobalLayoutListener, DefaultLifecycleObserver {

    private val activityRootView
        get() = activity.window.decorView

    private var lastKeyboardHeight: Int = 0

    private val isKeyboardVisible
        get() = lastKeyboardHeight >= dp2px(100f)

    private val keyboardChangedListener =
        arrayListOf<(visible: Boolean, height: Int) -> Unit>()

    private val showTask = Runnable {
        if (!popupWindow.isShowing && activityRootView.windowToken != null) {
            popupWindow.showAtLocation(activityRootView, Gravity.NO_GRAVITY, 0, 0)
        }

    }

    /**
     * this is used for get keyboard height
     */
    private val popupWindow: PopupWindow = PopupWindow(activity)
    private val windowContentView = View(activity)

    init {
        popupWindow.apply {
            contentView = windowContentView
            setBackgroundDrawable(ColorDrawable(0))
            width = 0
            height = WindowManager.LayoutParams.MATCH_PARENT
            softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
            inputMethodMode = PopupWindow.INPUT_METHOD_NEEDED
        }
        activity.lifecycle.addObserver(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        windowContentView.viewTreeObserver.addOnGlobalLayoutListener(this)
        activityRootView.post(showTask)
    }

    override fun onStop(owner: LifecycleOwner) {
        activityRootView.removeCallbacks(showTask)
        popupWindow.dismiss()
        windowContentView.viewTreeObserver.removeOnGlobalLayoutListener(this)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        keyboardChangedListener.clear()
    }

    fun addOnKeyboardChangedListener(listener: (visible: Boolean, height: Int) -> Unit) {
        keyboardChangedListener.add(listener)
    }

    fun removeOnKeyboardChangedListener(listener: (visible: Boolean, height: Int) -> Unit) {
        keyboardChangedListener.remove(listener)
    }

    override fun onGlobalLayout() {
        val windowRect = Rect()
        windowContentView.getWindowVisibleDisplayFrame(windowRect)
        val activityRect = Rect()
        activityRootView.getWindowVisibleDisplayFrame(activityRect)
        val activityHeight = activityRect.height()
        val windowHeight = windowRect.height()
        val keyboardHeight = kotlin.math.abs(activityHeight - windowHeight)
        if (keyboardHeight != lastKeyboardHeight) {
            lastKeyboardHeight = keyboardHeight
            val keyboardVisible = isKeyboardVisible
            keyboardChangedListener.forEach {
                it.invoke(keyboardVisible, keyboardHeight)
            }
        }
    }
}
