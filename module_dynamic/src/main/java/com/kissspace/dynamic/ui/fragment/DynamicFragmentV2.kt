package com.kissspace.dynamic.ui.fragment

import android.os.Bundle
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import by.kirich1409.viewbindingdelegate.viewBinding
import com.kissspace.common.base.BaseFragment
import com.kissspace.common.config.Constants
import com.kissspace.common.ext.safeClick
import com.kissspace.common.ext.setMarginStatusBar
import com.kissspace.common.router.RouterPath
import com.kissspace.common.router.jump
import com.kissspace.common.util.getH5Url
import com.kissspace.module_dynamic.R
import com.kissspace.module_dynamic.databinding.DynamicFragmentMainV2Binding
import com.kissspace.util.resToColor
import com.qmuiteam.qmui.kotlin.onClick

class DynamicFragmentV2:BaseFragment(R.layout.dynamic_fragment_main_v2) {
    private val mBinding by viewBinding<DynamicFragmentMainV2Binding>()
    private var currentIndex = 0

    override fun initView(savedInstanceState: Bundle?) {
        mBinding.topBar.setMarginStatusBar()
        mBinding.ivNewMessage.safeClick {
            jump(
                RouterPath.PATH_WEBVIEW,
                "url" to getH5Url(Constants.H5.dynamicInteractiveMessages),
                "showTitleBarMargin" to true
            )
        }
        initViewPager()
        initTab()
    }

    private fun  initViewPager(){
        val fragments = mutableListOf(DynamicListFragment(0),DynamicListFragment(1))
        mBinding.viewPager.apply {
            offscreenPageLimit = 2
            isUserInputEnabled = false
            adapter = object : FragmentStateAdapter(this@DynamicFragmentV2) {
                override fun getItemCount(): Int = 2
                override fun createFragment(position: Int): Fragment = fragments[position]
                override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
                    super.onAttachedToRecyclerView(recyclerView)
                    recyclerView.setItemViewCacheSize(fragments.size)
                }
            }
        }
    }

    private fun initTab(){
        mBinding.tabRecommend.onClick {
            currentIndex = 0
            resetTab(mBinding.tabRecommend)
            mBinding.viewPager.setCurrentItem(0,false)
        }
        mBinding.tabFollow.onClick {
            currentIndex = 0
            resetTab(mBinding.tabFollow)
            mBinding.viewPager.setCurrentItem(1,false)
        }

    }

    private fun resetTab(textView: TextView){
        mBinding.tabRecommend.setBackgroundResource(R.drawable.dynamic_tab_normal)
        mBinding.tabFollow.setBackgroundResource(R.drawable.dynamic_tab_normal)

        mBinding.tabRecommend.paint.isFakeBoldText = false
        mBinding.tabFollow.paint.isFakeBoldText = false

        mBinding.tabRecommend.setTextColor(com.kissspace.module_common.R.color.color_80FFFFFF.resToColor())
        mBinding.tabFollow.setTextColor(com.kissspace.module_common.R.color.color_80FFFFFF.resToColor())

        textView.setBackgroundResource(R.drawable.dynamic_tab_selected)
        textView.paint.isFakeBoldText = true
        textView.setTextColor(com.kissspace.module_common.R.color.color_FFEB71.resToColor())
    }
}