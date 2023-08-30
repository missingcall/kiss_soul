package com.kissspace.message.ui.activity

import android.os.Bundle
import android.widget.TextView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.didi.drouter.annotation.Router
import com.kissspace.common.base.BaseActivity
import com.kissspace.common.ext.setTitleBarListener
import com.kissspace.common.router.RouterPath
import com.kissspace.module_message.R
import com.kissspace.module_message.databinding.MessageTaskActivityBinding

/**
 * @Author gaohangbo
 * @Date 2023/8/17 22:24.
 * @Describe 任务中心
 */
@Router(path = RouterPath.PATH_MESSAGE_TASK)
class MessageTaskActivity: BaseActivity(R.layout.message_task_activity) {
    private val mBinding by viewBinding<MessageTaskActivityBinding>()
    override fun initView(savedInstanceState: Bundle?) {
        setTitleBarListener(mBinding.titleBar)
        mBinding.stateLayout.onEmpty {
            findViewById<TextView>(com.kissspace.module_common.R.id.tv_empty).text = "还没有任务消息呢"
        }
        mBinding.stateLayout.showEmpty()

    }
}