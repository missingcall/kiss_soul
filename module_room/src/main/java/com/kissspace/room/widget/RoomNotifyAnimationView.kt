package com.kissspace.room.widget

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.text.SpannedString
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.animation.addListener
import com.blankj.utilcode.util.ScreenUtils
import com.xiaweizi.marquee.MarqueeTextView
import com.kissspace.common.util.getPagPath
import com.kissspace.common.widget.EasyPagView
import com.kissspace.module_room.R
import com.kissspace.util.dp
import com.kissspace.util.loadImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.concurrent.ConcurrentLinkedQueue

/**
 *
 * @Author: nicko
 * @CreateDate: 2023/1/12 15:31
 * @Description: 房间顶部飘条
 *
 */
class RoomNotifyAnimationView : FrameLayout {
    private val textView: MarqueeTextView
    private val iamgeView: ImageView
    private val backgroundView: EasyPagView
    private var isFinished = true
    private val queue = ConcurrentLinkedQueue<GiftMessage>()
    private var job: Job? = null


    constructor(context: Context) : super(context, null)
    constructor(context: Context, attributeSet: AttributeSet?) : super(context, attributeSet)


    init {
        View.inflate(context, R.layout.room_layout_notify_animation, this)
        textView = findViewById(R.id.tv_gift_content)
        backgroundView = findViewById(R.id.background_pag)
        iamgeView = findViewById(R.id.image_gift)
    }

    fun playAnimation(
        svga: String,
        imageUrl: String,
        span: SpannedString,
        scope: CoroutineScope,
        gameType: String
    ) {
        queue.offer(GiftMessage(svga, imageUrl, span, gameType))
        if (job == null) {
            job = flow {
                for (i in 0 until Int.MAX_VALUE) {
                    emit(i)
                    delay(1000)
                }
            }.flowOn(Dispatchers.Main).onEach {
                if (isFinished) {
                    queue.poll()?.let {
                        playPag(it)
                    }
                }
            }.launchIn(scope)
        }
    }

    private fun playPag(message: GiftMessage) {
        this.visibility = View.VISIBLE
        backgroundView.setLoopCount(Int.MAX_VALUE)
        updateInfo(message)
        getPagPath(message.svga) {
            backgroundView.play(it)
            playWrapperAnimation()
        }
    }

    private fun updateInfo(message: GiftMessage) {
        iamgeView.visibility = if (message.gameType == "001") GONE else VISIBLE
        val lp = textView.layoutParams as ConstraintLayout.LayoutParams
        when (message.gameType) {
            "001" -> {
                lp.setMargins(52.dp.toInt(), 32.dp.toInt(), 95.dp.toInt(), 0)
            }

            "004", "002" -> {
                lp.setMargins(34.dp.toInt(), 40.dp.toInt(), 34.dp.toInt(), 0)
            }

            else -> {
                lp.setMargins(34.dp.toInt(), 48.dp.toInt(), 34.dp.toInt(), 0)
            }
        }

        textView.layoutParams = lp
        textView.text = message.text
        iamgeView.loadImage(message.imageUrl)
    }

    private fun playWrapperAnimation() {
        val enter = ObjectAnimator.ofFloat(
            this, "translationX", ScreenUtils.getScreenWidth().toFloat(), 0f
        ).apply {
            duration = 800
            interpolator = LinearInterpolator()
        }

        val exit = ObjectAnimator.ofFloat(
            this, "translationX", 0f, -ScreenUtils.getScreenWidth().toFloat()
        ).apply {
            duration = 800
            interpolator = LinearInterpolator()
        }
        AnimatorSet().apply {
            play(enter)
            play(exit).after(8000)
            addListener(onStart = {
                isFinished = false
                textView.startScroll()
            }, onEnd = {
                isFinished = true
                textView.stopScroll()
                backgroundView.clear()
                this@RoomNotifyAnimationView.alpha = 1f
                this@RoomNotifyAnimationView.translationX = 0f
                this@RoomNotifyAnimationView.visibility = View.INVISIBLE
            })
        }.start()
    }

    data class GiftMessage(
        var svga: String,
        val imageUrl: String,
        var text: SpannedString,
        var gameType: String
    )
}