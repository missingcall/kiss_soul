package com.kissspace.room.util

import android.animation.Keyframe
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Path
import android.graphics.PathMeasure
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.animation.addListener
import androidx.core.animation.doOnEnd
import com.kissspace.util.dp
import com.kissspace.util.loadImageCircle
import kotlin.math.abs


/**
 *
 * @Author: nicko
 * @CreateDate: 2023/2/13
 * @Description: 播放礼物轨迹动画
 *
 */
object GiftFlyAnimationUtil {

    fun playAnimation(
        context: Context,
        startView: View,
        endView: View,
        wrapper: ConstraintLayout,
        url: String,
        time: Long
    ) {


        var startX: Float
        var startY: Float
        var endX: Float
        var endY: Float

        val startLocation = intArrayOf(0, 0)
        startView.getLocationInWindow(startLocation)
        val endLocation = intArrayOf(0, 0)
        endView.getLocationInWindow(endLocation)

        startX = startLocation[0].toFloat()
        startY = startLocation[1].toFloat()
        endX = endLocation[0].toFloat()
        endY = endLocation[1].toFloat()


        // 确定起点和终点的 x 坐标和 y 坐标
        val x1: Float
        val y1: Float
        val x2: Float
        val y2: Float
        if (startX <= endX) {
            x1 = startX
            x2 = endX
        } else {
            x1 = endX
            x2 = startX
        }
        if (startY <= endY) {
            y1 = startY
            y2 = endY
        } else {
            y1 = endY
            y2 = startY
        }

        val imageView = ImageView(context)
        val imageViewParam = FrameLayout.LayoutParams(40.dp.toInt(), 40.dp.toInt())
        imageViewParam.gravity = Gravity.CENTER
        imageView.loadImageCircle(url)

        val giftWrapper = FrameLayout(context)
        val giftWrapperParam = FrameLayout.LayoutParams(42.dp.toInt(), 42.dp.toInt())
        wrapper.addView(giftWrapper, giftWrapperParam)
        giftWrapper.addView(imageView, imageViewParam)


        // 计算控制点的坐标
        val cx = (x1 + x2) / 2f
        val cy = (y1 + y2) / 2f - abs(x1 - x2) / 2f

        // 创建 Path 对象，并将其移动到起点
        val path = Path()
        path.moveTo(startX, startY)

        // 创建贝塞尔曲线
        path.quadTo(cx, cy, endX, endY)

        // 创建 ValueAnimator，并设置动画时长和插值器
        val valueAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = time
            interpolator = AccelerateInterpolator()
            addUpdateListener {
                val progress = it.animatedValue as Float
                val pathMeasure = PathMeasure(path, false)
                val pos = FloatArray(2)
                pathMeasure.getPosTan(pathMeasure.length * progress, pos, null)
                giftWrapper.x = pos[0]
                giftWrapper.y = pos[1]
            }
            doOnEnd {
                shakeView(2f, imageView,giftWrapper, wrapper)
            }
        }
        valueAnimator.start()

    }


    private fun shakeView(shakeFactor: Float, imageView: ImageView,giftWrapper:ViewGroup, wrapper: ViewGroup) {
        val pvhScaleX = PropertyValuesHolder.ofKeyframe(
            View.SCALE_X,
            Keyframe.ofFloat(0f, 1f),
            Keyframe.ofFloat(.1f, .9f),
            Keyframe.ofFloat(.2f, .9f),
            Keyframe.ofFloat(.3f, 1.1f),
            Keyframe.ofFloat(.4f, 1.1f),
            Keyframe.ofFloat(.5f, 1.1f),
            Keyframe.ofFloat(.6f, 1.1f),
            Keyframe.ofFloat(.7f, 1.1f),
            Keyframe.ofFloat(.8f, 1.1f),
            Keyframe.ofFloat(.9f, 1.1f),
            Keyframe.ofFloat(1f, 1f)
        )

        val pvhScaleY = PropertyValuesHolder.ofKeyframe(
            View.SCALE_Y,
            Keyframe.ofFloat(0f, 1f),
            Keyframe.ofFloat(.1f, .9f),
            Keyframe.ofFloat(.2f, .9f),
            Keyframe.ofFloat(.3f, 1.1f),
            Keyframe.ofFloat(.4f, 1.1f),
            Keyframe.ofFloat(.5f, 1.1f),
            Keyframe.ofFloat(.6f, 1.1f),
            Keyframe.ofFloat(.7f, 1.1f),
            Keyframe.ofFloat(.8f, 1.1f),
            Keyframe.ofFloat(.9f, 1.1f),
            Keyframe.ofFloat(1f, 1f)
        )

        val pvhRotate = PropertyValuesHolder.ofKeyframe(
            View.ROTATION,
            Keyframe.ofFloat(0f, 0f),
            Keyframe.ofFloat(.1f, -3f * shakeFactor),
            Keyframe.ofFloat(.2f, -3f * shakeFactor),
            Keyframe.ofFloat(.3f, 3f * shakeFactor),
            Keyframe.ofFloat(.4f, -3f * shakeFactor),
            Keyframe.ofFloat(.5f, 3f * shakeFactor),
            Keyframe.ofFloat(.6f, -3f * shakeFactor),
            Keyframe.ofFloat(.7f, 3f * shakeFactor),
            Keyframe.ofFloat(.8f, -3f * shakeFactor),
            Keyframe.ofFloat(.9f, 3f * shakeFactor),
            Keyframe.ofFloat(1f, 0f)
        )


        val objectAnimator = ObjectAnimator.ofPropertyValuesHolder(
            imageView, pvhScaleX, pvhScaleY, pvhRotate
        )
        objectAnimator.duration = 500
        objectAnimator.addListener(onEnd = {
            wrapper.removeView(giftWrapper)
        })
        objectAnimator.start()
    }

}
