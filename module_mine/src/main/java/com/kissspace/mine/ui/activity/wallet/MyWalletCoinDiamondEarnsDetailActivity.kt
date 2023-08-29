package com.kissspace.mine.ui.activity.wallet

import android.os.Bundle
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.angcyo.tablayout.DslTabLayout
import com.didi.drouter.annotation.Router
import com.hjq.bar.OnTitleBarListener
import com.hjq.bar.TitleBar
import com.kissspace.common.base.BaseActivity
import com.kissspace.common.binding.dataBinding
import com.kissspace.common.config.Constants
import com.kissspace.common.router.RouterPath
import com.kissspace.common.widget.tablayout.CustomTabLayoutBean
import com.kissspace.mine.ui.fragment.WalletCoinGainFragment
import com.kissspace.module_mine.R
import com.kissspace.module_mine.databinding.MineWalletGoldDetailBinding

/**
 * @Author gaohangbo
 * @Date 2022/12/31 20:26.
 * @Describe 金币钻石明细
 */
@Router(path = RouterPath.PATH_USER_WALLET_COIN_DIAMOND_DETAIL)
class MyWalletCoinDiamondEarnsDetailActivity : BaseActivity(R.layout.mine_wallet_gold_detail) {

    private val mBinding by dataBinding<MineWalletGoldDetailBinding>()
    private var walletType: String? = null

    override fun initView(savedInstanceState: Bundle?) {
        walletType = intent.getStringExtra("walletType")
        mBinding.titleBar.setOnTitleBarListener(object : OnTitleBarListener {
            override fun onLeftClick(titleBar: TitleBar?) {
                super.onLeftClick(titleBar)
                finish()
            }
        })
        when (walletType) {
            Constants.WalletType.EARNS.type ->{
//                listTitle.add("获取")
//                listTitle.add("消耗")
                mBinding.titleBar.title = "收益明细"
                val test1 = CustomTabLayoutBean(
                    "获取",
                    0,
                    R.mipmap.mine_icon_wallet_gain,
                    true
                )
                val test2 = CustomTabLayoutBean(
                    "消耗",
                    1,
                    R.mipmap.mine_icon_wallet_cost,
                    false
                )
                mBinding.customTabLayout.addTabItem(test1)
                mBinding.customTabLayout.addTabItem(test2)
            }
            Constants.WalletType.COIN.type -> {
                mBinding.titleBar.title = "金币明细"
                val test1 = CustomTabLayoutBean(
                    "获取",
                    0,
                    R.mipmap.mine_icon_wallet_gain,
                    true
                )

                val test2 = CustomTabLayoutBean(
                    "消耗",
                    1,
                    R.mipmap.mine_icon_wallet_cost,
                    false
                )
                mBinding.customTabLayout.addTabItem(test1)
                mBinding.customTabLayout.addTabItem(test2)
            }
            Constants.WalletType.DIAMOND.type -> {
                mBinding.titleBar.title = "家族收益明细"
                val test1 = CustomTabLayoutBean(
                    "房间周结收益",
                    0,
                    R.mipmap.mine_icon_family_sevenday,
                    true
                )
                val test2 = CustomTabLayoutBean(
                    "成员收益",
                    1,
                    R.mipmap.mine_icon_family_member_earns,
                    false
                )
                mBinding.customTabLayout.addTabItem(test1)
                mBinding.customTabLayout.addTabItem(test2)
            }
            else -> {

            }
        }
        addTab()
        mBinding.viewPager.apply {
            adapter = object : FragmentStateAdapter(this@MyWalletCoinDiamondEarnsDetailActivity) {
                override fun getItemCount(): Int = 2
                override fun createFragment(position: Int): Fragment =
                    if (position == 0) {
                        WalletCoinGainFragment.newInstance(walletType,0)
                    } else {
                        WalletCoinGainFragment.newInstance(walletType,1)
                    }

                override fun getItemId(position: Int): Long = position.toLong()

                override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
                    super.onAttachedToRecyclerView(recyclerView)
                    //不懒加载
                    recyclerView.setItemViewCacheSize(0)
                }
            }
        }
//        ViewPager2Delegate.install(mBinding.viewPager, mBinding.tabLayout)
    }

    private fun addTab() {
//        val param =
//            DslTabLayout.LayoutParams(
//                LinearLayout.LayoutParams.WRAP_CONTENT,
//                LinearLayout.LayoutParams.WRAP_CONTENT
//            )
//        listTitle.forEach{ it ->
//            val textView = TextView(this)
//            textView.text = it
//            textView.gravity = Gravity.CENTER_HORIZONTAL
//            textView.layoutParams = param
//            mBinding.tabLayout.addView(textView)
//        }

        mBinding.customTabLayout.setOnTabChangedListener {
            mBinding.viewPager.currentItem = it
        }
        mBinding.customTabLayout.setViewPager(mBinding.viewPager)


    }




}