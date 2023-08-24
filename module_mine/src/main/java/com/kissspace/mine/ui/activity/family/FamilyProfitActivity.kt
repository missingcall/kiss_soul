package com.kissspace.mine.ui.activity.family

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import by.kirich1409.viewbindingdelegate.viewBinding
import com.didi.drouter.annotation.Router
import com.hjq.bar.OnTitleBarListener
import com.hjq.bar.TitleBar
import com.kissspace.common.base.BaseActivity
import com.kissspace.common.config.Constants
import com.kissspace.common.router.RouterPath
import com.kissspace.common.widget.tablayout.CustomTabLayoutBean
import com.kissspace.mine.ui.fragment.FamilyRoomEarnsFragment
import com.kissspace.module_mine.R
import com.kissspace.module_mine.databinding.MineFamilyProfitBinding

/**
 * @Author gaohangbo
 * @Date 2022/12/29 08:38.
 * @Describe
 */
@Router(path = RouterPath.PATH_FAMILY_PROFIT)
class FamilyProfitActivity : BaseActivity(R.layout.mine_family_profit) {
        val mBinding by viewBinding<MineFamilyProfitBinding>()
    private var familyId:String?=null
    override fun initView(savedInstanceState: Bundle?) {
        familyId=intent.getStringExtra("familyId")
        mBinding.titleBar.setOnTitleBarListener(object : OnTitleBarListener {
            override fun onLeftClick(titleBar: TitleBar?) {
                super.onLeftClick(titleBar)
                finish()
            }
        })
        addTab()
        initViewPager()
    }

    private fun addTab() {
        mBinding.customTabLayout.setOnTabChangedListener {
            mBinding.viewPager.currentItem = it
        }
        mBinding.customTabLayout.setViewPager(mBinding.viewPager)
        val test1 = CustomTabLayoutBean(
            "今日流水",
            0,
            0,
            true
        )

        val test2 = CustomTabLayoutBean(
            "七日流水",
            1,
            0,
            false
        )
        mBinding.customTabLayout.addTabItem(test1)
        mBinding.customTabLayout.addTabItem(test2)
    }

    private fun initViewPager(){
        mBinding.viewPager.apply {
            adapter = object : FragmentStateAdapter(this@FamilyProfitActivity) {
                override fun getItemCount(): Int = 2
                override fun createFragment(position: Int): Fragment = if (position==0){
                    FamilyRoomEarnsFragment.newInstance(Constants.FamilyEarnsType.FamilyInToady.type,familyId)
                }else FamilyRoomEarnsFragment.newInstance(Constants.FamilyEarnsType.FamilyInSevenToady.type,familyId)
            }
        }
    }
}