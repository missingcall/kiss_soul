package com.kissspace.room.widget

import android.view.View
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.blankj.utilcode.util.ToastUtils
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.kissspace.common.config.Constants
import com.kissspace.common.model.MicQueueUserModel
import com.kissspace.common.util.mmkv.MMKVProvider
import com.kissspace.common.util.customToast
import com.kissspace.common.widget.BaseBottomSheetDialogFragment
import com.kissspace.common.widget.CommonConfirmDialog
import com.kissspace.module_room.R
import com.kissspace.module_room.databinding.RoomDialogMicQueueBinding
import com.kissspace.network.result.collectData
import com.kissspace.room.viewmodel.MicQueueViewModel
import org.json.JSONArray

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/12/20 16:25
 * @Description: 排麦弹窗
 *
 */
class MicQueueDialog : BaseBottomSheetDialogFragment<RoomDialogMicQueueBinding>() {
    private val mViewModel by viewModels<MicQueueViewModel>()
    private lateinit var userRole: String
    private lateinit var crId: String

    //取消所有
    private val CANCEL_MODE_ALL = 1

    //取消自己
    private val CANCEL_MODE_MINE = 2

    //取消其他人
    private val CANCEL_MODE_OTHER = 3
    fun initData(userRole: String, crId: String) {
        this.userRole = userRole
        this.crId = crId
    }

    override fun getViewBinding() = RoomDialogMicQueueBinding.inflate(layoutInflater)


    override fun initView() {
        initRecyclerView()
        initEvents()
        mViewModel.getMicQueueUserList(crId)

    }

    private fun initEvents() {
        mBinding.tvTips.visibility =
            if (userRole == Constants.ROOM_USER_TYPE_NORMAL) View.VISIBLE else View.GONE
        mBinding.tvCancel.visibility =
            if (userRole == Constants.ROOM_USER_TYPE_NORMAL) View.VISIBLE else View.INVISIBLE
        mBinding.tvCleanAll.visibility =
            if (userRole == Constants.ROOM_USER_TYPE_NORMAL) View.INVISIBLE else View.VISIBLE
        mBinding.tvInvite.visibility =
            if (userRole == Constants.ROOM_USER_TYPE_NORMAL) View.INVISIBLE else View.VISIBLE
        mBinding.tvCleanAll.setOnClickListener {
            CommonConfirmDialog(requireContext(), "是否清空列表") {
                if (this) {
                    mBinding.recyclerView.bindingAdapter.mutable.clear()
                    mBinding.recyclerView.bindingAdapter.notifyDataSetChanged()
                }
            }.show()
        }

        mBinding.tvCancel.setOnClickListener {
            val array = JSONArray()
            array.put(MMKVProvider.userId)
            mViewModel.cancelQueue(crId, array) {

            }
        }
        mBinding.tvCleanAll.setOnClickListener {
            val array = JSONArray()
            val data =
                mBinding.recyclerView.bindingAdapter.mutable as MutableList<MicQueueUserModel>
            data.forEach {
                array.put(it.userId)
            }
            mViewModel.cancelQueue(crId, array) {

            }
        }

        mBinding.tvInvite.setOnClickListener {
            if (getCheckedUser() == null) {
                customToast("请选择要抱上麦的用户",true)
            } else {
                mViewModel.inviteMic(crId, getCheckedUser()!!.userId){

                }
            }
        }
    }

    private fun getCheckedUser(): MicQueueUserModel? {
        val list = mBinding.recyclerView.bindingAdapter.getCheckedModels<MicQueueUserModel>()
        return if (list.isEmpty()) null else list[0]
    }

    private fun initRecyclerView() {
        mBinding.recyclerView.linear().setup {
            if (userRole == Constants.ROOM_USER_TYPE_NORMAL) {
                addType<MicQueueUserModel> { R.layout.room_dialog_mic_queue_user_item }
                onBind {
                    findView<TextView>(R.id.tv_index).text = (modelPosition + 1).toString()
                }
            } else {
                singleMode = true
                addType<MicQueueUserModel> { R.layout.room_dialog_mic_queue_manager_item }
                onChecked { position, isChecked, allChecked ->
                    val model = getModel<MicQueueUserModel>(position)
                    model.isChecked = isChecked
                    model.notifyChange()
                }
                onFastClick(R.id.iv_checkbox) {
                    checkedSwitch(modelPosition)
                }
                onClick(R.id.iv_delete) {
                    val model = getModel<MicQueueUserModel>()
                    if (model.isChecked) {
                        val array = JSONArray()
                        array.put(model.userId)
                        mViewModel.cancelQueue(crId, array) {

                        }
                    } else {
                        ToastUtils.showShort("请选择要取消排麦的用户")
                    }
                }
            }
        }.models = mutableListOf()
    }

    override fun observerData() {
        super.observerData()
        collectData(mViewModel.getQueueUsersEvent, onSuccess = {
            mBinding.root.visibility = View.VISIBLE
            if (it.isEmpty()) {
                mBinding.ivEmpty.visibility = View.VISIBLE
                mBinding.root.visibility = View.INVISIBLE
            }
            mBinding.recyclerView.bindingAdapter.addModels(it)
        }, onEmpty = {
            mBinding.ivEmpty.visibility = View.VISIBLE
            mBinding.root.visibility = View.INVISIBLE
        })
        collectData(mViewModel.inviteMicEvent, onSuccess = {
            removeRecyclerItem()
        })
        lifecycleScope.launchWhenCreated {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                mViewModel.cancelQueueEvent.collect {
                    val adapter = mBinding.recyclerView.bindingAdapter
                    val list = adapter.mutable as MutableList<MicQueueUserModel>
                    val checkedList = adapter.checkedPosition
//                    when (it) {
//                        CANCEL_MODE_ALL -> {
//                            checkedList.clear()
//                            list.clear()
//                            adapter.notifyDataSetChanged()
//                            mBinding.ivEmpty.visibility = View.VISIBLE
//                            mBinding.root.visibility = View.INVISIBLE
//                        }
//                        CANCEL_MODE_OTHER -> {
//                            list.removeAt(checkedList[0])
//                            adapter.notifyItemRemoved(checkedList[0])
//                            if (list.isEmpty()) {
//                                mBinding.ivEmpty.visibility = View.VISIBLE
//                                mBinding.root.visibility = View.INVISIBLE
//                            }
//                        }
//                        CANCEL_MODE_MINE -> {
//                            val model = list.find { that -> that.userId == MMKVProvider.userId }
//                            val index = list.indexOf(model)
//                            list.removeAt(index)
//                            adapter.notifyItemRemoved(index)
//                            if (list.isEmpty()) {
//                                mBinding.ivEmpty.visibility = View.VISIBLE
//                                mBinding.root.visibility = View.INVISIBLE
//                            }
//                        }
//                    }

                }
            }
        }

    }


    private fun removeRecyclerItem() {
        val adapter = mBinding.recyclerView.bindingAdapter
        val checkedIndex = adapter.checkedPosition[0]
        adapter.mutable.removeAt(checkedIndex)
        adapter.notifyItemRemoved(checkedIndex)
        adapter.checkedPosition.clear()
        if (adapter.mutable.isEmpty()) {
            mBinding.ivEmpty.visibility = View.VISIBLE
            mBinding.root.visibility = View.INVISIBLE
        }
    }
}
