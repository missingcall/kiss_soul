package com.kissspace.android.ui.fragment

import android.os.Bundle
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.drake.brv.utils.addModels
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.linear
import com.drake.brv.utils.mutable
import com.drake.brv.utils.setup
import com.kissspace.android.R
import com.kissspace.android.databinding.FragmentPartyPageListBinding
import com.kissspace.android.viewmodel.PartyViewModel
import com.kissspace.common.base.BaseFragment
import com.kissspace.common.config.Constants
import com.kissspace.common.ext.safeClick
import com.kissspace.common.model.RoomListBean
import com.kissspace.common.util.getMutable
import com.kissspace.common.util.jumpRoom
import com.kissspace.network.result.collectData
import com.kissspace.room.manager.RoomServiceManager

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/11/24
 * @Description: 派对列表fragment
 *
 */
class PartyPageListFragment : BaseFragment(R.layout.fragment_party_page_list) {
    private val mBinding by viewBinding<FragmentPartyPageListBinding>()
    private val mViewModel by viewModels<PartyViewModel>()
    private lateinit var mTageId: String
    private var position = 0
    private val mPageSize = 20
    private var isRefresh = true

    companion object {
        fun newInstance(position: Int, tagId: String) = PartyPageListFragment().apply {
            arguments = bundleOf("position" to position, "tagId" to tagId)
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        arguments?.let {
            mTageId = it.getString("tagId")!!
            position = it.getInt("position", 0)!!
        }
        initData()
        initRecyclerView()
    }

    private fun initData() {
        mViewModel.getRoomList(mTageId, mBinding.refreshLayout.index, mPageSize)
        mBinding.refreshLayout.apply {
            onLoadMore {
                isRefresh = false
                mViewModel.getRoomList(mTageId, mBinding.refreshLayout.index, mPageSize)
            }

            onRefresh {
                mViewModel.getRoomList(mTageId, mBinding.refreshLayout.index, mPageSize)
            }
        }
    }


    private fun initRecyclerView() {
        mBinding.recyclerView.linear().setup {
            addType<RoomListBean> {
                if (this.banner.isNotEmpty()) {
                    R.layout.layout_room_list_banner
                } else {
                    R.layout.layout_party_page_list_item
                }
            }
            onBind {
                if (itemViewType == R.layout.layout_party_page_list_item) {
                    findView<ConstraintLayout>(R.id.root).safeClick {
                        val model = getModel<RoomListBean>()
                        jumpRoom(model.crId, Constants.ROOM_TYPE_PARTY, roomList = mBinding.recyclerView.getMutable())
                    }
                }
            }
        }.models = mutableListOf()
    }

    fun onRefresh() {
        isRefresh = true
        mBinding.refreshLayout.index = 1
        mBinding.refreshLayout.refresh()
    }

    override fun createDataObserver() {
        super.createDataObserver()
        collectData(mViewModel.roomListEvent, onSuccess = {
            if (mBinding.refreshLayout.index == 1 && position == 0) {
                mViewModel.getRoomListBanner()
            }
            (parentFragment as PartyFragment).finishRefresh()
            if (!isRefresh) {
                for (record in it.records) {
                    val roomBean = mBinding.recyclerView.mutable.find { any ->
                        record.crId == (any as RoomListBean).crId
                    }
                    if (roomBean != null) {
                        val index = mBinding.recyclerView.mutable.indexOf(roomBean)
                        mBinding.recyclerView.mutable.removeAt(index)
                        mBinding.recyclerView.bindingAdapter.notifyItemRemoved(index)
                    }
                }
            }
            mBinding.refreshLayout.addData(it.records, hasMore = {
                it.records.size == 20
            }, isEmpty = {
                it.records.isEmpty()
            })
        })

        collectData(mViewModel.roomListBannerEvent, onSuccess = {
            val banner =
                it.filter { item -> item.state == "001" && item.os == "001" }
            if (banner.isNotEmpty()) {
                mBinding.recyclerView.addModels(
                    mutableListOf(RoomListBean(banner = banner.toMutableList())),
                    index = 2
                )
            }
        })
    }
}


