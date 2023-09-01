package com.kissspace.mine.ui.fragment

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.didi.drouter.api.DRouter
import com.drake.brv.BindingAdapter
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.grid
import com.drake.brv.utils.setup
import com.kongzue.dialogx.dialogs.CustomDialog
import com.kongzue.dialogx.interfaces.OnBindView
import com.tencent.qgame.animplayer.AnimView
import com.kissspace.common.base.BaseFragment
import com.kissspace.common.ext.safeClick
import com.kissspace.common.http.getSelectPayChannelList
import com.kissspace.common.model.GoodsListBean
import com.kissspace.common.provider.IPayProvider
import com.kissspace.common.util.mmkv.MMKVProvider
import com.kissspace.common.util.customToast
import com.kissspace.common.util.getMP4Path
import com.kissspace.common.util.jumpRoom
import com.kissspace.common.widget.CommonConfirmDialog
import com.kissspace.mine.ui.activity.StoreActivity
import com.kissspace.mine.viewmodel.StoreViewModel
import com.kissspace.mine.widget.GoodsBuyDialog
import com.kissspace.mine.widget.PreviewDressUpDialog
import com.kissspace.module_mine.R
import com.kissspace.module_mine.databinding.MineFragmentStoreCarBinding
import com.kissspace.network.result.collectData
import com.kissspace.util.loadImage
import com.kissspace.util.loadImageCircle
import com.kissspace.util.resToString
import com.kissspace.util.toast
import kotlinx.coroutines.launch
import java.io.File

/**
 *
 * @Author: nicko
 * @CreateDate: 2022/12/30 16:12
 * @Description: 商城列表fragment
 *
 */
class GoodsListFragment : BaseFragment(R.layout.mine_fragment_store_car) {
    private val mBinding by viewBinding<MineFragmentStoreCarBinding>()
    private val mViewModel by viewModels<StoreViewModel>()
    private lateinit var type: String

    companion object {
        fun newInstance(type: String) = GoodsListFragment().apply {
            arguments = bundleOf("type" to type)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        type = arguments?.getString("type")!!
    }

    override fun initView(savedInstanceState: Bundle?) {
        initRecyclerView()
        mViewModel.getGoodsList(type)
    }

    override fun createDataObserver() {
        super.createDataObserver()
        collectData(mViewModel.getGoodsListEvent, onSuccess = {
            mBinding.recyclerView.bindingAdapter.addModels(it)
        })

        collectData(mViewModel.buyEvent, onSuccess = {
            customToast("购买成功")
            (requireActivity() as StoreActivity).requestUserInfo()
        }, onError = {
            customToast(it.message)
        })
    }

    private fun initRecyclerView() {
        mBinding.recyclerView.grid(2).setup {
            addType<GoodsListBean> {
                when (type) {
                    "001" -> R.layout.mine_layout_store_car_item
                    "002" -> R.layout.mine_layout_store_avatar_item
                    else -> R.layout.mine_layout_goods_background_item
                }
            }
            onBind {
                if (type == "002") {
                    findView<ImageView>(R.id.iv_avatar).loadImageCircle(MMKVProvider.userAvatar)
                }
                bindItem()
            }
            onFastClick(R.id.flt_car) {
                val model = getModel<GoodsListBean>()
                if (model.svga.isNotEmpty() && type == "001") {
                    PreviewDressUpDialog.newInstance(model.svga).show(childFragmentManager, "")
                } else if (type == "003") {
                    previewBackground(model.svga.ifEmpty { model.icon })
                }
            }
            onFastClick(R.id.tv_buy) {
                val model = getModel<GoodsListBean>()
                if (model.attribute == "002") return@onFastClick
                if (model.pointsPrice != null && model.coinPrice != null) {
                    GoodsBuyDialog.newInstance(model).setCallback {
                        mViewModel.buy(model.commodityInfoId, it)
                    }.show(childFragmentManager)
                } else {
                    val type = if (model.coinPrice != null) "001" else "002"
                    val title =
                        if (model.coinPrice != null) "确定花费${model.coinPrice}金币购买装扮吗" else "确定花费${model.pointsPrice}积分购买装扮吗"
                    val dialog =
                        CommonConfirmDialog(
                            requireContext(),
                            title
                        ) {
                            if (this) {
                                mViewModel.buy(model.commodityInfoId, type)
                            }
                        }
                    dialog.show()
                }

            }
        }.models = mutableListOf()
    }


    private fun BindingAdapter.BindingViewHolder.bindItem() {
        val coinPrice = findView<TextView>(R.id.tv_price_coin)
        val pointsPrice = findView<TextView>(R.id.tv_price_points)
        val coinLp = coinPrice.layoutParams as ConstraintLayout.LayoutParams
        val pointLp = pointsPrice.layoutParams as ConstraintLayout.LayoutParams
        val model = getModel<GoodsListBean>()
        when {
            model.attribute == "002" -> {
                coinPrice.visibility = View.INVISIBLE
                pointsPrice.visibility = View.INVISIBLE
            }

            model.coinPrice != null && model.pointsPrice != null -> {
                coinPrice.text = model.coinPrice.toString()
                pointsPrice.text = model.pointsPrice.toString()
                coinLp.startToStart = R.id.tv_name
                pointLp.endToEnd = R.id.tv_valid_day
                coinPrice.visibility = View.VISIBLE
                pointsPrice.visibility = View.VISIBLE

            }

            model.coinPrice != null && model.pointsPrice == null -> {
                coinPrice.text = model.coinPrice.toString()
                coinLp.startToStart = R.id.tv_name
                coinPrice.visibility = View.VISIBLE
                pointsPrice.visibility = View.INVISIBLE
            }

            model.coinPrice == null && model.pointsPrice != null -> {
                pointsPrice.text = model.pointsPrice.toString()
                pointLp.startToStart = R.id.tv_name
                pointsPrice.visibility = View.VISIBLE
                coinPrice.visibility = View.INVISIBLE
            }

        }

    }

    private fun previewBackground(url: String) {
        CustomDialog.build().apply {
            setFullScreen(true)
            setCustomView(object :
                OnBindView<CustomDialog>(R.layout.mine_dialog_preview_background) {
                override fun onBind(dialog: CustomDialog?, v: View?) {
                    val imageView = v?.findViewById<ImageView>(R.id.image_view)
                    val animView = v?.findViewById<AnimView>(R.id.anim_view)
                    if (url.endsWith(".mp4")) {
                        imageView?.visibility = View.GONE
                        animView?.visibility = View.VISIBLE
                        animView?.setLoop(Int.MAX_VALUE)
                        getMP4Path(url){
                           lifecycleScope.launch{
                               animView?.startPlay(File(it))
                           }
                        }
                    } else {
                        imageView?.visibility = View.VISIBLE
                        animView?.visibility = View.GONE
                        imageView?.loadImage(url)
                    }
                    v?.findViewById<FrameLayout>(R.id.rootView)?.safeClick {
                        dismiss()
                    }
                }
            })
        }.show()
    }
}