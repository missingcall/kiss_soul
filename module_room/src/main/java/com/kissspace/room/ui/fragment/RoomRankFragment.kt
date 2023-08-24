package com.kissspace.room.ui.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import by.kirich1409.viewbindingdelegate.viewBinding
import com.angcyo.tablayout.delegate2.ViewPager2Delegate
import com.kongzue.dialogx.dialogs.CustomDialog
import com.kongzue.dialogx.interfaces.OnBindView
import com.kissspace.common.base.BaseFragment
import com.kissspace.module_room.R
import com.kissspace.module_room.databinding.RoomFragmentRankBinding
import com.kissspace.room.viewmodel.LiveViewModel

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/12/13 17:56
 * @Description: 房间排行榜页面
 *
 */
class RoomRankFragment : BaseFragment(R.layout.room_fragment_rank) {
    private val mBinding by viewBinding<RoomFragmentRankBinding>()
    private val mViewModel by activityViewModels<LiveViewModel>()
    private lateinit var crId: String
    private lateinit var userRole: String

    companion object {
        fun newInstance(crId: String, userRole: String) = RoomRankFragment().apply {
            arguments = bundleOf("crId" to crId, "userRole" to userRole)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        crId = arguments?.getString("crId")!!
        userRole = arguments?.getString("userRole")!!
    }

    override fun initView(savedInstanceState: Bundle?) {
        mBinding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = 2

            override fun createFragment(position: Int): Fragment {
                val type =
                    if (position == 0) RoomRankingTypeFragment.TYPE_INCOME else RoomRankingTypeFragment.TYPE_EXPEND
                return RoomRankingTypeFragment.newInstance(type, crId, userRole)
            }
        }
        ViewPager2Delegate.install(mBinding.viewPager, mBinding.tabLayout)
        mBinding.ivTips.setOnClickListener {
            CustomDialog.build().apply {
                maskColor = Color.parseColor("#4D000000")
                setCustomView(object :
                    OnBindView<CustomDialog>(R.layout.room_dialog_rank_rule) {
                    override fun onBind(dialog: CustomDialog?, v: View?) {
                        v?.findViewById<ImageView>(R.id.iv_close)?.setOnClickListener {
                            dialog?.dismiss()
                        }
                    }
                })
            }.show()
        }

    }
}