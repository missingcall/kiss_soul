package com.kissspace.room.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.kissspace.common.model.immessage.CreateBetMessage
import com.kissspace.common.util.countDown
import com.kissspace.common.util.parse2CountDown
import com.kissspace.module_room.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/12/8
 * @Description: 坐骑用户进场消息动画view
 *
 */
class RoomPredictionView : LinearLayout {
    private var title: TextView
    private var countDown: TextView
    private var progressbar: PredictionProgressView
    private var currentPredictionId: String = ""
    private var leftProgress: Int = 0
    private var rightProgress: Int = 0
    private var job: Job? = null

    constructor(context: Context) : super(context, null) {

    }

    constructor(context: Context, attributeSet: AttributeSet?) : super(context, attributeSet) {

    }

    init {
        View.inflate(context, R.layout.room_view_prediction, this)
        title = findViewById(R.id.tv_title)
        countDown = findViewById(R.id.tv_count_down)
        progressbar = findViewById(R.id.progress)

    }

    fun initData(message: CreateBetMessage, scope: CoroutineScope) {
        currentPredictionId = message.integralGuessId
        title.text = message.integralGuessTitle
        var time = message.validTime - System.currentTimeMillis()
        job?.cancel()
        job = countDown(Long.MAX_VALUE, reverse = true, scope = scope, onTick = {
            if (time <= 0) {
                leftProgress = 0
                rightProgress = 0
                visibility = View.INVISIBLE
            } else {
                time -= 1000
            }
            countDown.text = time.parse2CountDown()
        })
        updateProgress(message.integralGuessId, message.leftProgress, message.rightProgress)
    }

    fun updateProgress(betId: String, left: Int, right: Int) {
        if (betId != currentPredictionId) {
            return
        }
        leftProgress += left
        rightProgress += right
        when {
            leftProgress == 0 && rightProgress == 0 -> {
                progressbar.postDelayed({
                    progressbar.setProgress(0.5f, 0.5f, true)
                }, 100)
            }

            leftProgress > 0 && rightProgress == 0 -> {
                progressbar.postDelayed({
                    progressbar.showOnlyLeftProgress()
                }, 100)
            }

            leftProgress == 0 && rightProgress > 0 -> {
                progressbar.postDelayed({
                    progressbar.showOnlyRightProgress()
                }, 100)
            }

            else -> {
                val total = leftProgress + rightProgress
                progressbar.postDelayed({
                    progressbar.setProgress(
                        leftProgress / total.toFloat(), rightProgress / total.toFloat(), true
                    )
                }, 100)
            }
        }
    }

    fun finishBet(id: String) {
        if (currentPredictionId == id) {
            job?.cancel()
            currentPredictionId = ""
            visibility = View.INVISIBLE
        }
    }

}