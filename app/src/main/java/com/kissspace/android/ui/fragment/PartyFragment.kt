package com.kissspace.android.ui.fragment

import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import by.kirich1409.viewbindingdelegate.viewBinding
import com.angcyo.tablayout.DslTabLayout
import com.angcyo.tablayout.delegate2.ViewPager2Delegate
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.SizeUtils
import com.kissspace.mine.widget.FirstChargeDialog
import com.kissspace.pay.utils.PayUtils
import com.kissspace.android.R
import com.kissspace.android.databinding.FragmentMainPartyBinding
import com.kissspace.android.viewmodel.PartyViewModel
import com.kissspace.common.base.BaseFragment
import com.kissspace.common.config.Constants
import com.kissspace.common.router.jump
import com.kissspace.common.ext.safeClick
import com.kissspace.common.ext.setMarginStatusBar
import com.kissspace.common.model.RoomTagListBean
import com.kissspace.common.router.RouterPath
import com.kissspace.common.util.*
import com.kissspace.network.result.collectData
import com.kissspace.common.flowbus.Event
import com.kissspace.common.flowbus.FlowBus
import com.kissspace.common.http.getSelectPayChannelList
import com.kissspace.common.util.mmkv.MMKVProvider
import com.kissspace.util.toast
import okhttp3.Route

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/11/3
 * @Description: 派对fragment
 *
 */
class PartyFragment : BaseFragment(R.layout.fragment_main_party) {
    private val mBinding by viewBinding<FragmentMainPartyBinding>()
    private val mViewModel by viewModels<PartyViewModel>()
    private val mTabList = mutableListOf<RoomTagListBean>()
    override fun initView(savedInstanceState: Bundle?) {
        mBinding.m = mViewModel
        mBinding.lifecycleOwner = this
        mBinding.titleBar.setMarginStatusBar()
        initRefresh()
        initViewPager()
        mViewModel.getHomeBanner()
        mViewModel.getRoomTagList()

        mBinding.lltSearch.safeClick {
            jump(RouterPath.PATH_SEARCH)
        }

        mBinding.ivMyRoom.safeClick {
            jumpRoom(roomType = Constants.ROOM_TYPE_PARTY)
        }

        mBinding.ivRank.safeClick {
            val url =
                getH5Url(
                    Constants.H5.roomRankUrl,
                    true
                ) + "&fixedHeight=${BarUtils.getStatusBarHeight()}"
            jump(RouterPath.PATH_WEBVIEW, "url" to url)
        }


    }

    private fun initRefresh() {
        mBinding.refreshLayout.apply {
            setEnableLoadMore(false)
            setOnRefreshListener {
                mViewModel.getHomeBanner()
                val fragment =
                    childFragmentManager.findFragmentByTag("f${mBinding.viewPager.currentItem}")
                if (fragment != null) {
                    (fragment as PartyPageListFragment).onRefresh()
                }
            }
        }
    }


    private fun initViewPager() {
        mBinding.viewPager.apply {
            adapter = object : FragmentStateAdapter(this@PartyFragment) {
                override fun getItemCount(): Int = mTabList.size
                override fun createFragment(position: Int): Fragment =
                    PartyPageListFragment.newInstance(position, mTabList[position].roomTagId)

                override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
                    super.onAttachedToRecyclerView(recyclerView)
                    recyclerView.setItemViewCacheSize(3)
                }
            }
        }
        ViewPager2Delegate.install(mBinding.viewPager, mBinding.tabLayout)
    }

    override fun createDataObserver() {
        super.createDataObserver()
        collectData(mViewModel.tagListEvent, onSuccess = {
            mBinding.refreshLayout.finishRefresh()
            addTab(it)
        }, onEmpty = {
            mBinding.refreshLayout.finishRefresh()
        }, onError = {
            mBinding.refreshLayout.finishRefresh()
        })
        FlowBus.observerEvent<Event.RefreshChangeAccountEvent>(this) {
            mBinding.refreshLayout.autoRefresh()
        }
    }

    private fun addTab(tabList: List<RoomTagListBean>) {
        if (areCollectionsDifferent(mTabList, tabList)) {
            mTabList.clear()
            mTabList.addAll(tabList.filter { it.state == "001" })
            mBinding.tabLayout.removeAllViews()
            mTabList.forEach {
                val param = DslTabLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                param.setMargins(SizeUtils.dp2px(10f), 0, SizeUtils.dp2px(10f), SizeUtils.dp2px(10f))
                val textView = TextView(context)
                textView.text = it.roomTag
                textView.gravity = Gravity.BOTTOM
                textView.layoutParams = param
                mBinding.tabLayout.addView(textView)
            }
            val adapter = mBinding.viewPager.adapter as FragmentStateAdapter
            adapter.notifyDataSetChanged()
        }
        val fragment = childFragmentManager.findFragmentByTag("f${mBinding.viewPager.currentItem}")
        if (fragment != null) {
            (fragment as PartyPageListFragment).onRefresh()
        }
    }
    fun finishRefresh() {
        mBinding.refreshLayout.finishRefresh()
    }
}