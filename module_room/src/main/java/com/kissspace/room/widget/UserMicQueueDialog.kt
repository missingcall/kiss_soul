package com.kissspace.room.widget

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.LogUtils
import com.drake.brv.utils.addModels
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.linear
import com.drake.brv.utils.mutable
import com.drake.brv.utils.setup
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.Observer
import com.netease.nimlib.sdk.chatroom.ChatRoomServiceObserver
import com.netease.nimlib.sdk.chatroom.model.ChatRoomMessage
import com.kissspace.common.config.Constants
import com.kissspace.common.model.MicQueueUserModel
import com.kissspace.common.model.immessage.BaseAttachment
import com.kissspace.common.model.immessage.MicQueueMessage
import com.kissspace.common.util.mmkv.MMKVProvider
import com.kissspace.common.util.parseCustomMessage
import com.kissspace.common.util.customToast
import com.kissspace.common.widget.BaseBottomSheetDialogFragment
import com.kissspace.module_room.R
import com.kissspace.module_room.databinding.RoomDialogMicQueueUserBinding
import com.kissspace.network.result.collectData
import com.kissspace.room.viewmodel.MicQueueViewModel
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception

/**
 *
 * @Author: nicko
 * @CreateDate: 2023/1/29 18:16
 * @Description: 排麦弹窗-用户端
 *
 */
class UserMicQueueDialog : BaseBottomSheetDialogFragment<RoomDialogMicQueueUserBinding>() {
    private val mViewModel by viewModels<MicQueueViewModel>()
    private lateinit var crId: String
    private var callBack: () -> Unit = {}
    private val customMessageObserver = Observer<List<ChatRoomMessage>> {
        it?.forEach { message ->
            try {
                val json = JSONObject(message.attachStr)
                val type = json.getString("type")
                when (type) {
                    Constants.IMMessageType.MSG_USER_MIC_QUEUE -> {
                        mViewModel.getMicQueueUserList(crId)
                    }

                    Constants.IMMessageType.MSG_TYPE_INVITE_MIC -> {
                        val data = BaseAttachment(type, json.get("data"))
                        val message = parseCustomMessage<MicQueueMessage>(data.data)
                        if (message.userId == MMKVProvider.userId) {
                            dismiss()
                        }
                    }

                    Constants.IMMessageType.MSG_MANAGER_CANCEL_MIC_QUEUE,
                    Constants.IMMessageType.MSG_USER_CANCEL_MIC_QUEUE -> {
                        val data = BaseAttachment(type, json.get("data"))
                        val message = parseCustomMessage<MicQueueMessage>(data.data)
                        if (message.userId == MMKVProvider.userId) {
                            customToast("您已被请出了排麦队伍")
                            mBinding.tvCancel.visibility = View.INVISIBLE
                        }
                        mViewModel.getMicQueueUserList(crId)
                    }
                }
            } catch (e: Exception) {
                LogUtils.e("消息格式错误！${e.message}")
            }
        }
    }

    companion object {
        fun newInstance(crId: String) = UserMicQueueDialog().apply {
            arguments = bundleOf("crId" to crId)
        }
    }

    fun setCallBack(block: () -> Unit) {
        this.callBack = block
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        crId = arguments?.getString("crId")!!
        NIMClient.getService(ChatRoomServiceObserver::class.java)
            .observeReceiveMessage(customMessageObserver, true)
    }

    override fun getViewBinding() = RoomDialogMicQueueUserBinding.inflate(layoutInflater)

    override fun initView() {
        mViewModel.getMicQueueUserList(crId)
        mBinding.tvCancel.setOnClickListener {
            val jsonArray = JSONArray()
            jsonArray.put(MMKVProvider.userId)
            mViewModel.cancelQueue(crId, jsonArray) {
                customToast("你结束了本次排麦")
                callBack()
                dismiss()
            }
        }

        mBinding.recyclerView.linear().setup {
            addType<MicQueueUserModel> { R.layout.room_dialog_mic_queue_user_item }
            onBind {
                val index = findView<TextView>(R.id.tv_index)
                index.text = (modelPosition + 1).toString()
            }
        }.models = mutableListOf()
    }


    @SuppressLint("SetTextI18n")
    override fun observerData() {
        super.observerData()
        collectData(mViewModel.getQueueUsersEvent, onSuccess = {
            mBinding.recyclerView.visibility = View.VISIBLE
            mBinding.recyclerView.mutable.clear()
            mBinding.recyclerView.addModels(it)
        }, onEmpty = {
            mBinding.recyclerView.bindingAdapter.mutable.clear()
            mBinding.recyclerView.visibility = View.INVISIBLE
            mBinding.tvTips.visibility = View.INVISIBLE
            dismiss()
        })


    }

    override fun onDestroy() {
        super.onDestroy()
        NIMClient.getService(ChatRoomServiceObserver::class.java)
            .observeReceiveMessage(customMessageObserver, false)
    }
}