package com.kissspace.mine.ui.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.SpannedString
import androidx.core.text.buildSpannedString
import androidx.viewpager2.adapter.FragmentStateAdapter
import by.kirich1409.viewbindingdelegate.viewBinding
import com.angcyo.tablayout.delegate2.ViewPager2Delegate
import com.didi.drouter.annotation.Router
import com.didi.drouter.api.DRouter
import com.hjq.bar.OnTitleBarListener
import com.hjq.bar.TitleBar
import com.kissspace.common.base.BaseActivity
import com.kissspace.common.router.jump
import com.kissspace.common.ext.safeClick
import com.kissspace.common.ext.setMarginStatusBar
import com.kissspace.common.http.getSelectPayChannelList
import com.kissspace.common.provider.IPayProvider
import com.kissspace.common.router.RouterPath
import com.kissspace.mine.ui.fragment.GoodsListFragment
import com.kissspace.module_mine.R
import com.kissspace.module_mine.databinding.MineActivityStoreBinding
import com.kissspace.common.http.getUserInfo
import com.kissspace.util.formatDecimal
import com.kissspace.util.immersiveStatusBar
import com.kissspace.util.loadImage
import com.kissspace.util.size
import com.kissspace.util.sp
import java.math.RoundingMode

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/12/29 21:18
 * @Description: 装扮商城页
 *
 */
@Router(uri = RouterPath.PATH_STORE)
class StoreActivity : com.kissspace.common.base.BaseActivity(R.layout.mine_activity_store) {
    private val mBinding by viewBinding<MineActivityStoreBinding>()
    override fun initView(savedInstanceState: Bundle?) {
        immersiveStatusBar(false)
        requestUserInfo()
        mBinding.titleBar.setMarginStatusBar()
        mBinding.titleBar.setOnTitleBarListener(object : OnTitleBarListener {
            override fun onLeftClick(titleBar: TitleBar?) {
                finish()
            }

            override fun onRightClick(titleBar: TitleBar?) {
                jump(RouterPath.PATH_MY_DRESS_UP)
            }
        })
        mBinding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = 3

            override fun createFragment(position: Int) =
                when (position) {
                    0 -> GoodsListFragment.newInstance("001")
                    1 -> GoodsListFragment.newInstance("002")
                    else -> GoodsListFragment.newInstance("003")
                }
        }
        ViewPager2Delegate.install(mBinding.viewPager, mBinding.tabLayout)
        mBinding.tvRecharge.safeClick {
            getSelectPayChannelList { list ->
                DRouter.build(IPayProvider::class.java).getService()
                    .showPayDialogFragment(supportFragmentManager, list)
            }
        }

    }

    @SuppressLint("SetTextI18n")
    fun requestUserInfo() {
        getUserInfo(onSuccess = {
            mBinding.tvBalanceCoin.text = appendBalance(it.coin)
            mBinding.tvBalancePoints.text = appendBalance(it.integral)
        })
    }

    private fun formatBalance(balance: Double) = run {
        if (balance >= 100000000) {
            (balance / 100000000).formatDecimal(0, RoundingMode.DOWN)
        } else if (balance >= 1000000) {
            (balance / 10000).formatDecimal(0, RoundingMode.DOWN)
        } else {
            balance.formatDecimal(0, RoundingMode.DOWN)
        }
    }

    private fun appendBalance(balance: Double): SpannedString {
        return buildSpannedString {
            size(40.sp.toInt()) {
                append(formatBalance(balance))
            }
            if (balance >= 1000000) {
                size(22.sp.toInt()) {
                    append(if (balance >= 100000000) "亿" else "万")
                }
            }

        }
    }
}