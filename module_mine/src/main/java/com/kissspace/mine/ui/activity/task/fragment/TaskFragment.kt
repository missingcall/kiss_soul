package com.kissspace.mine.ui.activity.task.fragment

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.viewModels
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.kissspace.common.base.BaseFragment
import com.kissspace.common.binding.dataBinding
import com.kissspace.common.config.Constants.TASK_FINISH
import com.kissspace.common.config.Constants.TASK_NOT_FINISH
import com.kissspace.common.config.Constants.TASK_WAIT_TO_REWARD
import com.kissspace.common.http.getReceiveTaskReward
import com.kissspace.common.model.task.DailyTaskInfo
import com.kissspace.common.model.task.NoviceTaskInfo
import com.kissspace.common.util.*
import com.kissspace.common.widget.Footer
import com.kissspace.mine.viewmodel.TaskCenterViewModel
import com.kissspace.module_mine.R
import com.kissspace.module_mine.databinding.MineFragmentTaskDayBinding
import com.kissspace.module_mine.databinding.MineItemTaskDayCenterBinding
import com.kissspace.module_mine.databinding.MineItemTaskNewpeopleCenterBinding
import com.kissspace.util.loadImage
import com.kissspace.util.logE

/**
 * @Author gaohangbo
 * @Date 2023/1/9 13:06.
 * @Describe
 */
class TaskFragment : BaseFragment(R.layout.mine_fragment_task_day) {

    private val mBinding by dataBinding<MineFragmentTaskDayBinding>()
    private val mViewModel by viewModels<TaskCenterViewModel>()
    private var taskType: String? = null

    companion object {
        fun newInstance(tag: String) = TaskFragment().apply {
            arguments = bundleOf("taskType" to tag)
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        taskType = arguments?.getString("taskType")
        mBinding.pageRefreshLayout.apply {
            onRefresh {
                initData()
                mBinding.pageRefreshLayout.finishRefresh()
            }
        }
        initData()
    }

    private fun initData() {
        mViewModel.requestTaskList(onSuccess = {
            if (taskType.equals("DAY")) {
                initDayTask(it.dailyTaskInfoList)
            } else if (taskType.equals("NEW_PEOPLE")) {
                initNewPeopleTask(it.noviceTaskInfoList)
            }
        })
    }

    private fun initNewPeopleTask(noviceTaskInfoList: List<NoviceTaskInfo>) {
        mBinding.rvTask.linear().setup {
            addType<NoviceTaskInfo>(R.layout.mine_item_task_newpeople_center)
            addType<Footer>(R.layout.mine_task_bottom_view)

            onBind {
                when (val viewBinding = getBinding<ViewDataBinding>()) {
                    is MineItemTaskNewpeopleCenterBinding -> {
                        val model = getModel<NoviceTaskInfo>()
                        //	任务状态 001 未完成 002 待领取奖励 003 已完成
                        when (model.finishStatus) {
                            TASK_NOT_FINISH -> {
                                viewBinding.tvButton.setBackgroundResource(R.mipmap.mine_task_bg_go)
                            }
                            TASK_WAIT_TO_REWARD -> {
                                viewBinding.tvButton.isEnabled = true
                                viewBinding.tvButton.setBackgroundResource(R.mipmap.mine_task_bg_claim_rewards)
                            }
                            TASK_FINISH -> {
                                viewBinding.tvButton.isEnabled = false
                                viewBinding.tvButton.setBackgroundResource(R.mipmap.mine_task_bg_completed)
                            }
                        }
                        model.rewardType?.let {
                            val pair = getFirstGiftTypeImgText(it)
                            var i=0
                            for (item in pair){
                                if(i==0){
                                    setGift(
                                        viewBinding.ivGift, viewBinding.tvTaskReward, item.first,
                                        item.second as String
                                    )
                                }else if(i==1){
                                    setGift(
                                        viewBinding.ivGift1, viewBinding.tvTaskReward1, item.first,
                                        item.second as String
                                    )
                                }
                                i += 1
                            }
                        }
//                        viewBinding.tvTaskProgress.text = "0/1"
//                        if(modelPosition==1){
//                            viewBinding.tvTaskProgress.visibility= View.GONE
//                        }else{
//                            viewBinding.tvTaskProgress.visibility= View.VISIBLE
//                        }
                    }
                }
            }
            onFastClick(R.id.tv_button) {
                val model = getModel<NoviceTaskInfo>()
                if (model.finishStatus == TASK_WAIT_TO_REWARD) {
                    getReceiveTaskReward(model.taskId) {
                        taskType = arguments?.getString("taskType")
                        mViewModel.requestTaskList(onSuccess = {
                            val tvButton = findView<TextView>(R.id.tv_button)
                            tvButton.isEnabled = false
                            tvButton.setBackgroundResource(R.mipmap.mine_task_bg_completed)
                        })
                    }
                } else if (model.finishStatus == TASK_NOT_FINISH) {
                    logE("model.schema" + model.schema)
                    handleSchema(model.schema)
                }

            }
        }.models = noviceTaskInfoList
        mBinding.rvTask.bindingAdapter.addFooter(Footer(), animation = true)
    }

    private fun initDayTask(dailyTaskInfoList: List<DailyTaskInfo>) {
        mBinding.rvTask.linear().setup {
            addType<DailyTaskInfo>(R.layout.mine_item_task_day_center)
            addType<Footer>(R.layout.mine_task_bottom_view)
            onBind {
                when (val viewBinding = getBinding<ViewDataBinding>()) {
                    is MineItemTaskDayCenterBinding -> {
                        val model = getModel<DailyTaskInfo>()
                        //	任务状态 001 未完成 002 待领取奖励 003 已完成
                        when (model.finishStatus) {
                            TASK_NOT_FINISH -> {
                                viewBinding.tvButton.setBackgroundResource(R.mipmap.mine_task_bg_go)
                            }
                            TASK_WAIT_TO_REWARD -> {
                                viewBinding.tvButton.isEnabled = true
                                viewBinding.tvButton.setBackgroundResource(R.mipmap.mine_task_bg_claim_rewards)
                            }
                            TASK_FINISH -> {
                                viewBinding.tvButton.isEnabled = false
                                viewBinding.tvButton.setBackgroundResource(R.mipmap.mine_task_bg_completed)
                            }
                        }
//                        viewBinding.tvTaskProgress.text = "0/1"
//                        if(modelPosition==1){
//                            viewBinding.tvTaskProgress.visibility= View.GONE
//                        }else{
//                            viewBinding.tvTaskProgress.visibility= View.VISIBLE
//                        }
                        model.rewardType?.let {
                            val pair = getFirstGiftTypeImgText(it)
                            logE("pair$pair")
                            var i=0
                            for (item in pair){
                                if(i==0){
                                    setGift(
                                        viewBinding.ivGift, viewBinding.tvTaskReward, item.first,
                                        item.second as String
                                    )
                                }else if(i==1){
                                    setGift(
                                        viewBinding.ivGift1, viewBinding.tvTaskReward1, item.first,
                                        item.second as String
                                    )
                                }
                                i += 1
                            }
                        }
                    }
                }
            }
            onFastClick(R.id.tv_button) {
                val model = getModel<DailyTaskInfo>()
                if (model.finishStatus == TASK_WAIT_TO_REWARD) {
                    getReceiveTaskReward(model.taskId) {
                        val tvButton = findView<TextView>(R.id.tv_button)
                        tvButton.isEnabled = false
                        tvButton.setBackgroundResource(R.mipmap.mine_task_bg_completed)
                    }
                } else if (model.finishStatus == TASK_NOT_FINISH) {
                    logE("model.schema" + model.schema)
                    handleSchema(model.schema)
                }

            }
        }.models = dailyTaskInfoList
        mBinding.rvTask.bindingAdapter.addFooter(Footer(), animation = true)
    }

    private fun setGift(imageView: ImageView, textView: TextView, image: Any?, text: String) {
        if (image is String) {
            imageView.loadImage(image)
        } else if (image is Int) {
            imageView.setImageResource(image)
        }
        textView.text = text
    }

}