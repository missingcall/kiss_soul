package com.kissspace.mine.widget

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import com.blankj.utilcode.util.ConvertUtils
import com.kissspace.module_mine.R
import com.kissspace.util.logE

class DeleteDrawable(private val context: Context, private val imageWidth: Int) : BitmapDrawable() {
    private val mBitmap: Bitmap =
        BitmapFactory.decodeResource(context.resources, R.mipmap.mine_icon_report_delete)

    override fun draw(canvas: Canvas) {
        drawNormal(canvas)
        super.draw(canvas)
    }

    private fun drawNormal(canvas: Canvas) {
        logE("imageWidth$imageWidth")
        canvas.drawBitmap(
            mBitmap,
            (imageWidth - mBitmap.width - ConvertUtils.dp2px(3.5f)).toFloat(),
            ConvertUtils.dp2px(3.5f).toFloat(),
            Paint()
        )
    }

    companion object {
        private const val TAG = "DeleteDrawable"
    }
}