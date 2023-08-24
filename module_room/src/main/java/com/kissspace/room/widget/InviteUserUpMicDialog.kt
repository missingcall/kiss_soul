package com.kissspace.room.widget

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.ToastUtils
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.kissspace.common.config.Constants
import com.kissspace.common.model.RoomOnLineUserListBean
import com.kissspace.common.util.mmkv.MMKVProvider
import com.kissspace.common.util.customToast
import com.kissspace.common.widget.BaseBottomSheetDialogFragment
import com.kissspace.module_room.R
import com.kissspace.module_room.databinding.RoomDialogInviteUpMicBinding
import com.kissspace.network.result.collectData
import com.kissspace.room.viewmodel.InviteMicViewModel

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/12/21 15:57
 * @Description: 抱上麦接口联调
 *
 */
class InviteUserUpMicDialog :
    BaseBottomSheetDialogFragment<RoomDialogInviteUpMicBinding>() {
    private val mViewModel by viewModels<InviteMicViewModel>()
    private val mUserList = mutableListOf<RoomOnLineUserListBean>()
    private lateinit var crId: String
    private var index = -1

    companion object {
        fun newInstance(crId: String, index: Int) = InviteUserUpMicDialog().apply {
            arguments = bundleOf("crId" to crId, "index" to index)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            crId = it.getString("crId")!!
            index = it.getInt("index")
        }
    }

    override fun getViewBinding() = RoomDialogInviteUpMicBinding.inflate(layoutInflater)

    override fun initView() {
        mViewModel.getOnLineUsers(crId)
        mBinding.tvTitle.text = if (index > -1) "抱上麦(${getTitle(index)})" else "抱上麦"
        mBinding.editSearch.addTextChangedListener { text ->
            mBinding.recyclerView.bindingAdapter.checkedPosition.clear()
            if (text.toString().isEmpty()) {
                mUserList.clear()
                mBinding.recyclerView.bindingAdapter.mutable.clear()
                mViewModel.getOnLineUsers(crId)
            } else {
                val newList = mUserList.filter {
                    it.displayId.contains(text.toString()) || it.nickname.contains(
                        text.toString()
                    )
                }
                mBinding.recyclerView.bindingAdapter.mutable.clear()
                mBinding.recyclerView.bindingAdapter.notifyDataSetChanged()
                mBinding.recyclerView.bindingAdapter.addModels(newList)
            }

        }

        mBinding.tvSubmit.setOnClickListener {
            val checkedList =
                mBinding.recyclerView.bindingAdapter.getCheckedModels<RoomOnLineUserListBean>()
            if (checkedList.isEmpty()) {
                customToast("请选择要抱上麦的用户", true)
            } else {
                val model = checkedList[0]
                mViewModel.inviteMic(crId, model.userId, index)
            }
        }

        mBinding.recyclerView.linear().setup {
            addType<RoomOnLineUserListBean> { R.layout.room_dialog_setting_manager_item }
            singleMode = true
            onChecked { position, isChecked, _ ->
                val model = getModel<RoomOnLineUserListBean>(position)
                model.isChecked = isChecked
                model.notifyChange()
            }
            onFastClick(R.id.iv_checkbox) {
                checkedSwitch(modelPosition)
            }
        }.models = mutableListOf()
    }

    override fun observerData() {
        super.observerData()
        collectData(mViewModel.getUsersEvent, onSuccess = {
            it.removeAll { that -> that.userId == MMKVProvider.userId || that.microPhone || that.role == Constants.ROOM_USER_TYPE_ANCHOR }
            mUserList.addAll(it)
            mBinding.recyclerView.bindingAdapter.addModels(mUserList)
        })

        collectData(mViewModel.inviteMicEvent, onSuccess = {
            val adapter = mBinding.recyclerView.bindingAdapter
            val checkedIndex = adapter.checkedPosition[0]
            adapter.mutable.removeAt(checkedIndex)
            adapter.notifyItemRemoved(checkedIndex)
            adapter.checkedPosition.clear()
            customToast("操作成功")
        }, onError = {
            ToastUtils.showLong(it.message)
        })

    }

    private fun getTitle(index: Int): String {
        return when (index) {
            0 -> "主持"
            8 -> "老板"
            else -> StringUtils.getString(R.string.room_mic_action_title_index, index)
        }
    }

}