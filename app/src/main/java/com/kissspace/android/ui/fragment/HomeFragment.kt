package com.kissspace.android.ui.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout.LayoutParams
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
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.SizeUtils
import com.kissspace.util.dp
import com.google.android.material.appbar.AppBarLayout
import com.kissspace.mine.widget.FirstChargeDialog
import com.kissspace.pay.utils.PayUtils
import com.noober.background.drawable.DrawableCreator
import com.kissspace.android.R
import com.kissspace.android.adapter.HomeBannerAdapter
import com.kissspace.android.databinding.FragmentMainHomeBinding
import com.kissspace.android.viewmodel.HomeViewModel
import com.kissspace.common.base.BaseFragment
import com.kissspace.common.config.Constants
import com.kissspace.common.router.jump
import com.kissspace.common.ext.safeClick
import com.kissspace.common.flowbus.Event
import com.kissspace.common.flowbus.FlowBus
import com.kissspace.common.http.getSelectPayChannelList
import com.kissspace.common.model.RoomTagListBean
import com.kissspace.common.model.VideoBannerListBean
import com.kissspace.common.router.RouterPath
import com.kissspace.common.util.*
import com.kissspace.common.util.mmkv.MMKVProvider
import com.kissspace.common.widget.appbarlayout.AppBarStateChangeListener
import com.kissspace.network.result.collectData
import com.kissspace.util.immersiveStatusBar
import com.kissspace.util.logE
import com.youth.banner.config.IndicatorConfig
import com.youth.banner.indicator.RectangleIndicator
import kotlin.collections.isNotEmpty


/**
 *
 * @Author: nicko
 * @CreateDate: 2022/11/3
 * @Description: 潮播fragment
 *
 */
class HomeFragment : BaseFragment(R.layout.fragment_main_home) {
    private val mBinding by viewBinding<FragmentMainHomeBinding>()
    private val mViewModel by viewModels<HomeViewModel>()
    private val mTabList = mutableListOf<RoomTagListBean>()

    /**
     * 是否是展开状态
     */
    private var isExpanded = true

    override fun initView(savedInstanceState: Bundle?) {
        immersiveStatusBar(false)
        mBinding.m=mViewModel
        mBinding.lifecycleOwner = this
        mViewModel.isShowFirstRecharge.value = MMKVProvider.firstRecharge
        mViewModel.getHomeBanner()
        mViewModel.getRoomTagList()
        initBanner(mutableListOf())
        initViewPager()
        initRefresh()
        initClickEvent()
        initAppBarLayout()
    }

    private fun initAppBarLayout() {
        val expandedDrawable =
            DrawableCreator.Builder().setCornersRadius(ConvertUtils.dp2px(20f).toFloat())
                .setSolidColor(Color.parseColor("#50ffffff"))
                .setStrokeColor(Color.parseColor("#ffffff"))
                .setStrokeWidth(ConvertUtils.dp2px(0.5f).toFloat()).build()
        val searchExpandedDrawable = resources.getDrawable(R.mipmap.app_icon_home_search_white)
        searchExpandedDrawable.setBounds(
            0,
            0,
            searchExpandedDrawable.minimumWidth,
            searchExpandedDrawable.minimumHeight
        )
        val collapsedDrawable =
            DrawableCreator.Builder().setCornersRadius(ConvertUtils.dp2px(20f).toFloat())
                .setSolidColor(Color.parseColor("#F8F8F9"))
                .setStrokeColor(Color.parseColor("#F8F8F9"))
                .setStrokeWidth(ConvertUtils.dp2px(0.5f).toFloat()).build()
        val searchCollapsedDrawable = resources.getDrawable(com.kissspace.module_common.R.mipmap.common_icon_search)
        searchCollapsedDrawable.setBounds(
            0,
            0,
            searchExpandedDrawable.minimumWidth,
            searchExpandedDrawable.minimumHeight
        )
        mBinding.appbarLayout.addOnOffsetChangedListener(object : AppBarStateChangeListener() {
            override fun onStateChanged(appBarLayout: AppBarLayout, state: State, alpha: Int) {
                when (state) {
                    State.EXPANDED -> {
                        //展开状态
                        isExpanded = true
                        mBinding.ivRanking.setImageResource(R.mipmap.app_icon_home_my_ranking_white)
                        mBinding.ivMyRoom.setImageResource(R.mipmap.app_icon_home_my_room_white)
                        mBinding.layoutSearch.background = expandedDrawable
                        mBinding.tvSearch.setTextColor(resources.getColor(com.kissspace.module_common.R.color.common_white))
                        mBinding.tvSearch.setCompoundDrawables(
                            searchExpandedDrawable,
                            null,
                            null,
                            null
                        )
                        immersiveStatusBar(false)
                        mBinding.layoutTop.setBackgroundColor(Color.TRANSPARENT)
                        mBinding.vRadius.visibility = View.VISIBLE
                    }

                    State.COLLAPSED -> {
                        //折叠状态
                        isExpanded = false
                        mBinding.ivRanking.setImageResource(R.mipmap.icon_home_ranking)
                        mBinding.ivMyRoom.setImageResource(R.mipmap.icon_home_my_room)
                        mBinding.layoutSearch.background = collapsedDrawable
                        mBinding.tvSearch.setTextColor(resources.getColor(com.kissspace.module_common.R.color.color_A8A8B3))
                        mBinding.tvSearch.setCompoundDrawables(
                            searchCollapsedDrawable,
                            null,
                            null,
                            null
                        )
                        immersiveStatusBar(true)
                        mBinding.layoutTop.setBackgroundColor(Color.WHITE)
                        mBinding.vRadius.visibility = View.INVISIBLE
                    }

                    else -> {
                        //中间状态
                        mBinding.layoutTop.setBackgroundColor(Color.WHITE)
                        mBinding.layoutTop.background.alpha = alpha
                        if (mBinding.vRadius.visibility == View.INVISIBLE) {
                            mBinding.vRadius.visibility = View.VISIBLE
                        }
                    }
                }
            }
        })
    }

    private fun initClickEvent() {
        mBinding.ivFirstRecharge.safeClick {
            getSelectPayChannelList {
                val firstChargeDialog = FirstChargeDialog(requireContext())
                firstChargeDialog.callBack = { payChannelType: String, payProductId: String ->
                    PayUtils.pay(payChannelType, payProductId, activity as AppCompatActivity, onSuccess = {
                        mViewModel.isShowFirstRecharge.value =false
                        MMKVProvider.firstRecharge = false
                    })
                    firstChargeDialog.dismiss()
                }
                firstChargeDialog.show()
                if (it.isNotEmpty()) {
                    if (it.size > 1) {
                        firstChargeDialog.setData(
                            it[0].firstRechargePayProductListResponses,
                            it[1].firstRechargePayProductListResponses
                        )
                    }
                }
            }
        }

        mBinding.layoutSearch.safeClick {
            jump(RouterPath.PATH_SEARCH)
        }

        mBinding.ivRanking.setOnClickListener {
            val url =
                getH5Url(
                    Constants.H5.roomRankUrl,
                    true
                ) + "&fixedHeight=${BarUtils.getStatusBarHeight()}"
            logE("rank url------${url}")
            jump(RouterPath.PATH_WEBVIEW, "url" to url)
        }
        mBinding.ivMyRoom.safeClick {
            jumpRoom(roomType = Constants.ROOM_TYPE_PARTY)
        }
    }

    private fun initViewPager() {
        mBinding.viewPager.apply {
            adapter = object : FragmentStateAdapter(this@HomeFragment) {
                override fun getItemCount(): Int = mTabList.size
                override fun createFragment(position: Int): Fragment =
                    HomePageListFragment.newInstance(position, mTabList[position].roomTagId)

                override fun getItemId(position: Int): Long = position.toLong()

                //懒加载
                override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
                    super.onAttachedToRecyclerView(recyclerView)
                    recyclerView.setItemViewCacheSize(0)
                }
            }
        }
        ViewPager2Delegate.install(mBinding.viewPager, mBinding.tabLayout)
    }

    private fun initRefresh() {
        mBinding.refreshLayout.apply {
            setEnableLoadMore(false)
            setOnRefreshListener {
                mViewModel.getHomeBanner()
                mViewModel.getRoomTagList()
            }
        }
    }

    private fun initBanner(data: MutableList<VideoBannerListBean>) {
        mBinding.banner.apply {
            addBannerLifecycleObserver(this@HomeFragment)
            setAdapter(HomeBannerAdapter(requireContext(), data))
            setLoopTime(5000)
            indicator = RectangleIndicator(this@HomeFragment.context)
            setIndicatorSelectedColorRes(com.kissspace.module_common.R.color.common_white)
            setIndicatorNormalColorRes(com.kissspace.module_common.R.color.color_80FFFFFF)
            setIndicatorWidth(12.dp.toInt(), 12.dp.toInt())
            setIndicatorHeight(4.dp.toInt())
            setIndicatorMargins(IndicatorConfig.Margins(0, 0, 0, 25.dp.toInt()))
        }
    }


    override fun createDataObserver() {
        super.createDataObserver()
        collectData(mViewModel.tagList, onSuccess = {
            mBinding.refreshLayout.finishRefresh()
            addTab(it)
        }, onEmpty = {
            mBinding.refreshLayout.finishRefresh()
        }, onError = {
            mBinding.refreshLayout.finishRefresh()
        })
        collectData(mViewModel.getBannerEvent, onSuccess = {
            initBanner(it)
        })
        FlowBus.observerEvent<Event.RefreshChangeAccountEvent>(this) {
            mBinding.refreshLayout.autoRefresh()
        }
    }

    private fun addTab(tabList: MutableList<RoomTagListBean>) {
        if (areCollectionsDifferent(mTabList, tabList)) {
            mTabList.clear()
            mTabList.addAll(tabList.filter { it.state == "001" })
            mBinding.tabLayout.removeAllViews()
            mTabList.forEach {
                val param =
                    DslTabLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
                param.setMargins(SizeUtils.dp2px(10f), 0, SizeUtils.dp2px(10f), 0)
                val textView = TextView(context)
                textView.text = it.roomTag
                textView.setSingleLine()
                textView.gravity = Gravity.BOTTOM
                textView.layoutParams = param
                mBinding.tabLayout.addView(textView)
            }
            val adapter = mBinding.viewPager.adapter as FragmentStateAdapter
            adapter.notifyDataSetChanged()
        }
        val fragment = childFragmentManager.findFragmentByTag("f${mBinding.viewPager.currentItem}")
        if (fragment != null) {
            (fragment as HomePageListFragment).onRefresh()
        }
    }

    override fun onResume() {
        super.onResume()
        changeStatusBarTextColor()
        mViewModel.isShowFirstRecharge.value =MMKVProvider.firstRecharge
    }
    private fun  changeStatusBarTextColor(){
        immersiveStatusBar(!isExpanded)
    }
}