package com.kissspace.mine.ui.activity.wallet

import android.os.Bundle
import android.widget.TextView
import androidx.activity.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.didi.drouter.annotation.Router
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.kissspace.common.base.BaseActivity
import com.kissspace.common.config.Constants
import com.kissspace.common.router.parseIntent
import com.kissspace.common.ext.setTitleBarListener
import com.kissspace.common.model.wallet.WalletRecord
import com.kissspace.common.router.RouterPath
import com.kissspace.mine.viewmodel.WalletViewModel
import com.kissspace.module_mine.R
import com.kissspace.module_mine.databinding.MineActivityWalletDiamondDetailBinding

/**
 * @Author gaohangbo
 * @Date 2023/2/27 20:48.
 * @Describe 钻石兑换明细
 */
@Router(path = RouterPath.PATH_USER_WALLET_DIAMOND_EXCHANGE_DETAIL)
class MyWalletDiamondExchangeDetailActivity : BaseActivity(R.layout.mine_activity_wallet_diamond_detail) {
    private val mBinding by viewBinding<MineActivityWalletDiamondDetailBinding>()

    private val mViewModel by viewModels<WalletViewModel>()

    private val walletType by parseIntent<String>()
    override fun initView(savedInstanceState: Bundle?) {
        setTitleBarListener(mBinding.titleBar)
        when (walletType) {
//            Constants.WalletType.EARNS.type -> {
//                mViewModel.exchangeType.value = Constants.WalletType.EARNS.type
//                mViewModel.exchangeTypeTitleBg.value = resources.getDrawable(R.mipmap.mine_wallet_diamond_transfer_detail_text)
//            }
            Constants.WalletType.DIAMOND.type -> {
                mViewModel.exchangeType.value = Constants.WalletType.DIAMOND.type
//                mViewModel.exchangeTypeTitle.value = resources.getDrawable(R.mipmap.mine_wallet_diamond_transfer_detail_text)
            }

            else -> {

            }
        }
//        mBinding.titleBar.title = "${walletType}兑换明细"
        mBinding.pageRefreshLayout.apply {
            onRefresh {
                initData(true)
            }
            onLoadMore {
                initData(false)
            }
        }
        mBinding.rvDiamond.isNestedScrollingEnabled = false
        mBinding.rvDiamond.linear().setup {
            addType<WalletRecord> {
                R.layout.mine_list_item_wallet_diamond
            }
            onBind {
                val model = getModel<WalletRecord>()
                val tvName = findView<TextView>(R.id.tv_name)
                tvName.text = model.diamondTypeStr
            }
        }.models = mutableListOf()

        initData(true)

    }

    private fun initData(isRefresh: Boolean) {
        mViewModel.getDiamondList(
            "006",
            mBinding.pageRefreshLayout.index,
        ) {
            if (isRefresh) {
                mBinding.pageRefreshLayout.index = 1
                mBinding.rvDiamond.bindingAdapter.mutable.clear()
                if (it?.walletRecords?.size == 0) {
                    mBinding.pageRefreshLayout.showEmpty()
                } else {
                    mBinding.pageRefreshLayout.addData(
                        it?.walletRecords
                    )
                    mBinding.pageRefreshLayout.showContent()
                }
                mBinding.pageRefreshLayout.finishRefresh()
            } else {
                mBinding.pageRefreshLayout.addData(
                    it?.walletRecords
                )
                mBinding.pageRefreshLayout.finishLoadMore()
            }
        }
    }

}