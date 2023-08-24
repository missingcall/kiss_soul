package com.kissspace.mine.ui.fragment

import android.os.Bundle
import android.widget.TextView
import androidx.fragment.app.viewModels
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.kissspace.common.base.BaseFragment
import com.kissspace.common.binding.dataBinding
import com.kissspace.common.config.Constants
import com.kissspace.common.model.wallet.WalletRecord
import com.kissspace.common.model.wallet.WalletDetailModel
import com.kissspace.mine.viewmodel.WalletViewModel
import com.kissspace.module_mine.R
import com.kissspace.module_mine.databinding.MineFragmentWalletDetailBinding

/**
 * @Author gaohangbo
 * @Date 2022/12/31 20:31.
 * @Describe 金币钻石收益流水记录
 */
class WalletCoinGainFragment : BaseFragment(R.layout.mine_fragment_wallet_detail) {

    private val mBinding by dataBinding<MineFragmentWalletDetailBinding>()
    private val mViewModel by viewModels<WalletViewModel>()
    private val walletDetailModel = mutableListOf<WalletDetailModel>()

    companion object {
        fun newInstance(walletType: String?, position: Int): WalletCoinGainFragment {
            val args = Bundle()
            args.putString("walletType", walletType)
            args.putInt("position", position)
            val fragment = WalletCoinGainFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        val walletType = arguments?.getString("walletType")
        val position = arguments?.getInt("position")
        mBinding.pageRefreshLayout.apply {
            onRefresh {
                initData(true, walletType, position)
            }
            onLoadMore {
                initData(false, walletType, position)
            }
        }
        initData(true, walletType, position)
        when (walletType) {
            Constants.WalletType.COIN.type -> {
                mBinding.rvCoin.linear().setup {
                    addType<WalletRecord>(R.layout.mine_list_item_wallet_coin)
                    onBind {
                        val model = getModel<WalletRecord>()
                        val tvName = findView<TextView>(R.id.tv_name)
                        tvName.text = model.coinTypeStr
                    }
                }.models = walletDetailModel
            }
            Constants.WalletType.EARNS.type -> {
                mBinding.rvCoin.linear().setup {
                    addType<WalletRecord>(R.layout.mine_list_item_wallet_earns)
                    onBind {
                        val model = getModel<WalletRecord>()
                        val tvName = findView<TextView>(R.id.tv_name)
                        tvName.text = model.profitTypeStr
                    }
                }.models = walletDetailModel
            }
            Constants.WalletType.DIAMOND.type -> {
                mBinding.rvCoin.linear().setup {
                    addType<WalletRecord>(R.layout.mine_list_item_wallet_diamond)
                    onBind {
                        val model = getModel<WalletRecord>()
                        val tvName = findView<TextView>(R.id.tv_name)
                        tvName.text = model.diamondTypeStr

                    }
                }.models = walletDetailModel
            }
        }

    }

    //流水类型：001：收入，002： 支出
    private fun initData(isRefresh: Boolean, walletType: String?, position: Int?) {
        if(isRefresh){
            mBinding.pageRefreshLayout.index = 1
        }
        when (walletType) {
            Constants.WalletType.COIN.type -> {
                mViewModel.getCoinList(
                    if (position == 0) "001" else "002",
                    mBinding.pageRefreshLayout.index
                ) {
                    if (isRefresh) {
                        mBinding.rvCoin.bindingAdapter.mutable.clear()
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
                    if (mBinding.rvCoin.bindingAdapter.models?.size == it?.total) {
                        mBinding.pageRefreshLayout.finishLoadMoreWithNoMoreData()
                    }else{
                        mBinding.pageRefreshLayout.setNoMoreData(false)
                    }
                }
            }
            Constants.WalletType.EARNS.type -> {
                mViewModel.getEarnsList(
                    if (position == 0) "001" else "002",
                    mBinding.pageRefreshLayout.index,
                ) {
                    if (isRefresh) {
                        mBinding.rvCoin.bindingAdapter.mutable.clear()
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
                    if (mBinding.rvCoin.bindingAdapter.models?.size == it?.total) {
                        mBinding.pageRefreshLayout.finishLoadMoreWithNoMoreData()
                    }else{
                        mBinding.pageRefreshLayout.setNoMoreData(false)
                    }
                }
            }
            Constants.WalletType.DIAMOND.type -> {
                mViewModel.getDiamondList(
                    //家族周结收益 成员收益
                    if (position == 0) "011" else "001",
                    mBinding.pageRefreshLayout.index,
                ) {
                    if (isRefresh) {
                        mBinding.rvCoin.bindingAdapter.mutable.clear()
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
                    if (mBinding.rvCoin.bindingAdapter.models?.size == it?.total) {
                        mBinding.pageRefreshLayout.finishLoadMoreWithNoMoreData()
                    }else{
                        mBinding.pageRefreshLayout.setNoMoreData(false)
                    }
                }

            }
            else -> {

            }
        }
    }
}