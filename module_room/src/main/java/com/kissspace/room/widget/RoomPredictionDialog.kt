package com.kissspace.room.widget

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Gravity
import androidx.core.os.bundleOf
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import com.kissspace.common.base.BaseFragment
import com.kissspace.common.config.Constants
import com.kissspace.common.widget.BaseDialogFragment
import com.kissspace.module_room.R
import com.kissspace.module_room.databinding.RoomDialogPredictionBinding
import com.kissspace.room.ui.fragment.*
import com.kissspace.room.viewmodel.PredictionViewModel

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/12/10
 * @Description: 预言弹窗
 *
 */

class RoomPredictionDialog : BaseDialogFragment<RoomDialogPredictionBinding>(Gravity.BOTTOM) {
    private val mViewModel by viewModels<PredictionViewModel>()
    private var role = Constants.ROOM_USER_TYPE_NORMAL
    private lateinit var crId: String
    private lateinit var roomType: String
    private var anchorPredictionListFragment: AnchorPredictionListFragment? = null
    private var userPredictionListFragment: UserPredictionListFragment? = null

    companion object {
        fun newInstance(crId: String, role: String, roomType: String) =
            RoomPredictionDialog().apply {
                arguments = bundleOf("crId" to crId, "role" to role, "roomType" to roomType)
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        role = arguments?.getString("role")!!
        crId = arguments?.getString("crId")!!
        roomType = arguments?.getString("roomType")!!
    }

    override fun getLayoutId() = R.layout.room_dialog_prediction

    override fun initView() {
        if (role == Constants.ROOM_USER_TYPE_ANCHOR) {
            addFragment(AnchorPredictionListFragment.newInstance(crId,roomType))
        } else {
            addFragment(UserPredictionListFragment.newInstance(crId,roomType))
        }
    }

    fun addFragment(fragment: BaseFragment) {
        if (fragment is AnchorPredictionListFragment) {
            anchorPredictionListFragment = fragment
        }
        if (fragment is UserPredictionListFragment) {
            userPredictionListFragment = fragment
        }
        childFragmentManager.commit {
            addToBackStack(null)
            setCustomAnimations(
                com.kissspace.module_common.R.anim.slide_right_in,
                com.kissspace.module_common.R.anim.slide_left_out,
                com.kissspace.module_common.R.anim.slide_left_in,
                com.kissspace.module_common.R.anim.slide_right_out
            )
            replace(R.id.container, fragment)
        }
    }


//    fun onUserBetMessage(betMessage: PredictionBetMessage) {
//        if (userPredictionListFragment?.isAdded == true) {
//            userPredictionListFragment?.onUserBetMessage(betMessage)
//        }
//        if (anchorPredictionListFragment?.isAdded == true) {
//            anchorPredictionListFragment?.onUserBetMessage(betMessage)
//        }
//    }
//
//    fun onBetResultMessage(betMessage: PredictionResultMessage) {
//        if (userPredictionListFragment?.isAdded == true) {
//            userPredictionListFragment?.onBetResultMessage(betMessage)
//        }
//        if (anchorPredictionListFragment?.isAdded == true) {
//            anchorPredictionListFragment?.onBetResultMessage(betMessage)
//        }
//    }

}
