package com.kissspace.room.widget

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.View
import android.widget.LinearLayout.HORIZONTAL
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.GsonUtils
import com.drake.brv.utils.*
import com.drake.net.reflect.TypeToken
import com.kissspace.util.toast
import com.kissspace.common.config.Constants
import com.kissspace.common.router.jump
import com.kissspace.common.ext.safeClick
import com.kissspace.common.flowbus.Event
import com.kissspace.common.flowbus.FlowBus
import com.kissspace.common.http.getReceiveTaskReward
import com.kissspace.common.model.TaskRewardModel
import com.kissspace.common.router.RouterPath
import com.kissspace.common.util.*
import com.kissspace.common.widget.BaseBottomSheetDialogFragment
import com.kissspace.module_room.R
import com.kissspace.module_room.databinding.RoomItemTaskBinding
import com.kissspace.module_room.databinding.RoomRewardListDialogBinding
import com.kissspace.network.result.collectData
import com.kissspace.common.http.getUserInfo
import com.kissspace.room.viewmodel.LiveViewModel
import kotlinx.coroutines.Job
import okhttp3.internal.filterList
import com.kissspace.util.logE
import com.kissspace.util.orZero
import java.lang.reflect.Type

class TaskRewardListDialogFragment : BaseBottomSheetDialogFragment<RoomRewardListDialogBinding>() {

    private val mViewModel by viewModels<LiveViewModel>()

    private val taskRewardList: MutableList<TaskRewardModel> = ArrayList()

    override fun getViewBinding(): RoomRewardListDialogBinding =
        RoomRewardListDialogBinding.inflate(layoutInflater)

    @SuppressLint("SetTextI18n")
    override fun initView() {
        mViewModel.getChatRoomTaskRewardList()
        refreshIntegral()
        //默认第一个是拆盲盒
        mBinding.rvTaskList.linear(HORIZONTAL).divider(R.drawable.room_divider_reward_list).setup {
            addType<TaskRewardModel> { R.layout.room_item_task }
            onBind {
                when (val mBinding = getBinding<ViewDataBinding>()) {
                    is RoomItemTaskBinding -> {
                        val model = getModel<TaskRewardModel>()
                        val giftType = getGiftTypeWithKey(model.rewardType)
                        val type: Type = object : TypeToken<Map<String, String>>() {}.type
                        val countMap: Map<String, String> =
                            GsonUtils.fromJson(model.rewardType, type)
                        val giftCount = countMap.entries.firstOrNull()?.value ?: "0"
                        //001 待解锁 002 进行中 003 待领取 004 已领取
                        when (model.finishStatus) {
                            Constants.TaskStatus.TOBE_UNLOCKED.type -> {
                                //完成条件转int
                                mBinding.clTvStatus.visibility = View.VISIBLE
                                mBinding.tvStatus.text =
                                    "${model.conditional?.toInt().orZero() / 60}分钟"
                                mBinding.tvStatus.setTextColor(resources.getColor(com.kissspace.module_common.R.color.color_80FFFFFF))
                                mBinding.tvReceiveStatus.text =
                                    Constants.TaskStatus.TOBE_UNLOCKED.value
                                mBinding.tvReceiveStatus.visibility = View.VISIBLE
                                mBinding.tvTime.visibility = View.INVISIBLE
                                mBinding.tvReward.visibility = View.GONE
                                //001积分 002 道具 003 礼包盲盒
                                if (giftType == Constants.GiftType.INTEGRAL.type) {
                                    mBinding.ivGift.setImageResource(R.mipmap.room_icon_gift_integral_alpha)
                                    mBinding.tvCount.text = giftCount
                                    mBinding.tvCount.visibility = View.VISIBLE
                                    mBinding.tvCount.setTextColor(resources.getColor(com.kissspace.module_common.R.color.color_80FFFFFF))
                                } else if (giftType == Constants.GiftType.BLIND_BOX.type) {
                                    mBinding.ivGift.setImageResource(com.kissspace.module_common.R.mipmap.common_icon_reward)
                                    mBinding.tvCount.visibility = View.GONE
                                }

                            }
                            Constants.TaskStatus.IN_PROGRESS.type -> {
                                mBinding.clTvStatus.visibility = View.VISIBLE
                                mBinding.tvStatus.text = Constants.TaskStatus.IN_PROGRESS.value
                                mBinding.tvStatus.setTextColor(Color.WHITE)
                                mBinding.tvReward.visibility = View.GONE
                                mBinding.tvTime.text = "${model.validTime.orZero() / 60}分"
                                mBinding.tvTime.visibility = View.VISIBLE
                                mBinding.tvReceiveStatus.visibility = View.GONE
                                mBinding.tvTime.setTextColor(Color.WHITE)
                                mBinding.tvCount.setTextColor(Color.WHITE)
                                mBinding.tvTime.tag = model.taskId
                                model.validTime?.let {
                                    mBinding.tvTime.text = it.formatDurationMS1
                                    val job: Job = countDown(
                                        it,
                                        reverse = false,
                                        scope = lifecycleScope,
                                        onTick = { time ->
                                            if (mBinding.tvTime.tag.equals(model.taskId)) {
                                                if (time != 0L) {
                                                    mBinding.tvTime.text = time.toHHMMSS(false)
                                                } else {
                                                    mBinding.clTvStatus.visibility = View.VISIBLE
                                                    mBinding.tvReward.visibility = View.VISIBLE
                                                    mBinding.tvTime.visibility = View.INVISIBLE
                                                    if (giftType == Constants.GiftType.INTEGRAL.type) {
                                                        mBinding.tvReward.text = "领取"
                                                        mBinding.tvReward.background = resources.getDrawable(R.mipmap.room_icon_use_integral)
                                                        mBinding.tvStatus.text =
                                                            Constants.GiftType.INTEGRAL.value
                                                    } else if (giftType == Constants.GiftType.BLIND_BOX.type) {
                                                        mBinding.tvReward.text = "拆盲盒"
                                                        mBinding.tvReward.background = resources.getDrawable(R.mipmap.room_icon_use_integral)
                                                        mBinding.tvStatus.text =
                                                            Constants.GiftType.BLIND_BOX.value
                                                    }
                                                    mBinding.ivGift.setImageResource(R.mipmap.room_icon_gift_integral)
                                                    mBinding.tvCount.text = giftCount
                                                    mBinding.tvCount.visibility = View.VISIBLE
                                                }
                                            } else {
                                                if (mBinding.tvTime.getTag(com.kissspace.module_common.R.id.countDownTimeId) != null) {
                                                    val job: Job =
                                                        mBinding.tvTime.getTag(com.kissspace.module_common.R.id.countDownTimeId) as Job
                                                    logE("jobCancel$job")
                                                    job.cancel()
                                                }
                                            }
                                        })
                                    mBinding.tvTime.setTag(
                                        com.kissspace.module_common.R.id.countDownTimeId,
                                        job
                                    )
                                }
                                if (giftType == Constants.GiftType.INTEGRAL.type) {
                                    mBinding.ivGift.setImageResource(R.mipmap.room_icon_gift_integral)
                                    mBinding.tvCount.text = giftCount
                                    mBinding.tvCount.visibility = View.VISIBLE
                                } else if (giftType == Constants.GiftType.BLIND_BOX.type) {
                                    mBinding.ivGift.setImageResource(com.kissspace.module_common.R.mipmap.common_icon_reward)
                                    mBinding.tvCount.visibility = View.GONE
                                }
                            }
                            Constants.TaskStatus.TOBE_COLLECTED.type -> {
                                mBinding.tvReward.visibility = View.VISIBLE
                                mBinding.clTvStatus.visibility = View.VISIBLE
                                mBinding.tvTime.visibility = View.INVISIBLE
                                mBinding.tvReceiveStatus.visibility = View.GONE
                                if (giftType == Constants.GiftType.INTEGRAL.type) {
                                    mBinding.tvReward.text = "领取"
                                    mBinding.tvReward.background = resources.getDrawable(R.mipmap.room_icon_use_integral)
                                    mBinding.tvStatus.text = Constants.GiftType.INTEGRAL.value
                                    mBinding.ivGift.setImageResource(R.mipmap.room_icon_gift_integral)
                                    mBinding.tvCount.text = giftCount
                                    mBinding.tvCount.setTextColor(Color.WHITE)
                                    mBinding.tvCount.visibility = View.VISIBLE
                                } else if (giftType == Constants.GiftType.BLIND_BOX.type) {
                                    mBinding.tvReward.text = "拆盲盒"
                                    mBinding.tvReward.background = resources.getDrawable(R.mipmap.room_icon_use_integral)
                                    mBinding.tvStatus.text = Constants.GiftType.BLIND_BOX.value
                                    mBinding.ivGift.setImageResource(com.kissspace.module_common.R.mipmap.common_icon_reward)
                                    mBinding.tvCount.visibility = View.GONE
                                }
                            }
                            Constants.TaskStatus.RECEIVED.type -> {
                                mBinding.tvReward.visibility = View.GONE
                                mBinding.tvTime.visibility = View.INVISIBLE
                                mBinding.tvReceiveStatus.visibility = View.VISIBLE
                                mBinding.tvReceiveStatus.text = Constants.TaskStatus.RECEIVED.value
                                mBinding.tvCount.setTextColor(resources.getColor(com.kissspace.module_common.R.color.color_80FFFFFF))
                                mBinding.clTvStatus.visibility = View.INVISIBLE
                                if (giftType == Constants.GiftType.INTEGRAL.type) {
                                    mBinding.ivGift.setImageResource(R.mipmap.room_icon_gift_integral_alpha)
                                    mBinding.tvCount.text = giftCount
                                    mBinding.tvCount.visibility = View.VISIBLE
                                } else if (giftType == Constants.GiftType.BLIND_BOX.type) {
                                    mBinding.ivGift.setImageResource(com.kissspace.module_common.R.mipmap.common_icon_reward)
                                    mBinding.tvCount.visibility = View.GONE
                                }
                            }
                        }
                    }
                }
            }

            onFastClick(R.id.tv_reward) {
                val model = getModel<TaskRewardModel>()
                val type: Type = object : TypeToken<Map<String, Int>>() {}.getType()
                val countMap: Map<String, Long> = GsonUtils.fromJson(model.rewardType, type)
                val taskReward = countMap.entries.firstOrNull()?.key ?: ""
                if (taskReward == Constants.GiftType.BLIND_BOX.type) {
                    if (model.finishStatus == Constants.TaskStatus.TOBE_COLLECTED.type) {
                        //拆盲盒
                        FlowBus.post(Event.OpenBlindBoxEvent)
                        dismiss()
                    }
                } else {
                    model.taskId?.let {
                        getReceiveTaskReward(it) { success ->
                            if (success) {
                                refreshTaskRewardList()
                            }
                        }
                    }
                }
            }
        }.models = taskRewardList

        mBinding.tvCollectPoints.safeClick {
            val list = taskRewardList.filterList {
                this.finishStatus == Constants.TaskStatus.TOBE_COLLECTED.type
            }
            val taskIdList: MutableList<String?> = ArrayList()
            list.forEach {
                it.taskId?.let { id ->
                    taskIdList.add(id)
                }
            }
            if (taskIdList.isNotEmpty()) {
                mViewModel.getAllIntegralList(taskIdList)
            }
        }

        mBinding.tvUseIntegral.safeClick {
            //使用积分
            FlowBus.post(Event.UseTaskRewardPointsEvent)
            dismiss()
        }

        mBinding.ivQuestion.safeClick {
            jump(
                RouterPath.PATH_WEBVIEW,
                "url" to getH5Url(Constants.H5.pointsReceiveUrl),
                "showTitle" to true
            )
        }
        initEventObserver()
    }

    private fun initEventObserver() {
        FlowBus.observerEvent<Event.RefreshPoints>(this) {
            //重新获取任务列表
            mViewModel.getChatRoomTaskRewardList()
        }
    }

    private fun addDefaultTaskReward() {
        taskRewardList.add(
            TaskRewardModel(
                finishStatus = Constants.TaskStatus.TOBE_COLLECTED.type,
                rewardType = "{\"003\":1}"
            )
        )
    }

    override fun observerData() {
        super.observerData()
        //获取积分列表弹窗
        collectData(mViewModel.taskRewardListEvent, onSuccess = {
            taskRewardList.clear()
            addDefaultTaskReward()
            taskRewardList.addAll(it)
            mBinding.rvTaskList.models = taskRewardList
        })
        //一键获取所有积分
        collectData(mViewModel.getTaskPointsListEvent, onSuccess = {
            refreshTaskRewardList()
        })
    }

    private fun refreshTaskRewardList() {
        toast("领取成功")
        //重新获取任务列表
        mViewModel.getChatRoomTaskRewardList()
        FlowBus.post(Event.RefreshPoints)
        refreshIntegral()
    }

    private fun refreshIntegral() {
        getUserInfo(onSuccess = { userinfo ->
            mBinding.tvIntegralBalance.text =
                formatNumIntegral(userinfo.integral)
        })
    }


}