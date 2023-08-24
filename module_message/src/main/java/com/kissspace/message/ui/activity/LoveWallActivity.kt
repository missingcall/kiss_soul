package com.kissspace.message.ui.activity

import android.os.Bundle
import androidx.viewpager2.adapter.FragmentStateAdapter
import by.kirich1409.viewbindingdelegate.viewBinding
import com.angcyo.tablayout.delegate2.ViewPager2Delegate
import com.didi.drouter.annotation.Router
import com.hjq.bar.OnTitleBarListener
import com.hjq.bar.TitleBar
import com.kissspace.common.base.BaseActivity
import com.kissspace.common.router.RouterPath
import com.kissspace.common.widget.tablayout.CustomTabLayoutBean
import com.kissspace.message.ui.fragment.LoveWallFragment
import com.kissspace.message.widget.LoveWallRuleDialog
import com.kissspace.module_message.R
import com.kissspace.module_message.databinding.MessageActivityLoveWallBinding

/**
 *
 * @Author: nicko
 * @CreateDate: 2023/1/5 19:59
 * @Description: 真爱墙页面
 *
 */
@Router(path = RouterPath.PATH_LOVE_WALL)
class LoveWallActivity : com.kissspace.common.base.BaseActivity(R.layout.message_activity_love_wall) {
    private val mBinding by viewBinding<MessageActivityLoveWallBinding>()

    override fun initView(savedInstanceState: Bundle?) {

        mBinding.titleBar.setOnTitleBarListener(object : OnTitleBarListener {
            override fun onLeftClick(titleBar: TitleBar?) {
                finish()
            }

            override fun onRightClick(titleBar: TitleBar?) {
                LoveWallRuleDialog().show(supportFragmentManager)
            }
        })
        mBinding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = 2

            override fun createFragment(position: Int) =
                if (position == 0) LoveWallFragment.newInstance("001") else LoveWallFragment.newInstance(
                    "002"
                )
        }

        initTable()

    }

    private fun initTable() {
        mBinding.customTabLayout.setOnTabChangedListener {
            mBinding.viewPager.currentItem = it
        }
        mBinding.customTabLayout.setViewPager(mBinding.viewPager)
        val test1 = CustomTabLayoutBean(
            "最新",
            0,
            R.mipmap.message_love_wall_table_new,
            true
        )

        val test2 = CustomTabLayoutBean(
            "最壕",
            1,
            R.mipmap.message_love_wall_table_money,
            false
        )
        mBinding.customTabLayout.addTabItem(test1)
        mBinding.customTabLayout.addTabItem(test2)
    }

}