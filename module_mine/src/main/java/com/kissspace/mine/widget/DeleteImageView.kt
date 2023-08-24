package com.kissspace.mine.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.kissspace.mine.widget.DeleteDrawable
import android.view.View.OnTouchListener
import android.view.MotionEvent
import android.view.View
import com.blankj.utilcode.util.ConvertUtils
import com.kissspace.mine.widget.DeleteImageView
import com.kissspace.mine.widget.DeleteImageView.OnDeleteClickListener

class DeleteImageView : AppCompatImageView {
    private var deleteDrawable: DeleteDrawable? = null

     var mImageWith:Int=0

    constructor(context: Context,imageWith:Int) : super(context) {
        this.mImageWith=imageWith
        init(context)

    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context)
    }

    private fun init(context: Context) {
        deleteDrawable = DeleteDrawable(context,mImageWith)
        deleteDrawable?.callback = this
        setOnTouchListener(object : OnTouchListener {
            var x = 0
            var y = 0
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        x = event.x.toInt()
                        y = event.y.toInt()
                        return touchRectDelegate.contains(x, y)
                    }
                    MotionEvent.ACTION_UP -> if (touchRectDelegate.contains(x, y)) {
                        if (onDeleteClickListener != null) {
                            onDeleteClickListener?.onDelete()
                        }
                        //重新绘制
                        //invalidate();
                        return true
                    }
                }
                return false
            }
        })
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        checkAndInitCheckDrawBounds()
        //
        deleteDrawable!!.bounds = checkBounds //返回drawable的约束矩形
        //        checkDrawable.setSelected(isSelected);
        deleteDrawable!!.draw(canvas)
    }

    private fun checkAndInitCheckDrawBounds() {
        if (checkBounds.isEmpty && width > 0 || checkBounds.left < 0) {
            val left = width - CHECK_DRAWABLE_SIZE - paddingRight - CHECK_DRAWABLE_MARGIN
            val top = paddingTop + CHECK_DRAWABLE_MARGIN
            val right = width - CHECK_DRAWABLE_MARGIN
            val bottom = paddingBottom + CHECK_DRAWABLE_SIZE
            checkBounds= Rect(left, top, right,bottom)  //设置位置
            touchRectDelegate.set(checkBounds)
            //扩展点击范围
            touchRectDelegate.inset(-30, -30)
        }
    }

    var onDeleteClickListener: OnDeleteClickListener? = null

    interface OnDeleteClickListener {
        fun onDelete()
    }

    companion object {
        private const val TAG = "CheckImageView"
        private val CHECK_DRAWABLE_SIZE = ConvertUtils.dp2px(21f)
        private val CHECK_DRAWABLE_MARGIN = ConvertUtils.dp2px(3.5f)

        //对于每一个imageview来说，小勾勾的位置都是固定的，所以直接给静态，不用new太多对象
        private var checkBounds = Rect()
        private val touchRectDelegate = Rect()
    }
}