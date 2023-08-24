package com.kissspace.room.widget

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.Observer
import com.netease.nimlib.sdk.chatroom.ChatRoomServiceObserver
import com.netease.nimlib.sdk.chatroom.model.ChatRoomMessage
import com.kissspace.common.config.Constants
import com.kissspace.common.model.MicQueueUserModel
import com.kissspace.common.util.getMutable
import com.kissspace.common.util.customToast
import com.kissspace.common.widget.BaseBottomSheetDialogFragment
import com.kissspace.common.widget.CommonConfirmDialog
import com.kissspace.module_room.R
import com.kissspace.module_room.databinding.RoomDialogMicQueueAnchorBinding
import com.kissspace.network.result.collectData
import com.kissspace.room.viewmodel.MicQueueViewModel
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception

/**
 *
 * @Author: nicko
 * @CreateDate: 2023/1/29 18:16
 * @Description: 排麦弹窗-管理端
 *
 */
class AnchorMicQueueDialog : BaseBottomSheetDialogFragment<RoomDialogMicQueueAnchorBinding>() {
    private val mViewModel by viewModels<MicQueueViewModel>()
    private lateinit var crId: String
    private val customMessageObserver = Observer<List<ChatRoomMessage>> {
        it?.forEach { message ->
            try {
                val json = JSONObject(message.attachStr)
                val type = json.getString("type")
                if (type == Constants.IMMessageType.MSG_USER_MIC_QUEUE ||
                    type == Constants.IMMessageType.MSG_MANAGER_CANCEL_MIC_QUEUE ||
                    type == Constants.IMMessageType.MSG_USER_CANCEL_MIC_QUEUE
                ) {
                    mViewModel.getMicQueueUserList(crId)
                }
            } catch (e: Exception) {
                LogUtils.e("消息格式错误！${e.message}")
            }
        }

    }

    companion object {
        fun newInstance(crId: String) = AnchorMicQueueDialog().apply {
            arguments = bundleOf("crId" to crId)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        crId = arguments?.getString("crId")!!
        NIMClient.getService(ChatRoomServiceObserver::class.java)
            .observeReceiveMessage(customMessageObserver, true)
    }

    override fun getViewBinding() = RoomDialogMicQueueAnchorBinding.inflate(layoutInflater)

    override fun initView() {
        mViewModel.getMicQueueUserList(crId)
        mBinding.tvCleanAll.setOnClickListener {
            CommonConfirmDialog(requireContext(), "是否清空列表？") {
                if (this) {
                    val array = JSONArray()
                    val list = mBinding.recyclerView.getMutable<MicQueueUserModel>()
                    list.forEach {
                        array.put(it.userId)
                    }
                    mViewModel.cancelQueue(crId, array) {
                        mBinding.recyclerView.visibility = View.INVISIBLE
                        mBinding.tvCleanAll.visibility = View.INVISIBLE
                        mBinding.tvInvite.visibility = View.INVISIBLE
                        mBinding.tvEmpty.visibility = View.VISIBLE
                        mBinding.ivEmpty.visibility = View.VISIBLE
                    }
                }
            }.show()

        }
        mBinding.tvInvite.setOnClickListener {
            if (mBinding.recyclerView.bindingAdapter.checkedPosition.isEmpty()) {
                ToastUtils.showLong("请选择要抱上麦的用户")
                return@setOnClickListener
            }
            val checkedIndex = mBinding.recyclerView.bindingAdapter.checkedPosition[0]
            val model =
                mBinding.recyclerView.getMutable<MicQueueUserModel>()[checkedIndex]
            mViewModel.inviteMic(crId, model.userId) {
                customToast("操作成功")
                deleteData(checkedIndex)
            }
        }
    }


    @SuppressLint("SetTextI18n")
    override fun observerData() {
        super.observerData()
        collectData(mViewModel.getQueueUsersEvent, onSuccess = {
            mBinding.recyclerView.visibility = View.VISIBLE
            mBinding.tvCleanAll.visibility = View.VISIBLE
            mBinding.tvInvite.visibility = View.VISIBLE
            mBinding.tvEmpty.visibility = View.GONE
            mBinding.ivEmpty.visibility = View.GONE
            mBinding.recyclerView.linear().setup {
                singleMode = true
                addType<MicQueueUserModel> { R.layout.room_dialog_mic_queue_manager_item }
                onChecked { position, isChecked, _ ->
                    val model = getModel<MicQueueUserModel>(position)
                    model.isChecked = isChecked
                    model.notifyChange()
                }
                onFastClick(R.id.iv_checkbox) {
                    checkedSwitch(modelPosition)
                }
                onClick(R.id.iv_delete) {
                    val model = getModel<MicQueueUserModel>()
                    val array = JSONArray()
                    array.put(model.userId)
                    mViewModel.cancelQueue(crId, array) {
                        deleteData(modelPosition)
                    }
                }
            }.models = it
        }, onEmpty = {
            mBinding.tvCleanAll.visibility = View.INVISIBLE
            mBinding.tvInvite.visibility = View.INVISIBLE
            mBinding.tvEmpty.visibility = View.VISIBLE
            mBinding.ivEmpty.visibility = View.VISIBLE
            mBinding.recyclerView.visibility = View.INVISIBLE
        })
    }

    private fun deleteData(index: Int) {
        mBinding.recyclerView.bindingAdapter.mutable.removeAt(index)
        mBinding.recyclerView.bindingAdapter.notifyItemRemoved(index)
        if (mBinding.recyclerView.bindingAdapter.mutable.isEmpty()) {
            mBinding.tvCleanAll.visibility = View.INVISIBLE
            mBinding.tvInvite.visibility = View.INVISIBLE
            mBinding.tvEmpty.visibility = View.VISIBLE
            mBinding.ivEmpty.visibility = View.VISIBLE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        NIMClient.getService(ChatRoomServiceObserver::class.java)
            .observeReceiveMessage(customMessageObserver, false)
    }
}