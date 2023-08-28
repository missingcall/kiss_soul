package com.kissspace.common.ext

import android.app.Activity
import android.content.Context
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.viewpager2.widget.ViewPager2
import cc.shinichi.library.ImagePreview
import com.angcyo.tablayout.DslTabLayout
import com.angcyo.tablayout.delegate2.ViewPager2Delegate
import com.drake.brv.utils.bindingAdapter
import com.hjq.bar.OnTitleBarListener
import com.hjq.bar.TitleBar
import com.kissspace.util.getDrawable
import com.kissspace.util.statusBarHeight
import com.qmuiteam.qmui.widget.QMUIRadiusImageView2

object ViewClickDelay {
    var lastClickTime: Long = 0
    var SPACE_TIME: Long = 1000
}

//点击防抖
fun View.safeClick(action: () -> Unit) {
    this.setOnClickListener {
        val currentTime = System.currentTimeMillis()
        if (currentTime - ViewClickDelay.lastClickTime > ViewClickDelay.SPACE_TIME) {
            ViewClickDelay.lastClickTime = System.currentTimeMillis()
            action()
        }
    }
}


fun RecyclerView.scrollToBottom() {
    val models = this.bindingAdapter.models
    this.scrollToPosition(models?.size?.minus(1) ?: 0)
}


fun TextView.setDrawable(resourceId: Int, gravity: Int) {
    val drawable = getDrawable(resourceId)
    drawable.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
    when (gravity) {
        Gravity.TOP -> this.setCompoundDrawables(null, drawable, null, null)
        Gravity.START -> this.setCompoundDrawables(drawable, null, null, null)
        Gravity.END -> this.setCompoundDrawables(null, null, drawable, null)
        Gravity.BOTTOM -> this.setCompoundDrawables(null, null, null, drawable)
    }
}

fun View.setMarginStatusBar() {
    val lp = this.layoutParams as ViewGroup.MarginLayoutParams
    lp.setMargins(0, statusBarHeight, 0, 0)
    this.layoutParams = lp
}

fun TitleBar.setTitleBarListener(
    onLeftClick: (() -> Unit)? = null,
    onRightClick: (() -> Unit)? = null
) {
    this.setOnTitleBarListener(object : OnTitleBarListener {
        override fun onLeftClick(titleBar: TitleBar?) {
            onLeftClick?.invoke()
        }

        override fun onRightClick(titleBar: TitleBar?) {
            onRightClick?.invoke()
        }
    })
}

fun Activity.setTitleBarListener(
    titleBar: TitleBar,
    leftClick: (() -> Unit) = {},
    rightClick: (() -> Unit) = {}
) {
    titleBar.setOnTitleBarListener(object : OnTitleBarListener {
        override fun onLeftClick(titleBar: TitleBar?) {
            super.onLeftClick(titleBar)
            leftClick.invoke()
            finish()
        }

        override fun onRightClick(titleBar: TitleBar?) {
            super.onRightClick(titleBar)
            rightClick.invoke()
        }
    })
}


fun RecyclerView.isScroll2End(): Boolean {
    if (this.layoutManager is LinearLayoutManager) {
        val linearLayoutManager = this.layoutManager as LinearLayoutManager
        val firstItem = linearLayoutManager.findFirstVisibleItemPosition()
        return this.getChildAt(0).y == 0f && firstItem == 0
    } else if (this.layoutManager is StaggeredGridLayoutManager) {
        val aa =
            (this.layoutManager as StaggeredGridLayoutManager).findFirstVisibleItemPositions(null)
        return this.getChildAt(0).y == 0f && aa[0] == 0
    }
    return false
}

fun DslTabLayout.setUpViewPager2(viewPager2: ViewPager2, onPageChanged: (Int) -> Unit) {
    ViewPager2Delegate.install(viewPager2, this)
    this.configTabLayoutConfig {
        onSelectItemView = { _, index, selected, _ ->
            if (selected) {
                onPageChanged(index)
            }
            false
        }
    }
}


fun EditText.showSoftInput() {
    isFocusable = true
    isFocusableInTouchMode = true
    requestFocus()
    if (isSystemInsetsAnimationSupport()) {
        ViewCompat.getWindowInsetsController(this)?.show(WindowInsetsCompat.Type.ime())
    } else {
        postDelayed({
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(this, 0)
        }, 300)
    }
}

/** 判断系统是否支持[WindowInsetsAnimationCompat] */
internal fun View.isSystemInsetsAnimationSupport(): Boolean {
    val windowInsetsController = ViewCompat.getWindowInsetsController(this)
    return !(windowInsetsController == null || windowInsetsController.systemBarsBehavior == 0)
}

fun previewPicture(
    activity: Activity,
    modelPosition: Int,
    target: QMUIRadiusImageView2?,
    imageList: MutableList<String>
) {
    ImagePreview.instance
        .setContext(activity)
        .setIndex(modelPosition)
        .setImageList(imageList)
        .setTransitionView(target)
        .setTransitionShareElementName("shared_element_container")
        .setEnableUpDragClose(true)
        .setEnableDragClose(true)
        .setEnableDragCloseIgnoreScale(true)
        .setShowDownButton(false)
        .setShowIndicator(false)
        .start()
}




