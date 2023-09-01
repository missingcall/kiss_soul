package com.kissspace.mine.ui.activity.feedback

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.didi.drouter.annotation.Router
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.kissspace.common.base.BaseActivity
import com.kissspace.common.ext.*
import com.kissspace.common.flowbus.Event
import com.kissspace.common.flowbus.FlowBus
import com.kissspace.common.model.feedback.FeedBackModel
import com.kissspace.common.router.RouterPath
import com.kissspace.common.router.RouterPath.PATH_ADD_FEEDBACK
import com.kissspace.common.router.jump
import com.kissspace.common.router.parseIntent
import com.kissspace.mine.ui.activity.feedback.FeedBackAddActivity.Companion.FeedBackAddResult
import com.kissspace.mine.ui.activity.feedback.FeedBackRecodeListActivity.Companion.FeedBackRecodeListResult
import com.kissspace.mine.viewmodel.FeedBackViewModel
import com.kissspace.module_mine.R
import com.kissspace.module_mine.databinding.MineActivtiyFeedbackBinding
import com.kissspace.util.activity

/**
 * @Author gaohangbo
 * @Date 2023/1/6 20:02.
 * @Describe
 */
@Router(uri = RouterPath.PATH_FEEDBACK_TYPE_LIST)
class FeedBackTypeListActivity : BaseActivity(R.layout.mine_activtiy_feedback) {

    private val mBinding by viewBinding<MineActivtiyFeedbackBinding>()

    private val mViewModel by viewModels<FeedBackViewModel>()

    var launcher: ActivityResultLauncher<Intent>? = null

    private var showFeedBack by parseIntent<Boolean>(defaultValue = false)

    override fun initView(savedInstanceState: Bundle?) {
        setTitleBarListener(mBinding.titleBar, rightClick = {
            jump(RouterPath.PATH_FEEDBACK_RECODE_LIST, activity = activity, resultLauncher = launcher)
        })

        mBinding.m = mViewModel
        mBinding.lifecycleOwner = this
        mViewModel.isShowRedSpot.value = showFeedBack
        launcher = registerForActivityResult(object : ActivityResultContract<Intent, String?>() {
            override fun createIntent(context: Context, input: Intent): Intent {
                return input
            }

            override fun parseResult(resultCode: Int, intent: Intent?): String? {
                return intent?.getStringExtra("result")
            }
        }) {
            if (it.equals(FeedBackRecodeListResult)) {
                mViewModel.isShowRedSpot.value = false
            } else if (it.equals(FeedBackAddResult)) {
                finish()
            }
        }
        //获取意见反馈类型
        mViewModel.getFeedBackListType {
            mBinding.rvFeedback.bindingAdapter.addModels(it)
        }

        mBinding.rvFeedback.linear().setup {
            addType<FeedBackModel>(R.layout.mine_item_feedback_type)
            onFastClick(R.id.cl_root) {
                val model = getModel<FeedBackModel>(modelPosition)
                jump(
                    PATH_ADD_FEEDBACK,
                    "feedBackTypeId" to model.typeId.orEmpty(),
                    "feedBackTypeName" to model.typeName.orEmpty(), resultLauncher = launcher
                )
            }
            onBind {
                findView<View>(R.id.view_line).visibility =
                    if (modelPosition == modelCount - 1) View.INVISIBLE else View.VISIBLE
            }

        }.models = mutableListOf<FeedBackModel>()

        FlowBus.observerEvent<Event.RefreshFeedBackType>(this) {
            mViewModel.isShowRedSpot.value = true
        }
    }

}