package com.kissspace.room.ui.fragment

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.widget.ImageView
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.grid
import com.drake.brv.utils.mutable
import com.drake.brv.utils.setup
import com.kissspace.common.base.BaseFragment
import com.kissspace.common.config.Constants
import com.kissspace.common.flowbus.Event
import com.kissspace.common.flowbus.FlowBus
import com.kissspace.common.model.GiftModel
import com.kissspace.common.model.immessage.GiftMessage
import com.kissspace.common.util.mmkv.MMKVProvider
import com.kissspace.common.util.getMutable
import com.kissspace.module_room.R
import com.kissspace.module_room.databinding.RoomFragmentGiftListBinding
import com.kissspace.network.net.Method
import com.kissspace.network.result.collectData
import com.kissspace.network.net.request
import com.kissspace.room.http.RoomApi
import com.kissspace.room.viewmodel.GiftViewModel
import com.kissspace.room.widget.GiftDialog

/**
 *
 * @Author: nicko
 * @CreateDate: 2023/2/18 14:22
 * @Description: 礼物列表
 *
 */
class GiftListFragment : BaseFragment(R.layout.room_fragment_gift_list) {
    private lateinit var tabId: String
    private var position: Int = 0
    private var isDarkMode = true
    private var isOpenBox = false
    private var isPackGift = false
    private val mBinding by viewBinding<RoomFragmentGiftListBinding>()
    private val mViewModel by viewModels<GiftViewModel>()

    companion object {
        fun newInstance(
            tabId: String,
            position: Int,
            isOpenBox: Boolean,
            isDarkMode: Boolean,
            isPackGift: Boolean
        ) = GiftListFragment().apply {
            arguments = bundleOf(
                "tabId" to tabId,
                "position" to position,
                "isDarkMode" to isDarkMode,
                "isOpenBox" to isOpenBox,
                "isPackGift" to isPackGift
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            tabId = it.getString("tabId")!!
            position = it.getInt("position")
            isDarkMode = it.getBoolean("isDarkMode")
            isOpenBox = it.getBoolean("isOpenBox")
            isPackGift = it.getBoolean("isPackGift")
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        mBinding.vm = mViewModel
        initRecyclerView()
        mViewModel.requestGiftList(tabId)
    }

    private fun initRecyclerView() {
        mBinding.stateLayout.emptyLayout =
            if (isDarkMode) R.layout.room_gift_empty_dark else R.layout.room_gift_empty_light

        mBinding.recyclerView.grid(4).setup {
            singleMode = true
            addType<GiftModel> {
                if (isDarkMode) R.layout.room_dialog_gift_recycler_item_dark else R.layout.room_dialog_gift_recycler_item_light
            }
            onChecked { position, isChecked, _ ->
                val model = getModel<GiftModel>(position)
                model.checked = isChecked
                model.notifyChange()
                if (isChecked) {
                    val imageView =
                        mBinding.recyclerView.layoutManager?.findViewByPosition(position)
                            ?.findViewById<ImageView>(R.id.iv_gift)
                    imageView?.let { playChooseAnimation(it) }
                }
            }
            onFastClick(R.id.root) {
                if (isCheckedAll()) {
                    singleMode = true
                    checkedAll(false)
                    (requireParentFragment() as GiftDialog).checkAllGift(false)
                } else {
                    checkedSwitch(modelPosition)
                    FlowBus.post(Event.ClearGiftChecked(tabId))
                    val model = getModel<GiftModel>()
                    (requireParentFragment() as GiftDialog).onGiftClick(model)
                }
            }
        }.models = mutableListOf()
    }

    override fun createDataObserver() {
        super.createDataObserver()
        collectData(mViewModel.giftListEvent, onSuccess = {
            mBinding.recyclerView.bindingAdapter.addModels(it)
            //恢复上次选中的礼物，并滚动到该位置
            if (tabId != Constants.GIFT_TAB_ID_PACKAGE) {
                var position = 0
                it.forEachIndexed { index, model ->
                    if (isOpenBox) {
                        if (model.giftOrBox == "002" && model.boxType == "002") {
                            mBinding.recyclerView.bindingAdapter.setChecked(index, true)
                            (requireParentFragment() as GiftDialog).onGiftClick(model)
                        }
                    } else if (model.id == MMKVProvider.lastGiftIds) {
                        position = index
                        mBinding.recyclerView.bindingAdapter.setChecked(index, true)
                        (requireParentFragment() as GiftDialog).onGiftClick(model)
                    }
                }
                mBinding.recyclerView.scrollToPosition(position)
            }
        }, onEmpty = {
            mBinding.stateLayout.showEmpty()
        })

        FlowBus.observerEvent<Event.ClearGiftChecked>(this) {
            if (tabId != it.tabId) {
                mBinding.recyclerView.bindingAdapter.checkedAll(false)
                (requireParentFragment() as GiftDialog).checkAllGift(false)
            }
        }

        FlowBus.observerEvent<Event.RefreshIntegralEvent>(this) {
            val params = mutableMapOf<String, Any?>()
            params["giftTabId"] = tabId
            if (tabId != Constants.GIFT_TAB_ID_PACKAGE) {
                request<MutableList<GiftModel>>(RoomApi.API_GET_GIFT_BY_ID,
                    Method.GET,
                    params,
                    onSuccess = {
                        val model =
                            it.find { that -> that.giftOrBox == "002" && that.boxType == "002" }
                        if (model != null) {
                            val box = mBinding.recyclerView.getMutable<GiftModel>()
                                ?.find { that -> that.id == model.id }
                            box?.freeNumber = model.freeNumber
                            box?.notifyChange()
                        }
                    })
            }
        }
    }

    fun getCheckedGift() = mBinding.recyclerView.bindingAdapter.getCheckedModels<GiftModel>()

    fun checkAllGift(isCheckedAll: Boolean) {
        mBinding.recyclerView.bindingAdapter.singleMode = !isCheckedAll
        mBinding.recyclerView.bindingAdapter.checkedAll(isCheckedAll)
    }

    fun clearChecked(){
        mBinding.recyclerView.bindingAdapter.checkedAll(false)
    }

    private fun playChooseAnimation(imageView: ImageView) {
        val scaleX = ObjectAnimator.ofFloat(imageView, "scaleX", 1f, 0.5f, 1f, 0.8f, 1f)
        val scaleY = ObjectAnimator.ofFloat(imageView, "scaleY", 1f, 0.5f, 1f, 0.8f, 1f)
        val animatorSet = AnimatorSet().apply {
            duration = 500
            playTogether(scaleX, scaleY)
        }
        animatorSet.start()
    }

    fun updateGiftList(giftMessage: GiftMessage) {
        giftMessage.targetUsers.forEach {
            it.giftInfos.forEach { gift ->
                val targetGift =
                    mBinding.recyclerView.getMutable<GiftModel>().find { t -> t.id == gift.giftId }
                if (targetGift != null) {
                    targetGift.num -= gift.total
                    if (targetGift.num <= 0) {
                        val index = mBinding.recyclerView.getMutable<GiftModel>().indexOfFirst { t->t.id ==targetGift.id }
                        mBinding.recyclerView.mutable.removeAt(index)
                        mBinding.recyclerView.bindingAdapter.checkedPosition.remove(index)
                        mBinding.recyclerView.bindingAdapter.notifyItemRemoved(index)
                    } else {
                        targetGift.notifyChange()
                    }
                }
            }
        }
    }
}