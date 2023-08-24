package com.kissspace.message.ui.activity

import android.graphics.Color
import android.os.Bundle
import by.kirich1409.viewbindingdelegate.viewBinding
import com.didi.drouter.annotation.Router
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.kissspace.common.base.BaseActivity
import com.kissspace.common.router.jump
import com.kissspace.common.ext.scrollToBottom
import com.kissspace.common.ext.setTitleBarListener
import com.kissspace.common.model.SystemMessageModel
import com.kissspace.common.model.SystemMessageResponse
import com.kissspace.common.router.RouterPath
import com.kissspace.common.util.mmkv.MMKVProvider
import com.kissspace.message.http.MessageApi
import com.kissspace.module_message.R
import com.kissspace.module_message.databinding.MessageAcitivitySystemMessageBinding
import com.kissspace.network.net.Method
import com.kissspace.network.net.request
import com.kissspace.util.setStatusBarColor

/**
 *
 * @Author: nicko
 * @CreateDate: 2023/2/20
 * @Description: 系统消息
 *
 */
@Router(path = RouterPath.PATH_SYSTEM_MESSAGE)
class SystemMessageActivity : com.kissspace.common.base.BaseActivity(R.layout.message_acitivity_system_message) {
    private val mBinding by viewBinding<MessageAcitivitySystemMessageBinding>()

    override fun initView(savedInstanceState: Bundle?) {
        setStatusBarColor(Color.TRANSPARENT,false)
        setTitleBarListener(mBinding.titleBar)
        initRefresh()
        initRecycler()
        initData(true)
    }

    private fun initRecycler() {
        mBinding.recyclerView.linear().setup {
            addType<SystemMessageModel> {
                if (this.messageFormat == "001") R.layout.message_system_message_item_normal else R.layout.message_system_message_item_multi
            }
            onClick(R.id.root) {
                val model = getModel<SystemMessageModel>()
                if (model.hyperlink.isNotEmpty()) {
                    jump(RouterPath.PATH_WEBVIEW, "url" to model.hyperlink, "showTitle" to true)
                }
            }
        }.models = mutableListOf()
    }

    private fun initRefresh() {
        mBinding.pageRefreshLayout.apply {
            upFetchEnabled = true
            onLoadMore { initData(false) }
        }
    }

    private fun initData(isFirstLoad: Boolean) {
        val param = mutableMapOf<String, Any?>()
        param["pageNum"] = mBinding.pageRefreshLayout.index
        param["pageSize"] = 10
        param["os"] = "android"
        request<SystemMessageResponse>(
            MessageApi.API_SYSTEM_MESSAGE,
            Method.GET,
            param,
            onSuccess = {
                MMKVProvider.systemMessageLastReadCount = it.total
                mBinding.pageRefreshLayout.addData(it.records.asReversed(), isEmpty = {
                    it.total == 0
                }, hasMore = {
                    it.size * 10 < it.total
                })
                if (isFirstLoad) {
                    mBinding.recyclerView.scrollToBottom()
                }
            })
    }

}