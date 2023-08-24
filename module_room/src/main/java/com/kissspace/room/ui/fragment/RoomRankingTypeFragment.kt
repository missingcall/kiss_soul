package com.kissspace.room.ui.fragment

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import by.kirich1409.viewbindingdelegate.viewBinding
import com.angcyo.tablayout.delegate2.ViewPager2Delegate
import com.kissspace.common.base.BaseFragment
import com.kissspace.module_room.R
import com.kissspace.module_room.databinding.RoomFragmentRankingTypeBinding
import com.kissspace.room.viewmodel.LiveViewModel

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/12/13 17:57
 * @Description: 排行榜类型 魅力榜/贡献榜
 *
 */
class RoomRankingTypeFragment : BaseFragment(R.layout.room_fragment_ranking_type) {
    private val mBinding by viewBinding<RoomFragmentRankingTypeBinding>()
    private val mViewModel by viewModels<LiveViewModel>()
    private lateinit var crId: String
    private lateinit var userRole: String
    private var type = 0

    companion  object {
        const val TYPE_INCOME = 0x1
        const val TYPE_EXPEND = 0x2
        fun newInstance(type: Int, crId: String, userRole: String) =
            RoomRankingTypeFragment().apply {
                arguments = bundleOf("type" to type, "crId" to crId, "userRole" to userRole)
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        type = arguments?.getInt("type")!!
        crId = arguments?.getString("crId")!!
        userRole = arguments?.getString("userRole")!!
    }


    override fun initView(savedInstanceState: Bundle?) {
        mBinding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = 2

            override fun createFragment(position: Int): Fragment {
                val rankCycle = if (position == 0) "001" else "002"
                val rankType = if (type == TYPE_INCOME) "001" else "002"
                return RoomRankListFragment.newInstance(crId, rankCycle, rankType, userRole)
            }

        }
        ViewPager2Delegate.install(mBinding.viewPager, mBinding.tabLayout)
    }


}