package com.kissspace.dynamic.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.viewpager2.adapter.FragmentStateAdapter
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.kissspace.dynamic.DynamicIndexFragmentFactory
import com.kissspace.common.base.BaseFragment
import com.kissspace.common.ext.safeClick
import com.kissspace.common.router.RouterPath
import com.kissspace.common.router.jump
import com.kissspace.module_dynamic.R
import com.kissspace.module_dynamic.databinding.DynamicFragmentMainBinding


/**
 * @Author gaohangbo
 * @Date 2023/7/20 10:25.
 * @Describe 动态主页
 */
class DynamicNatureFragment : BaseFragment(R.layout.dynamic_fragment_main) {

   private val mBinding by viewBinding<DynamicFragmentMainBinding>()

   private var fragmentFactory: FragmentFactory? = null

//    private  var mWebView: BridgeWebView?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        fragmentFactory = DynamicIndexFragmentFactory("recommend", "focus")
        childFragmentManager.fragmentFactory = fragmentFactory as DynamicIndexFragmentFactory
        super.onCreate(savedInstanceState)
    }

    override fun initView(savedInstanceState: Bundle?) {
        mBinding.viewPager2.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = 2

            override fun createFragment(position: Int): Fragment =
                if (position == 0) {
                    (fragmentFactory as DynamicIndexFragmentFactory).getRecommendFragment()
                } else {
                    (fragmentFactory as DynamicIndexFragmentFactory).getFocusFragment()
                }
        }
        // 将ViewPager2与TabLayout关联
        TabLayoutMediator(
            mBinding.tabLayout, mBinding.viewPager2
        ) { tab: TabLayout.Tab, position: Int ->
            if (position == 0) {
                tab.text = "推荐"
            } else {
                tab.text = "关注"
            }
        }.attach()

        mBinding.tvTitle.safeClick {
            jump(RouterPath.PATH_DYNAMIC_SEND_FRIEND)
        }


    }

}
