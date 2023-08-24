package com.kissspace.android.ui.fragment

import android.os.Bundle
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import by.kirich1409.viewbindingdelegate.viewBinding
import com.drake.brv.utils.addModels
import com.drake.brv.utils.mutable
import com.drake.brv.utils.setup
import com.kissspace.android.R
import com.kissspace.android.databinding.FragmentHomePageListBinding
import com.kissspace.android.viewmodel.HomePageListViewModel
import com.kissspace.common.base.BaseFragment
import com.kissspace.common.ext.safeClick
import com.kissspace.common.model.RoomListBean
import com.kissspace.common.util.jumpRoom
import com.kissspace.network.result.collectData

class HomePageListFragment : BaseFragment(R.layout.fragment_home_page_list) {
    private val mBinding by viewBinding<FragmentHomePageListBinding>()
    private val mViewModel by viewModels<HomePageListViewModel>()
    private lateinit var mTageId: String
    private var position: Int = 0
    private val mRoomList = mutableListOf<RoomListBean>()
    private val mPageSize = 20
    private var isRefresh = true

    companion object {
        fun newInstance(position: Int, tagId: String) = HomePageListFragment().apply {
            arguments = bundleOf("position" to position, "tagId" to tagId)
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        arguments?.let {
            mTageId = it.getString("tagId")!!
            position = it.getInt("position")!!
        }
        initRecyclerView()
        mBinding.refreshLayout.onLoadMore {
            isRefresh = false
            mViewModel.getRoomList(mTageId, mBinding.refreshLayout.index, mPageSize)
        }
        mViewModel.getRoomList(mTageId, mBinding.refreshLayout.index, mPageSize)
    }

    private fun initRecyclerView() {
        val gridLayoutManager = GridLayoutManager(context, 2)
        gridLayoutManager.spanSizeLookup = object : SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val viewType = mBinding.recyclerView.adapter?.getItemViewType(position)
                return if (viewType == R.layout.layout_home_page_list_item) 1 else 2
            }
        }
        mBinding.recyclerView.setup {
            addType<RoomListBean> {
                if (this.banner.isNotEmpty()) {
                    R.layout.layout_room_list_banner
                } else {
                    R.layout.layout_home_page_list_item
                }
            }
            onBind {
                if (itemViewType == R.layout.layout_home_page_list_item) {
                    findView<ConstraintLayout>(R.id.root).safeClick {
                        val model = getModel<RoomListBean>()
                        jumpRoom(crId = model.crId)
                    }
                }
            }
        }.models = mRoomList
        mBinding.recyclerView.layoutManager = gridLayoutManager
    }

    fun onRefresh() {
        mBinding.refreshLayout.index = 1
        isRefresh = true
        mViewModel.getRoomList(mTageId, mBinding.refreshLayout.index, mPageSize)
    }

    override fun createDataObserver() {
        super.createDataObserver()
        collectData(mViewModel.roomListBannerEvent, onSuccess = {
            val banner = it.filter { item -> item.state == "001" && item.os == "001" }
            if (banner.isNotEmpty()) {
                mBinding.recyclerView.addModels(
                    mutableListOf(RoomListBean(banner = banner.toMutableList())), index = 2
                )
            }
        })
        collectData(mViewModel.roomList, onSuccess = {
            if (mBinding.refreshLayout.index == 1 && position == 0) {
                mViewModel.getRoomListBanner()
            }
            if (isRefresh) {
                mBinding.recyclerView.mutable.clear()
            }
            mBinding.refreshLayout.addData(it.records, hasMore = {
                it.records.size == 20
            }, isEmpty = {
                it.records.isEmpty()
            })
        })
    }
}