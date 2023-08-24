package com.kissspace.room.util

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.blankj.utilcode.util.LogUtils
import com.opensource.svgaplayer.*
import com.tencent.qgame.animplayer.AnimConfig
import com.tencent.qgame.animplayer.AnimView
import com.tencent.qgame.animplayer.inter.IAnimListener
import com.kissspace.common.model.immessage.UserEnterMessage
import com.kissspace.common.util.getMP4Path
import com.kissspace.room.widget.UserEnterRoomView
import com.kissspace.util.runOnUi
import java.io.File
import java.util.concurrent.ConcurrentLinkedQueue

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/12/22 10:06
 * @Description: 坐骑动效任务队列管理
 *
 */
class CarAnimationTaskQueue(
    private val animView: AnimView,
    private val enterRoomView: UserEnterRoomView,
) : DefaultLifecycleObserver {
    private var isFinished = true
    private val carQueue = ConcurrentLinkedQueue<String>()
    private val userEnterQueue = ConcurrentLinkedQueue<UserEnterMessage>()

    init {
        animView.setAnimListener(object : IAnimListener {
            override fun onFailed(errorType: Int, errorMsg: String?) {
            }

            override fun onVideoComplete() {
                isFinished = true
                playAnimation()
            }

            override fun onVideoDestroy() {
            }

            override fun onVideoRender(frameIndex: Int, config: AnimConfig?) {
            }

            override fun onVideoStart() {
                isFinished = false
            }

        })
    }


    fun addTask(userEnterMessage: UserEnterMessage) {
        carQueue.offer(userEnterMessage.carPath)
        userEnterQueue.offer(userEnterMessage)
        playAnimation()

    }

    private fun playAnimation() {
        if (isFinished && carQueue.isNotEmpty()) {
            userEnterQueue.poll()?.let {
                runOnUi {
                    enterRoomView.initData(it)
                    enterRoomView.startAnimation()
                }
            }
            carQueue.poll()?.let {
                getMP4Path(it) { path ->
                    animView.startPlay(File(path))
                }
            }
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        animView.stopPlay()
    }
}