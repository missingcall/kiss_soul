package com.kissspace.mine.ui.activity.feedback

import android.os.Bundle
import android.widget.LinearLayout
import androidx.activity.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.didi.drouter.annotation.Router
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.hjq.bar.OnTitleBarListener
import com.hjq.bar.TitleBar
import com.kissspace.common.base.BaseActivity
import com.kissspace.common.model.feedback.FeedBackRecodeModel
import com.kissspace.common.router.RouterPath
import com.kissspace.mine.viewmodel.FeedBackViewModel
import com.kissspace.module_mine.R
import com.kissspace.module_mine.databinding.MineActivtiyFeedbackListBinding
import com.kissspace.module_mine.databinding.MineItemFeedbackRecodeBinding
import com.kissspace.util.logE

/**
 * @Author gaohangbo
 * @Date 2023/1/7 10:46.
 * @Describe
 */
@Router(path = RouterPath.PATH_FEEDBACK_RECODE_LIST)
class FeedBackRecodeListActivity : com.kissspace.common.base.BaseActivity(R.layout.mine_activtiy_feedback_list) {
    private val mBinding by viewBinding<MineActivtiyFeedbackListBinding>()
    private val mViewModel by viewModels<FeedBackViewModel>()
    override fun initView(savedInstanceState: Bundle?) {

        mBinding.m = mViewModel
        mBinding.lifecycleOwner = this
        mBinding.titleBar.setOnTitleBarListener(object : OnTitleBarListener {
            override fun onLeftClick(titleBar: TitleBar?) {
                super.onLeftClick(titleBar)
                setResult(RESULT_OK,intent.putExtra("result",FeedBackRecodeListResult))
                finish()
            }
        })

        mViewModel.getFeedBackListRecode {
            mBinding.rvFeedback.bindingAdapter.addModels(it)
            mViewModel.isShowEmpty.value = it.isEmpty()
        }
        mBinding.rvFeedback.linear().setup {
            addType<FeedBackRecodeModel>(R.layout.mine_item_feedback_recode)
            onBind {
                val model = getModel<FeedBackRecodeModel>()
                logE("model.feedbackReply" + model.feedbackReply)
                model.isShowFeedBackReply = model.feedbackReply.isNotBlank()
                val mineItemFeedBackBinding = getBinding<MineItemFeedbackRecodeBinding>()
                mineItemFeedBackBinding.rvImage.linear(LinearLayout.HORIZONTAL).setup {
                    addType<String>(R.layout.mine_item_feedback_picture)
//
                }.models = model.feedbackImage
            }

        }.models = mutableListOf()

    }
    companion object {
        const val FeedBackRecodeListResult = "FeedBackRecodeListResult"
    }
}