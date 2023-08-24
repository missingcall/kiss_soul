package com.kissspace.room.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.drake.brv.utils.addModels
import com.drake.brv.utils.linear
import com.drake.brv.utils.mutable
import com.drake.brv.utils.setup
import com.drake.net.time.Interval
import com.kissspace.common.model.RoomPKInfoMessage
import com.kissspace.common.model.TeamPKUserInfo
import com.kissspace.common.util.formatComfortTime2
import com.kissspace.common.util.getMutable
import com.kissspace.common.util.parse2CountDown
import com.kissspace.module_room.R
import com.kissspace.module_room.databinding.RoomLayoutPkInfoBinding
import com.kissspace.util.loadImageCircle
import com.kissspace.util.logE
import java.util.concurrent.TimeUnit

/**
 *
 * @Author: nicko
 * @CreateDate: 2023/5/11 15:58
 * @Description: 房间PK信息view
 *
 */

class RoomPKInfoView : ConstraintLayout {
    private val mBinding = RoomLayoutPkInfoBinding.inflate(LayoutInflater.from(context), this, true)
    private val mLeftData = mutableListOf<TeamPKUserInfo>()
    private val mRightData = mutableListOf<TeamPKUserInfo>()
    private var mInterval: Interval? = null

    constructor(context: Context) : super(context, null)
    constructor(context: Context, attributeSet: AttributeSet?) : super(context, attributeSet)

    init {
        mLeftData.add(TeamPKUserInfo("", 0.0))
        mLeftData.add(TeamPKUserInfo("", 0.0))
        mLeftData.add(TeamPKUserInfo("", 0.0))
        mRightData.addAll(mLeftData)
        initRecyclerView(mBinding.recyclerPkRankBlue, true)
        initRecyclerView(mBinding.recyclerPkRankRed, false)

    }

    private fun initRecyclerView(recyclerView: RecyclerView, isBlue: Boolean) {
        recyclerView.linear(LinearLayout.HORIZONTAL).setup {
            addType<TeamPKUserInfo> {
                if (profilePath.isNullOrEmpty()) {
                    if (isBlue) {
                        R.layout.room_layout_pk_rank_user_item_left_empty
                    } else {
                        R.layout.room_layout_pk_rank_user_item_right_empty
                    }

                } else {
                    if (isBlue) {
                        R.layout.room_layout_pk_rank_user_item_left_normal
                    } else {
                        R.layout.room_layout_pk_rank_user_item_right_normal
                    }
                }
            }
            onBind {
                val rankIndex = findView<ImageView>(R.id.iv_position)
                val model = getModel<TeamPKUserInfo>()
                val img = when (modelPosition) {
                    0 -> if (isBlue) R.mipmap.room_icon_pk_rank_index_third_blue else R.mipmap.room_icon_pk_rank_index_first_red
                    1 -> if (isBlue) R.mipmap.room_icon_pk_rank_index_second_blue else R.mipmap.room_icon_pk_rank_index_second_red
                    else -> if (isBlue) R.mipmap.room_icon_pk_rank_index_first_blue else R.mipmap.room_icon_pk_rank_index_third_red
                }
                rankIndex.setImageResource(img)

                if (itemViewType == R.layout.room_layout_pk_rank_user_item_left_normal || itemViewType == R.layout.room_layout_pk_rank_user_item_right_normal) {
                    val background = findView<FrameLayout>(R.id.flt_avatar)
                    val img =
                        if (isBlue) if (modelPosition == 2) R.mipmap.room_bg_pk_rank_user_first else R.mipmap.room_pk_rank_user_second_blue else if (modelPosition == 0) R.mipmap.room_bg_pk_rank_user_first else R.mipmap.room_pk_rank_user_second_red
                    background.setBackgroundResource(img)

                    val avatar = findView<ImageView>(R.id.iv_avatar)
                    avatar.loadImageCircle(model.profilePath)
                }
            }
        }.models = if (isBlue) mLeftData else mRightData
    }

    fun startCountDown(countDownTime: Long, lifecycleOwner: LifecycleOwner) {
        if (mInterval == null) {
            val total = (countDownTime - System.currentTimeMillis()) / 1000
            mInterval = Interval(0, 1, TimeUnit.SECONDS, total).life(lifecycleOwner)
        }
        mInterval?.subscribe {
            mBinding.tvPkInfightingCountdown.text = (it * 1000).parse2CountDown()
        }?.start()
    }

    fun updatePKInfo(data: RoomPKInfoMessage) {
        mBinding.roomPkProgress.setProgress(
            data.blueTeamDto.totalPkValue, data.redTeamDto.totalPkValue
        )
        updateRecyclerView(
            mBinding.recyclerPkRankBlue, data.blueTeamDto.userList.ifEmpty { mutableListOf() }, true
        )
        updateRecyclerView(
            mBinding.recyclerPkRankRed, data.redTeamDto.userList.ifEmpty { mutableListOf() }, false
        )

    }

    private fun updateRecyclerView(
        recyclerView: RecyclerView, data: List<TeamPKUserInfo>, isBlueTeam: Boolean
    ) {

        val newList = mutableListOf<TeamPKUserInfo>()
        newList.addAll(data)
        if (newList.size < 3) {
            for (i in 0 until 3 - newList.size) {
                newList.add(TeamPKUserInfo("", 0.0))
            }
        }
        if (isBlueTeam) {
            newList.sortBy { it.boostValue }
        } else {
            newList.sortByDescending { it.boostValue }
        }
        recyclerView.mutable.clear()
        recyclerView.addModels(newList)
    }

    fun pkFinish() {

    }
}