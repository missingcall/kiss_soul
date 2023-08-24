package com.kissspace.room.widget

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ToastUtils
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.kissspace.common.config.Constants
import com.kissspace.common.model.RoomOnLineUserListBean
import com.kissspace.common.util.mmkv.MMKVProvider
import com.kissspace.common.widget.BaseBottomSheetDialogFragment
import com.kissspace.module_room.R
import com.kissspace.module_room.databinding.RoomDialogSettingManagerBinding
import com.kissspace.network.result.collectData
import com.kissspace.room.viewmodel.SettingManagerViewModel
import com.kissspace.util.toast

/**
 *  设置管理/抱上麦弹窗
 */
class SettingManagerDialog :
    BaseBottomSheetDialogFragment<RoomDialogSettingManagerBinding>() {
    private val mViewModel by viewModels<SettingManagerViewModel>()
    private val mUserList = mutableListOf<RoomOnLineUserListBean>()
    private lateinit var crId: String

    companion object {
        fun newInstance(crId: String) = SettingManagerDialog().apply {
            arguments = bundleOf("crId" to crId)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            crId = it.getString("crId")!!
        }
    }

    override fun getViewBinding() = RoomDialogSettingManagerBinding.inflate(layoutInflater)

    override fun initView() {
        mViewModel.getOnLineUsers(crId)
        mBinding.commonSearchView.onEditorActionBlock = {
            mBinding.recyclerView.bindingAdapter.checkedPosition.clear()
            if (it.isEmpty()) {
                mUserList.clear()
                mBinding.recyclerView.bindingAdapter.mutable.clear()
                mViewModel.getOnLineUsers(crId)
            } else {
                val newList = mUserList.filter {bean->
                    bean.displayId.contains(it) || bean.nickname.contains(
                        it
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
               toast("请选择用户")
            } else {
                val model = checkedList[0]
                if (model.role == Constants.ROOM_USER_TYPE_MANAGER) {
                    mViewModel.cancelManager(crId, model.userId)
                } else {
                    mViewModel.setManager(crId, model.userId)
                }
            }
        }

        mBinding.recyclerView.linear().setup {
            addType<RoomOnLineUserListBean> { R.layout.room_dialog_setting_manager_item }
            singleMode = true
            onChecked { position, isChecked, _ ->
                val model = getModel<RoomOnLineUserListBean>(position)
                model.isChecked = isChecked
                model.notifyChange()
                if (model.role == Constants.ROOM_USER_TYPE_MANAGER) {
                    mBinding.tvSubmit.text = "取消管理"
                } else {
                    mBinding.tvSubmit.text = "设为管理"
                }
            }
            onFastClick(R.id.iv_checkbox) {
                checkedSwitch(modelPosition)
            }
        }.models = mutableListOf()
    }

    override fun observerData() {
        super.observerData()
        collectData(mViewModel.getUsersEvent, onSuccess = {
            it.removeAll { that -> that.userId == MMKVProvider.userId }
            mUserList.addAll(it)
            mBinding.recyclerView.bindingAdapter.addModels(mUserList)
        })

        collectData(mViewModel.setManagerEvent, onSuccess = {
            val checkedList =
                mBinding.recyclerView.bindingAdapter.getCheckedModels<RoomOnLineUserListBean>()
            if (checkedList.isNotEmpty()) {
                checkedList[0].role = Constants.ROOM_USER_TYPE_MANAGER
                checkedList[0].notifyChange()
                if (checkedList[0].role == Constants.ROOM_USER_TYPE_MANAGER) {
                    mBinding.tvSubmit.text = "取消管理"
                } else {
                    mBinding.tvSubmit.text = "设为管理"
                }
            }
        })

        collectData(mViewModel.cancelManagerEvent, onSuccess = {
            val checkedList =
                mBinding.recyclerView.bindingAdapter.getCheckedModels<RoomOnLineUserListBean>()
            if (checkedList.isNotEmpty()) {
                checkedList[0].role = Constants.ROOM_USER_TYPE_NORMAL
                checkedList[0].notifyChange()
                if (checkedList[0].role == Constants.ROOM_USER_TYPE_MANAGER) {
                    mBinding.tvSubmit.text = "取消管理"
                } else {
                    mBinding.tvSubmit.text = "设为管理"
                }
            }
        })
    }

}