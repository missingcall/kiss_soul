package com.kissspace.room.widget

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.AnimationSet
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.animation.addListener
import com.kissspace.util.dp
import com.kissspace.common.model.immessage.UserEnterMessage
import com.kissspace.common.widget.UserLevelIconView
import com.kissspace.module_room.R
import com.kissspace.util.ellipsizeString
import com.kissspace.util.loadImageCircle
import com.kissspace.util.screenWidth
import kotlin.math.exp

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/12/8
 * @Description: 坐骑用户进场消息动画view
 *
 */
class UserEnterRoomView : LinearLayout {
    private var avatarImage: ImageView
//    private var incomeLeve: UserLevelIconView
//    private var expendLevel: UserLevelIconView
    private var nicknameText: TextView

    constructor(context: Context) : super(context, null) {

    }

    constructor(context: Context, attributeSet: AttributeSet?) : super(context, attributeSet) {

    }

    init {
        View.inflate(context, R.layout.room_view_user_enter_room, this)
        avatarImage = findViewById(R.id.user_avatar)
//        incomeLeve = findViewById(R.id.level_income)
//        expendLevel = findViewById(R.id.level_expend)
        nicknameText = findViewById(R.id.tv_nickname)

    }

    fun initData(message: UserEnterMessage) {
        avatarImage.loadImageCircle(message.profilePath)
//        incomeLeve.setLeveCount(message.charmLevel)
//        expendLevel.setLeveCount(message.consumeLevel)
        nicknameText.text = message.nickname.ellipsizeString(6)
    }

    fun startAnimation() {
        val animator1 =
            ObjectAnimator.ofFloat(this, "translationX", screenWidth.toFloat() + width, 12.dp)
                .apply {
                    duration = 1000
                    interpolator = AccelerateInterpolator()
                }

        val animator2 =
            ObjectAnimator.ofFloat(this, "translationX", 12.dp, -width.toFloat()).apply {
                duration = 300
                startDelay = 2000
                interpolator = AccelerateInterpolator()
            }

        AnimatorSet().apply {
            playSequentially(animator1, animator2)
            addListener(onStart = {
                this@UserEnterRoomView.visibility = View.VISIBLE
            }, onEnd = {
                this@UserEnterRoomView.alpha = 1f
                this@UserEnterRoomView.translationX = screenWidth.toFloat() + width
                this@UserEnterRoomView.visibility = View.INVISIBLE
            })
        }.start()

    }
}