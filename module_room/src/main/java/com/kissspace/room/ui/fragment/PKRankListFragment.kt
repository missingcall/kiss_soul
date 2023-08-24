package com.kissspace.room.ui.fragment

import android.os.Bundle
import by.kirich1409.viewbindingdelegate.viewBinding
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.kissspace.common.base.BaseFragment
import com.kissspace.module_room.R
import com.kissspace.module_room.databinding.RoomFragmentRankListBinding

class PKRankListFragment : BaseFragment(R.layout.room_fragment_rank_list) {
    private val mBinding by viewBinding<RoomFragmentRankListBinding>()

    override fun initView(savedInstanceState: Bundle?) {
        mBinding.recyclerView.linear().setup {

        }
    }
}