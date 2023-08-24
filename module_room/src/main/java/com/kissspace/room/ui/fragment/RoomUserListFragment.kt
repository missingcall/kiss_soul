package com.kissspace.room.ui.fragment

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.drake.brv.utils.*
import com.kissspace.common.base.BaseFragment
import com.kissspace.common.model.RoomOnLineUserListBean
import com.kissspace.module_room.R
import com.kissspace.module_room.databinding.RoomFragmentUserListBinding
import com.kissspace.network.result.collectData
import com.kissspace.room.viewmodel.LiveViewModel
import com.kissspace.room.widget.RoomUserProfileDialog

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/12/13 17:57
 * @Description: 房间在线用户列表fragment
 *
 */
class RoomUserListFragment : BaseFragment(R.layout.room_fragment_user_list) {
    private val mBinding by viewBinding<RoomFragmentUserListBinding>()
    private val mViewModel by activityViewModels<LiveViewModel>()
    private lateinit var crId: String
    private lateinit var userRole: String

    companion object {
        fun newInstance(crId: String, role: String) = RoomUserListFragment().apply {
            arguments = bundleOf("crId" to crId, "userRole" to role)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        crId = arguments?.getString("crId")!!
        userRole = arguments?.getString("userRole")!!
    }

    override fun onResume() {
        super.onResume()
        mViewModel.getOnLineUsers(crId)
    }

    override fun initView(savedInstanceState: Bundle?) {
        mBinding.recyclerView.linear().setup {
            addType<RoomOnLineUserListBean> { R.layout.room_user_list_item }
            onFastClick(R.id.iv_avatar) {
                val model = getModel<RoomOnLineUserListBean>()
                RoomUserProfileDialog.newInstance(model.userId, userRole, crId)
                    .show(childFragmentManager)
            }
        }.models = mutableListOf()
    }

    override fun createDataObserver() {
        super.createDataObserver()
        collectData(mViewModel.getOnLineUsersEvent, onSuccess = {
            mBinding.recyclerView.mutable.clear()
            mBinding.recyclerView.addModels(it)
        })
    }
}